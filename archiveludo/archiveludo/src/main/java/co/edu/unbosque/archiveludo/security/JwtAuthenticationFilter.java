package co.edu.unbosque.archiveludo.security;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Filtro JWT que se ejecuta una vez por solicitud HTTP. Extrae el token del
 * header "Authorization: Bearer <token>", lo valida y establece la
 * autenticacion en el SecurityContext.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

	private final JwtUtil jwtUtil;
	private final UserDetailsService userDetailsService;

	public JwtAuthenticationFilter(JwtUtil jwtUtil, UserDetailsService userDetailsService) {
		this.jwtUtil = jwtUtil;
		this.userDetailsService = userDetailsService;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		final String cabeceraAutorizacion = request.getHeader("Authorization");

		String correo = null;
		String jwt = null;

		if (cabeceraAutorizacion != null && cabeceraAutorizacion.startsWith("Bearer ")) {
			jwt = cabeceraAutorizacion.substring(7);
			try {
				correo = jwtUtil.extraerCorreo(jwt);
			} catch (Exception e) {
				// Token vencido, mal firmado o mal formado: la peticion sigue sin autenticar
				log.warn("No se pudo extraer el usuario del token JWT: {}", e.getMessage());
			}
		}

		if (correo != null && SecurityContextHolder.getContext().getAuthentication() == null) {
			try {
				UserDetails userDetails = userDetailsService.loadUserByUsername(correo);

				if (jwtUtil.validarToken(jwt, userDetails)) {
					UsernamePasswordAuthenticationToken tokenAutenticacion = new UsernamePasswordAuthenticationToken(
							userDetails, null, userDetails.getAuthorities());
					tokenAutenticacion.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
					SecurityContextHolder.getContext().setAuthentication(tokenAutenticacion);
				}
			} catch (Exception e) {
				// El usuario del token ya no existe en la BD
				log.warn("No se pudo autenticar el token JWT: {}", e.getMessage());
			}
		}

		filterChain.doFilter(request, response);
	}

}