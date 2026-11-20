package com.petcarestore.servicio;

import com.petcarestore.modelo.Inventario;
import java.util.List;

public interface DashboardServicio {
  ResumenDashboard resumen();

  long productosActivos();

  List<Inventario> alertasStockBajo();

  List<ConteoCategoria> distribucionCategorias();

  /** Totales que muestran las tarjetas del dashboard. */
  record ResumenDashboard(
      long totalProductos, long totalClientes, long totalCategorias, long stockBajo) {}

  /** Conteo de productos por categoría para las barras del dashboard. */
  record ConteoCategoria(String nombre, long cantidad, int porcentaje) {}
}
