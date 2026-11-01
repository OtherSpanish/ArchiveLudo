package co.edu.unbosque.archiveludo.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

/**
 * Utilidad para generar, leer y validar tokens JWT (JJWT 0.12).
 * El token guarda el correo del usuario (subject) y su rol (claim "rol").
 */
@Component
public class JwtUtil {

	private static final long MILISEGUNDOS_POR_HORA = 60L * 60L * 1000L;

	/**
	 * Clave secreta inyectada desde application.properties (jwt.secret). Debe tener
	 * al menos 32 caracteres para HMAC-SHA256.
	 */
	@Value("${jwt.secret}")
	private String secreto;

	/** Horas que dura el token desde que se genera (jwt.horas-validez). */
	@Value("${jwt.horas-validez}")
	private long horasValidez;


	public String extraerCorreo(String token) {
		return extraerTodosLosClaims(token).getSubject();
	}

	public Date extraerFechaVencimiento(String token) {
		return extraerTodosLosClaims(token).getExpiration();
	}

	public String extraerRol(String token) {
		return extraerTodosLosClaims(token).get("rol", String.class);
	}

	/**
	 * Lee el contenido del token. Lanza una excepcion si la firma no coincide o si vencio.
	 */
	private Claims extraerTodosLosClaims(String token) {
		return Jwts.parser()
				.verifyWith(obtenerClaveFirma())
				.build()
				.parseSignedClaims(token)
				.getPayload();
	}

	// ──────────────────────────────────────────────
	// Generacion de token
	// ──────────────────────────────────────────────

	/**
	 * Genera un token JWT para el usuario. Incluye su rol como claim "rol"
	 * (por ejemplo ROLE_CURADOR).
	 */
	public String generarToken(UserDetails userDetails) {
		Map<String, Object> claims = new HashMap<String, Object>();
		claims.put("rol", obtenerPrimerRol(userDetails));
		return crearToken(claims, userDetails.getUsername());
	}

	/**
	 * Devuelve el primer rol del usuario. En ArchiveLudo cada usuario tiene uno solo.
	 */
	private String obtenerPrimerRol(UserDetails userDetails) {
		for (GrantedAuthority autoridad : userDetails.getAuthorities()) {
			return autoridad.getAuthority();
		}
		return "ROLE_CONSULTA";
	}

	private String crearToken(Map<String, Object> claims, String correo) {
		Date fechaEmision = new Date();
		Date fechaVencimiento = new Date(fechaEmision.getTime() + horasValidez * MILISEGUNDOS_POR_HORA);

		return Jwts.builder()
				.claims(claims)
				.subject(correo)
				.issuedAt(fechaEmision)
				.expiration(fechaVencimiento)
				.signWith(obtenerClaveFirma())
				.compact();
	}


	/**
	 * El token es valido si pertenece a ese usuario y no ha vencido.
	 */
	public boolean validarToken(String token, UserDetails userDetails) {
		String correo = extraerCorreo(token);
		return correo.equals(userDetails.getUsername()) && !estaVencido(token);
	}

	private boolean estaVencido(String token) {
		return extraerFechaVencimiento(token).before(new Date());
	}

	

	private SecretKey obtenerClaveFirma() {
		byte[] bytesClave = secreto.getBytes(StandardCharsets.UTF_8);
		return Keys.hmacShaKeyFor(bytesClave);
	}

}