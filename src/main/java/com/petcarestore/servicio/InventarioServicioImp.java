package com.petcarestore.servicio;

import com.petcarestore.modelo.*;
import com.petcarestore.repositorio.*;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InventarioServicioImp implements InventarioServicio {
  private final InventarioRepositorio inventarios;
  private final ProductoRepositorio productos;

  public InventarioServicioImp(InventarioRepositorio inventarios, ProductoRepositorio productos) {
    this.inventarios = inventarios;
    this.productos = productos;
  }

  public List<Inventario> listar() {
    return inventarios.findAll();
  }

  public Inventario buscarPorId(Integer id) {
    return inventarios.findById(id).orElseThrow(RegistroNoEncontradoException::new);
  }

  @Transactional
  @PreAuthorize("hasAnyRole('ADMINISTRADOR','EMPLEADO')")
  public void actualizar(Integer id, Integer cantidad, Integer stockMinimo) {
    if (cantidad == null || cantidad < 0)
      throw new ReglaNegocioException("La cantidad debe ser cero o mayor.");
    Integer productoId =
        inventarios.buscarProductoId(id).orElseThrow(RegistroNoEncontradoException::new);
    Producto producto =
        productos.bloquearPorId(productoId).orElseThrow(RegistroNoEncontradoException::new);
    Inventario inventario =
        inventarios
            .bloquearPorProductoId(producto.getId())
            .orElseThrow(RegistroNoEncontradoException::new);
    boolean administrador =
        SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_ADMINISTRADOR"));
    if (stockMinimo != null) {
      if (!administrador)
        throw new org.springframework.security.access.AccessDeniedException(
            "Solo el administrador puede modificar el minimo.");
      if (stockMinimo < 0)
        throw new ReglaNegocioException("El stock minimo no puede ser negativo.");
      inventario.setStockMinimo(stockMinimo);
    }
    producto.setStock(cantidad);
    inventario.setCantidad(cantidad);
    inventario.setFechaActualizacion(LocalDateTime.now());
    productos.save(producto);
    inventarios.save(inventario);
  }
}
