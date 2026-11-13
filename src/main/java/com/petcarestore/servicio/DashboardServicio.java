package com.petcarestore.servicio;

public interface DashboardServicio {
  ResumenDashboard resumen();

  /** Totales que muestran las tarjetas del dashboard. */
  record ResumenDashboard(
      long totalProductos, long totalClientes, long totalCategorias, long stockBajo) {}
}
