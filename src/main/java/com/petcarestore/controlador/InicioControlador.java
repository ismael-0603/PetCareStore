package com.petcarestore.controlador;

import com.petcarestore.repositorio.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class InicioControlador {
  private final ProductoRepositorio productos;
  private final ClienteRepositorio clientes;
  private final CategoriaRepositorio categorias;
  private final InventarioRepositorio inventarios;

  public InicioControlador(
      ProductoRepositorio productos,
      ClienteRepositorio clientes,
      CategoriaRepositorio categorias,
      InventarioRepositorio inventarios) {
    this.productos = productos;
    this.clientes = clientes;
    this.categorias = categorias;
    this.inventarios = inventarios;
  }

  @GetMapping("/login")
  public String login() {
    return "login";
  }

  @GetMapping({"/", "/dashboard"})
  public String dashboard(Model model) {
    model.addAttribute("totalProductos", productos.count());
    model.addAttribute("totalClientes", clientes.count());
    model.addAttribute("totalCategorias", categorias.count());
    model.addAttribute("stockBajo", inventarios.contarStockBajo());
    return "dashboard";
  }

  @GetMapping("/error/403")
  public String denegado(jakarta.servlet.http.HttpServletResponse response) {
    response.setStatus(403);
    return "error/403";
  }
}
