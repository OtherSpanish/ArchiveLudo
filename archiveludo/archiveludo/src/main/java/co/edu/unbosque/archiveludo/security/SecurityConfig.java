package co.edu.unbosque.archiveludo.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	private final JwtAuthenticationFilter jwtAuthFilter;
	private final UserDetailsService userDetailsService;

	public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter, UserDetailsService userDetailsService) {
		this.jwtAuthFilter = jwtAuthFilter;
		this.userDetailsService = userDetailsService;
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http, CorsConfigurationSource corsConfigurationSource)
			throws Exception {
		http.cors(cors -> cors.configurationSource(corsConfigurationSource)).csrf(csrf -> csrf.disable())
				.authorizeHttpRequests(auth -> auth
						// Rutas publicas
						.requestMatchers("/api/auth/**").permitAll()
						.requestMatchers("/", "/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
						.requestMatchers("/error").permitAll()
						.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

						// Gestion de usuarios (todo el CRUD): solo administrador
						.requestMatchers("/api/usuarios/**").hasRole("ADMINISTRADOR")

						// Lectura: cualquier rol
						.requestMatchers(HttpMethod.GET, "/api/**").hasAnyRole("CONSULTA", "CURADOR", "ADMINISTRADOR")

						// Crear y actualizar: curador o administrador (consulta recibe 403)
						.requestMatchers(HttpMethod.POST, "/api/**").hasAnyRole("CURADOR", "ADMINISTRADOR")
						.requestMatchers(HttpMethod.PUT, "/api/**").hasAnyRole("CURADOR", "ADMINISTRADOR")

						// Eliminar: solo administrador (curador y consulta reciben 403)
						.requestMatchers(HttpMethod.DELETE, "/api/**").hasRole("ADMINISTRADOR")

						.anyRequest().authenticated())
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				// Sin token o con token vencido: 401 (el front cierra la sesion). Con token sin permiso: 403
				.exceptionHandling(excepcion -> excepcion.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
				.authenticationProvider(authenticationProvider())
				.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}

	@Bean
	public AuthenticationProvider authenticationProvider() {
		DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);
		authProvider.setPasswordEncoder(passwordEncoder());
		return authProvider;
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
		return config.getAuthenticationManager();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

}