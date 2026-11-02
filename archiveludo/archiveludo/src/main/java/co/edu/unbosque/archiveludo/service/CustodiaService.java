package co.edu.unbosque.archiveludo.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.archiveludo.dto.CustodiaDTO;
import co.edu.unbosque.archiveludo.entity.Copia;
import co.edu.unbosque.archiveludo.entity.Custodia;
import co.edu.unbosque.archiveludo.entity.Institucion;
import co.edu.unbosque.archiveludo.exceptions.LanzadorException;
import co.edu.unbosque.archiveludo.repository.CopiaRepository;
import co.edu.unbosque.archiveludo.repository.CustodiaRepository;
import co.edu.unbosque.archiveludo.repository.InstitucionRepository;

/**
 * Logica de negocio de las custodias: en que institucion esta (o estuvo) cada copia.
 * Reglas: una copia tiene como maximo una custodia vigente (sin fecha de fin),
 * y los periodos de custodia de una misma copia no se cruzan.
 */
@Service
public class CustodiaService {

	private final CustodiaRepository custodiaRepository;
	private final CopiaRepository copiaRepository;
	private final InstitucionRepository institucionRepository;
	private final ModelMapper mapper;

	public CustodiaService(CustodiaRepository custodiaRepository, CopiaRepository copiaRepository,
			InstitucionRepository institucionRepository, ModelMapper mapper) {
		this.custodiaRepository = custodiaRepository;
		this.copiaRepository = copiaRepository;
		this.institucionRepository = institucionRepository;
		this.mapper = mapper;
	}


	/**
	 * Devuelve el historial de custodias de una copia, de la mas reciente a la mas antigua.
	 * La vigente es la que no trae fechaFin.
	 *
	 * @param idTexto id de la copia tal como llego en la URL
	 * @return historial de custodias (vacio si la copia no tiene ninguna)
	 * @throws IdInvalidoException        si el id no es un numero mayor que cero
	 * @throws CopiaNoEncontradaException si no existe la copia
	 */
	public List<CustodiaDTO> buscarPorCopia(String idTexto) {
		long idCopia = LanzadorException.convertirId(idTexto, "copia");

		Copia copia = copiaRepository.buscarPorId(idCopia);
		LanzadorException.verificarCopiaExiste(copia, idCopia);

		List<Custodia> custodias = custodiaRepository.buscarPorIdCopia(idCopia);

		List<CustodiaDTO> custodiasDTO = new ArrayList<CustodiaDTO>();
		for (Custodia custodia : custodias) {
			custodiasDTO.add(convertirADTO(custodia));
		}
		return custodiasDTO;
	}

	/**
	 * @param idTexto id de la custodia tal como llego en la URL
	 * @return la custodia
	 * @throws IdInvalidoException           si el id no es un numero mayor que cero
	 * @throws CustodiaNoEncontradaException si no existe
	 */
	public CustodiaDTO buscarPorId(String idTexto) {
		long idCustodia = LanzadorException.convertirId(idTexto, "custodia");

		Custodia custodia = custodiaRepository.buscarPorId(idCustodia);
		LanzadorException.verificarCustodiaExiste(custodia, idCustodia);

		return convertirADTO(custodia);
	}


	/**
	 * Registra la custodia vigente de una copia. Si la copia ya tenia una vigente,
	 * esa se cierra con fecha de fin = fecha de inicio de la nueva, en la misma transaccion.
	 * La nueva siempre queda vigente: si el cuerpo trae fechaFin, se ignora.
	 *
	 * @param idTexto     id de la copia tal como llego en la URL
	 * @param custodiaDTO institucion, fecha de inicio y condicion de almacenamiento
	 * @return la custodia creada
	 * @throws IdInvalidoException              si el id de la copia o de la institucion no es valido
	 * @throws CopiaNoEncontradaException       si no existe la copia
	 * @throws InstitucionNoEncontradaException si no existe la institucion
	 * @throws DatosCustodiaInvalidosException  si las fechas o la condicion no son validas,
	 *                                          o si el periodo se cruza con otra custodia
	 */
	@Transactional
	public CustodiaDTO crear(String idTexto, CustodiaDTO custodiaDTO) {
		long idCopia = LanzadorException.convertirId(idTexto, "copia");

		Copia copia = copiaRepository.buscarPorId(idCopia);
		LanzadorException.verificarCopiaExiste(copia, idCopia);

		long idInstitucion = verificarInstitucion(custodiaDTO.getIdInstitucion());
		LocalDate fechaInicio = custodiaDTO.getFechaInicio();
		LanzadorException.verificarFechasCustodia(fechaInicio, null);
		LanzadorException.verificarCondicionAlmacenamiento(custodiaDTO.getCondicionAlmacenamiento());

		Custodia vigente = custodiaRepository.buscarVigentePorIdCopia(idCopia);
		long idVigente = 0;
		if (vigente != null) {
			LanzadorException.verificarInicioNoAnteriorAVigente(fechaInicio, vigente.getFechaInicio());
			idVigente = vigente.getIdCustodia();
		}

		// La vigente no se compara: se va a cerrar justo donde empieza la nueva, asi que no se cruzan
		verificarSinCruces(idCopia, idVigente, fechaInicio, null);

		if (vigente != null) {
			custodiaRepository.cerrar(idVigente, fechaInicio);
		}

		Custodia custodia = new Custodia(idCopia, idInstitucion, fechaInicio, null,
				custodiaDTO.getCondicionAlmacenamiento().trim());
		Custodia custodiaGuardada = custodiaRepository.crear(custodia);
		return convertirADTO(custodiaGuardada);
	}

