package com.petcarestore.controlador;

import com.petcarestore.servicio.DashboardServicio;
import com.petcarestore.servicio.DashboardServicio.ResumenDashboard;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class InicioControlador {
  private final DashboardServicio dashboard;

  public InicioControlador(DashboardServicio dashboard) {
    this.dashboard = dashboard;
  }

  @GetMapping("/login")
  public String login() {
    return "login";
  }

  @GetMapping({"/", "/dashboard"})
  public String dashboard(Model model) {
    ResumenDashboard resumen = dashboard.resumen();
    model.addAttribute("totalProductos", resumen.totalProductos());
    model.addAttribute("productosActivos", dashboard.productosActivos());
    model.addAttribute("totalClientes", resumen.totalClientes());
    model.addAttribute("totalCategorias", resumen.totalCategorias());
    model.addAttribute("stockBajo", resumen.stockBajo());
    model.addAttribute("alertasReposicion", dashboard.alertasStockBajo());
    model.addAttribute("distribucionCategorias", dashboard.distribucionCategorias());
    return "dashboard";
  }

  @GetMapping("/error/403")
  public String denegado(HttpServletResponse response) {
    response.setStatus(403);
    return "error/403";
  }
}
