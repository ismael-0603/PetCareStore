package com.petcarestore.controlador;

import com.petcarestore.repositorio.InventarioRepositorio;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class NavegacionConsejo {
  private final InventarioRepositorio inventarios;

  public NavegacionConsejo(InventarioRepositorio inventarios) {
    this.inventarios = inventarios;
  }

  @ModelAttribute("alertasInventario")
  public long alertasInventario() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
      return 0L;
    }
    try {
      return inventarios.contarStockBajo();
    } catch (RuntimeException e) {
      return 0L;
    }
  }
}