	/**
	 * Corrige la institucion, las fechas o la condicion de una custodia. La copia no cambia.
	 * Con fechaFin en null la custodia queda vigente, lo que solo se permite si la copia
	 * no tiene otra vigente (lo detecta la verificacion de cruces).
	 *
	 * @param idTexto     id de la custodia tal como llego en la URL
	 * @param custodiaDTO datos nuevos
	 * @return la custodia actualizada
	 * @throws IdInvalidoException              si el id de la custodia o de la institucion no es valido
	 * @throws CustodiaNoEncontradaException    si no existe
	 * @throws InstitucionNoEncontradaException si no existe la institucion
	 * @throws DatosCustodiaInvalidosException  si las fechas o la condicion no son validas,
	 *                                          o si el periodo se cruza con otra custodia
	 */
	@Transactional
	public CustodiaDTO actualizar(String idTexto, CustodiaDTO custodiaDTO) {
		long idCustodia = LanzadorException.convertirId(idTexto, "custodia");

		Custodia custodia = custodiaRepository.buscarPorId(idCustodia);
		LanzadorException.verificarCustodiaExiste(custodia, idCustodia);

		long idInstitucion = verificarInstitucion(custodiaDTO.getIdInstitucion());
		LanzadorException.verificarFechasCustodia(custodiaDTO.getFechaInicio(), custodiaDTO.getFechaFin());
		LanzadorException.verificarCondicionAlmacenamiento(custodiaDTO.getCondicionAlmacenamiento());

		verificarSinCruces(custodia.getIdCopia(), idCustodia, custodiaDTO.getFechaInicio(),
				custodiaDTO.getFechaFin());

		custodia.setIdInstitucion(idInstitucion);
		custodia.setFechaInicio(custodiaDTO.getFechaInicio());
		custodia.setFechaFin(custodiaDTO.getFechaFin());
		custodia.setCondicionAlmacenamiento(custodiaDTO.getCondicionAlmacenamiento().trim());

		custodiaRepository.actualizar(custodia);
		return convertirADTO(custodia);
	}

	/**
	 * Elimina una custodia. Ninguna tabla depende de CUSTODIA, asi que no hay 409.
	 * Si era la vigente, la copia queda sin custodia vigente (la anterior no se reabre).
	 *
	 * @param idTexto id de la custodia tal como llego en la URL
	 * @throws IdInvalidoException           si el id no es un numero mayor que cero
	 * @throws CustodiaNoEncontradaException si no existe
	 */
	@Transactional
	public void eliminar(String idTexto) {
		long idCustodia = LanzadorException.convertirId(idTexto, "custodia");

		Custodia custodia = custodiaRepository.buscarPorId(idCustodia);
		LanzadorException.verificarCustodiaExiste(custodia, idCustodia);

		custodiaRepository.eliminar(idCustodia);
	}


	/**
	 * Verifica que el id de la institucion sea valido y que la institucion exista.
	 * Si no se envia, llega en 0 y da 400.
	 */
	private long verificarInstitucion(long idInstitucion) {
		LanzadorException.verificarIdValido(idInstitucion, "institucion");

		Institucion institucion = institucionRepository.buscarPorId(idInstitucion);
		LanzadorException.verificarInstitucionExiste(institucion, idInstitucion);
		return idInstitucion;
	}

	/**
	 * Compara el periodo [inicio, fin] con el de cada custodia de la copia, menos la indicada
	 * (la misma que se edita, o la vigente que se va a cerrar). Si se cruza con alguna, lanza la excepcion.
	 *
	 * @param idCopia            copia a la que pertenecen las custodias
	 * @param idCustodiaExcluida custodia que no se compara (0 si no hay)
	 * @param inicio             inicio del periodo
	 * @param fin                fin del periodo (null = sin fin, sigue vigente)
	 */
	private void verificarSinCruces(long idCopia, long idCustodiaExcluida, LocalDate inicio, LocalDate fin) {
		List<Custodia> custodias = custodiaRepository.buscarPorIdCopia(idCopia);

		for (Custodia otra : custodias) {
			if (otra.getIdCustodia() != idCustodiaExcluida) {
				boolean seCruza = periodosSeCruzan(inicio, fin, otra.getFechaInicio(), otra.getFechaFin());
				LanzadorException.verificarSinCruceDeCustodias(seCruza, otra.getIdCustodia());
			}
		}
	}

	/**
	 * Dos periodos se cruzan si cada uno empieza antes de que termine el otro.
	 * Un fin en null significa "sin fin" (custodia vigente).
	 * Que uno termine el mismo dia que empieza el otro NO es un cruce: es el traslado.
	 */
	private boolean periodosSeCruzan(LocalDate inicioA, LocalDate finA, LocalDate inicioB, LocalDate finB) {
		boolean aEmpiezaAntesDeQueTermineB = (finB == null) || inicioA.isBefore(finB);
		boolean bEmpiezaAntesDeQueTermineA = (finA == null) || inicioB.isBefore(finA);
		return aEmpiezaAntesDeQueTermineB && bEmpiezaAntesDeQueTermineA;
	}


	/**
	 * Convierte la entity a DTO y completa el nombre de la institucion.
	 */
	private CustodiaDTO convertirADTO(Custodia custodia) {
		CustodiaDTO custodiaDTO = mapper.map(custodia, CustodiaDTO.class);

		Institucion institucion = institucionRepository.buscarPorId(custodia.getIdInstitucion());
		if (institucion != null) {
			custodiaDTO.setNombreInstitucion(institucion.getNombre());
		}
		return custodiaDTO;
	}

}