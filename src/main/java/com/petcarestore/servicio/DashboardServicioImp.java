package com.petcarestore.servicio;

import com.petcarestore.modelo.Inventario;
import com.petcarestore.repositorio.CategoriaRepositorio;
import com.petcarestore.repositorio.ClienteRepositorio;
import com.petcarestore.repositorio.InventarioRepositorio;
import com.petcarestore.repositorio.ProductoRepositorio;
import java.util.List;
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

  @Override
  @Transactional(readOnly = true)
  public long productosActivos() {
    return productos.countByEstadoTrue();
  }

  @Override
  @Transactional(readOnly = true)
  public List<Inventario> alertasStockBajo() {
    return inventarios.listarStockBajo().stream().limit(5).toList();
  }

  @Override
  @Transactional(readOnly = true)
  public List<ConteoCategoria> distribucionCategorias() {
    List<Object[]> filas = productos.contarPorCategoria();
    long maximo =
        filas.stream()
            .mapToLong(f -> ((Number) f[1]).longValue())
            .max()
            .orElse(0L);
    return filas.stream()
        .map(
            f -> {
              String nombre = String.valueOf(f[0]);
              long cantidad = ((Number) f[1]).longValue();
              int porcentaje =
                  maximo == 0
                      ? 0
                      : (cantidad == 0 ? 6 : (int) Math.max(12, Math.round((cantidad * 100.0) / maximo)));
              return new ConteoCategoria(nombre, cantidad, porcentaje);
            })
        .toList();
  }
}
