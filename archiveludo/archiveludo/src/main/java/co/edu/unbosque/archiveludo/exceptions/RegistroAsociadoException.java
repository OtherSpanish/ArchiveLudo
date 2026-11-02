package co.edu.unbosque.archiveludo.exceptions;

/**
 * Se lanza al intentar eliminar algo de lo que dependen otros registros
 * (por ejemplo, un usuario que registro copias). La usan todos los DELETE.
 */
public class RegistroAsociadoException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	/**
	 * @param recursoConArticulo nombre del recurso con su articulo, por ejemplo "el usuario" o "la copia"
	 * @param id                 id del recurso que se intento eliminar
	 */
	public RegistroAsociadoException(String recursoConArticulo, long id) {
		super("No se puede eliminar " + recursoConArticulo + " con id " + id
				+ " porque tiene registros asociados");
	}

}