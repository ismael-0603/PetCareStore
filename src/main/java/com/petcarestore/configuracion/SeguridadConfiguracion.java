package com.petcarestore.configuracion;

import com.petcarestore.repositorio.UsuarioRepositorio;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SeguridadConfiguracion {
  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public UserDetailsService userDetailsService(UsuarioRepositorio repositorio) {
    return correo ->
        repositorio
            .findByCorreoIgnoreCase(correo.trim())
            .map(u -> new UsuarioAutenticado(u))
            .orElseThrow(() -> new UsernameNotFoundException("Credenciales invalidas"));
  }

  @Bean
  public SecurityFilterChain filtro(HttpSecurity http, UsuarioRepositorio usuarios)
      throws Exception {
    http.authorizeHttpRequests(
            a ->
                a.requestMatchers(
                        "/login",
                        "/css/**",
                        "/js/**",
                        "/img/**",
                        "/vendor/**",
                        "/error",
                        "/error/**")
                    .permitAll()
                    .requestMatchers("/usuarios/**", "/categorias/**", "/clientes/**")
                    .hasRole("ADMINISTRADOR")
                    .requestMatchers(
                        HttpMethod.GET,
                        "/productos",
                        "/inventario",
                        "/inventario/editar/*",
                        "/imagenes/**",
                        "/",
                        "/dashboard")
                    .hasAnyRole("ADMINISTRADOR", "EMPLEADO")
                    .requestMatchers(HttpMethod.POST, "/inventario/guardar")
                    .hasAnyRole("ADMINISTRADOR", "EMPLEADO")
                    .requestMatchers("/productos/**")
                    .hasRole("ADMINISTRADOR")
                    .anyRequest()
                    .denyAll())
        .formLogin(
            f ->
                f.loginPage("/login")
                    .usernameParameter("correo")
                    .defaultSuccessUrl("/dashboard", true)
                    .permitAll())
        .logout(l -> l.logoutSuccessUrl("/login?logout").permitAll())
        .exceptionHandling(
            e ->
                e.accessDeniedHandler(
                    (request, response, exception) -> {
                      response.setStatus(403);
                      request.getRequestDispatcher("/error/403").forward(request, response);
                    }));
    http.addFilterBefore(
        new EstadoUsuarioFiltro(usuarios),
        org.springframework.security.web.access.intercept.AuthorizationFilter.class);
    return http.build();
  }
}
