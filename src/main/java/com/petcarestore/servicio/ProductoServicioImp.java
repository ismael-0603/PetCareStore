package com.petcarestore.servicio;

import com.petcarestore.modelo.*;
import com.petcarestore.repositorio.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductoServicioImp implements ProductoServicio {
  private final ProductoRepositorio productos;
  private final CategoriaRepositorio categorias;
  private final InventarioRepositorio inventarios;

  public ProductoServicioImp(
      ProductoRepositorio productos,
      CategoriaRepositorio categorias,
      InventarioRepositorio inventarios) {
    this.productos = productos;
    this.categorias = categorias;
    this.inventarios = inventarios;
  }

  public List<Producto> listar() {
    return productos.findAll();
  }

  public Producto buscarPorId(Integer id) {
    return productos.findById(id).orElseThrow(RegistroNoEncontradoException::new);
  }

  public List<Producto> filtrar(
      String nombre, Integer categoria, Boolean disponible, BigDecimal minimo, BigDecimal maximo) {
    if ((minimo != null && minimo.signum() < 0)
        || (maximo != null && maximo.signum() < 0)
        || (minimo != null && maximo != null && minimo.compareTo(maximo) > 0))
      throw new ReglaNegocioException("El rango de precios no es valido.");
    return productos.filtrar(nombre, categoria, disponible, minimo, maximo);
  }

  @Transactional
  @PreAuthorize("hasRole('ADMINISTRADOR')")
  public Producto guardar(Producto datos) {
    if (datos.getStock() == null
        || datos.getStock() < 0
        || datos.getPrecio() == null
        || datos.getPrecio().signum() < 0)
      throw new ReglaNegocioException("Precio y stock deben ser cero o mayores.");
    if (datos.getCategoria() == null || datos.getCategoria().getId() == null)
      throw new ReglaNegocioException("Seleccione una categoria.");
    Categoria categoria =
        categorias
            .findById(datos.getCategoria().getId())
            .orElseThrow(RegistroNoEncontradoException::new);
    Producto producto =
        datos.getId() == null
            ? new Producto()
            : productos
                .bloquearPorId(datos.getId())
                .orElseThrow(RegistroNoEncontradoException::new);
    producto.setNombre(datos.getNombre());
    producto.setDescripcion(datos.getDescripcion());
    producto.setCategoria(categoria);
    producto.setPrecio(datos.getPrecio());
    producto.setEstado(datos.isEstado());
    producto.setStock(datos.getStock());
    if (datos.getImagen() != null) producto.setImagen(datos.getImagen());
    producto = productos.saveAndFlush(producto);
    Inventario inventario =
        inventarios.bloquearPorProductoId(producto.getId()).orElse(new Inventario());
    inventario.setProducto(producto);
    inventario.setCantidad(producto.getStock());
    inventario.setFechaActualizacion(LocalDateTime.now());
    inventarios.save(inventario);
    return producto;
  }

  @Transactional
  @PreAuthorize("hasRole('ADMINISTRADOR')")
  public void eliminar(Integer id) {
    Producto producto = productos.bloquearPorId(id).orElseThrow(RegistroNoEncontradoException::new);
    inventarios.deleteByProductoId(id);
    inventarios.flush();
    productos.delete(producto);
  }
}
