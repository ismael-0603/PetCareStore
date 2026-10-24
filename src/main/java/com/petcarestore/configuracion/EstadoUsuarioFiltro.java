package com.petcarestore.configuracion;

import com.petcarestore.repositorio.UsuarioRepositorio;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

public class EstadoUsuarioFiltro extends OncePerRequestFilter {
  private final UsuarioRepositorio usuarios;

  public EstadoUsuarioFiltro(UsuarioRepositorio usuarios) {
    this.usuarios = usuarios;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {
    var autenticacion = SecurityContextHolder.getContext().getAuthentication();
    if (autenticacion != null
        && autenticacion.getPrincipal() instanceof UsuarioAutenticado anterior) {
      var usuario = usuarios.findById(anterior.getId());
      if (usuario.isEmpty() || !usuario.get().isEstado()) {
        SecurityContextHolder.clearContext();
        if (request.getSession(false) != null) request.getSession(false).invalidate();
        response.sendRedirect(request.getContextPath() + "/login?error");
        return;
      }
      var actual = new UsuarioAutenticado(usuario.get());
      var nueva =
          UsernamePasswordAuthenticationToken.authenticated(actual, null, actual.getAuthorities());
      nueva.setDetails(autenticacion.getDetails());
      SecurityContextHolder.getContext().setAuthentication(nueva);
    }
    chain.doFilter(request, response);
  }
}
