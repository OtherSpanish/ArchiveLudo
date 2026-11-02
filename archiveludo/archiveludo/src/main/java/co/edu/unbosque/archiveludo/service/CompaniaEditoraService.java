package co.edu.unbosque.archiveludo.service;

import java.util.ArrayList;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.archiveludo.dto.CompaniaEditoraDTO;
import co.edu.unbosque.archiveludo.entity.CompaniaEditora;
import co.edu.unbosque.archiveludo.exceptions.LanzadorException;
import co.edu.unbosque.archiveludo.repository.CompaniaEditoraRepository;

/**
 * Logica de negocio de las companias editoras (CRUD).
 */
@Service
public class CompaniaEditoraService {

	private final CompaniaEditoraRepository companiaEditoraRepository;
	private final ModelMapper mapper;

	public CompaniaEditoraService(CompaniaEditoraRepository companiaEditoraRepository, ModelMapper mapper) {
		this.companiaEditoraRepository = companiaEditoraRepository;
		this.mapper = mapper;
	}

	/**
	 * @return todas las companias editoras, ordenadas por nombre
	 */
	public List<CompaniaEditoraDTO> buscarTodas() {
		List<CompaniaEditora> editoras = companiaEditoraRepository.buscarTodas();

		List<CompaniaEditoraDTO> editorasDTO = new ArrayList<CompaniaEditoraDTO>();
		for (CompaniaEditora editora : editoras) {
			editorasDTO.add(mapper.map(editora, CompaniaEditoraDTO.class));
		}
		return editorasDTO;
	}

	/**
	 * @param idTexto id tal como llego en la URL
	 * @return la compania editora
	 * @throws IdInvalidoException                  si el id no es un numero mayor que cero
	 * @throws CompaniaEditoraNoEncontradaException si no existe
	 */
	public CompaniaEditoraDTO buscarPorId(String idTexto) {
		long idEditora = LanzadorException.convertirId(idTexto, "compañia editora");

		CompaniaEditora companiaEditora = companiaEditoraRepository.buscarPorId(idEditora);
		LanzadorException.verificarCompaniaEditoraExiste(companiaEditora, idEditora);

		return mapper.map(companiaEditora, CompaniaEditoraDTO.class);
	}

	/**
	 * Crea una compania editora nueva.
	 *
	 * @param companiaEditoraDTO datos de la editora
	 * @return la editora creada, con su id
	 * @throws CampoObligatorioException si falta el nombre o el pais
	 */
	@Transactional
	public CompaniaEditoraDTO crear(CompaniaEditoraDTO companiaEditoraDTO) {
		verificarDatos(companiaEditoraDTO);

		CompaniaEditora companiaEditora = new CompaniaEditora(companiaEditoraDTO.getNombre().trim(),
				companiaEditoraDTO.getPais().trim());

		CompaniaEditora editoraGuardada = companiaEditoraRepository.crear(companiaEditora);
		return mapper.map(editoraGuardada, CompaniaEditoraDTO.class);
	}

	/**
	 * Actualiza el nombre y el pais de una compania editora.
	 *
	 * @param idTexto            id tal como llego en la URL
	 * @param companiaEditoraDTO datos nuevos
	 * @return la editora actualizada
	 * @throws IdInvalidoException                  si el id no es un numero mayor que cero
	 * @throws CompaniaEditoraNoEncontradaException si no existe
	 * @throws CampoObligatorioException            si falta el nombre o el pais
	 */
	@Transactional
	public CompaniaEditoraDTO actualizar(String idTexto, CompaniaEditoraDTO companiaEditoraDTO) {
		long idEditora = LanzadorException.convertirId(idTexto, "compañia editora");

		CompaniaEditora companiaEditora = companiaEditoraRepository.buscarPorId(idEditora);
		LanzadorException.verificarCompaniaEditoraExiste(companiaEditora, idEditora);

		verificarDatos(companiaEditoraDTO);

		companiaEditora.setNombre(companiaEditoraDTO.getNombre().trim());
		companiaEditora.setPais(companiaEditoraDTO.getPais().trim());

		companiaEditoraRepository.actualizar(companiaEditora);
		return mapper.map(companiaEditora, CompaniaEditoraDTO.class);
	}

	/**
	 * Elimina una compania editora solo si no publica ningun videojuego.
	 * Se revisa antes de borrar para responder 409, en vez de dejar que el
	 * ON DELETE CASCADE de COMPED_VID borre la relacion con los videojuegos.
	 *
	 * @param idTexto id tal como llego en la URL
	 * @throws IdInvalidoException                  si el id no es un numero mayor que cero
	 * @throws CompaniaEditoraNoEncontradaException si no existe
	 * @throws RegistrosAsociadosException          si publica algun videojuego
	 */
	@Transactional
	public void eliminar(String idTexto) {
		long idEditora = LanzadorException.convertirId(idTexto, "compañia editora");

		CompaniaEditora companiaEditora = companiaEditoraRepository.buscarPorId(idEditora);
		LanzadorException.verificarCompaniaEditoraExiste(companiaEditora, idEditora);

		boolean tieneVideojuegos = companiaEditoraRepository.contarVideojuegosAsociados(idEditora) > 0;
		LanzadorException.verificarSinRegistrosAsociados(tieneVideojuegos, "la compañia editora", idEditora);

		companiaEditoraRepository.eliminar(idEditora);
	}

	/**
	 * Validaciones comunes a crear y actualizar.
	 */
	private void verificarDatos(CompaniaEditoraDTO companiaEditoraDTO) {
		LanzadorException.verificarCampoObligatorio(companiaEditoraDTO.getNombre(), "nombre");
		LanzadorException.verificarCampoObligatorio(companiaEditoraDTO.getPais(), "pais");
	}

}