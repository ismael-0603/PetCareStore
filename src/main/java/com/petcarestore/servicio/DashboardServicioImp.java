package com.petcarestore.servicio;

import com.petcarestore.repositorio.CategoriaRepositorio;
import com.petcarestore.repositorio.ClienteRepositorio;
import com.petcarestore.repositorio.InventarioRepositorio;
import com.petcarestore.repositorio.ProductoRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DashboardServicioImp implements DashboardServicio {
  private final ProductoRepositorio productos;
  private final ClienteRepositorio clientes;
  private final CategoriaRepositorio categorias;
  private final InventarioRepositorio inventarios;

  public DashboardServicioImp(
      ProductoRepositorio productos,
      ClienteRepositorio clientes,
      CategoriaRepositorio categorias,
      InventarioRepositorio inventarios) {
    this.productos = productos;
    this.clientes = clientes;
    this.categorias = categorias;
    this.inventarios = inventarios;
  }

  @Override
  @Transactional(readOnly = true)
  public ResumenDashboard resumen() {
    return new ResumenDashboard(
        productos.count(), clientes.count(), categorias.count(), inventarios.contarStockBajo());
  }
}
