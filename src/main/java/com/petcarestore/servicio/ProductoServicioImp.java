package com.petcarestore.servicio;

import com.petcarestore.modelo.*;
import com.petcarestore.repositorio.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductoServicioImp implements ProductoServicio {
  private final ProductoRepositorio productos;
  private final CategoriaRepositorio categorias;
  private final InventarioRepositorio inventarios;
  private final ReporteProductos reportes;

  public ProductoServicioImp(
      ProductoRepositorio productos,
      CategoriaRepositorio categorias,
      InventarioRepositorio inventarios,
      ReporteProductos reportes) {
    this.productos = productos;
    this.categorias = categorias;
    this.inventarios = inventarios;
    this.reportes = reportes;
  }

  @Override
  public byte[] exportarExcel(
      String nombre, Integer categoria, Boolean disponible, BigDecimal minimo, BigDecimal maximo) {
    return reportes.excel(filtrar(nombre, categoria, disponible, minimo, maximo));
  }

  @Override
  public byte[] exportarPdf(
      String nombre, Integer categoria, Boolean disponible, BigDecimal minimo, BigDecimal maximo) {
    return reportes.pdf(filtrar(nombre, categoria, disponible, minimo, maximo));
  }

  @Override
  public Producto buscarPorId(Integer id) {
    return productos.findById(id).orElseThrow(RegistroNoEncontradoException::new);
  }

  @Override
  public List<Producto> filtrar(
      String nombre, Integer categoria, Boolean disponible, BigDecimal minimo, BigDecimal maximo) {
    validarRango(minimo, maximo);
    return productos.filtrar(nombre, categoria, disponible, minimo, maximo);
  }

  @Override
  public Page<Producto> filtrarPagina(
      String nombre,
      Integer categoria,
      Boolean disponible,
      BigDecimal minimo,
      BigDecimal maximo,
      int pagina,
      int tamano) {
    validarRango(minimo, maximo);
    // Se acota la página y el tamaño para que un parámetro manipulado no rompa la consulta.
    Pageable pageable = PageRequest.of(Math.max(pagina, 0), Math.min(Math.max(tamano, 1), 50));
    return productos.filtrarPagina(nombre, categoria, disponible, minimo, maximo, pageable);
  }

  private void validarRango(BigDecimal minimo, BigDecimal maximo) {
    if ((minimo != null && minimo.signum() < 0)
        || (maximo != null && maximo.signum() < 0)
        || (minimo != null && maximo != null && minimo.compareTo(maximo) > 0))
      throw new ReglaNegocioException("El rango de precios no es valido.");
  }

  @Override
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

  @Override
  @Transactional
  @PreAuthorize("hasRole('ADMINISTRADOR')")
  public void eliminar(Integer id) {
    Producto producto = productos.bloquearPorId(id).orElseThrow(RegistroNoEncontradoException::new);
    inventarios.deleteByProductoId(id);
    inventarios.flush();
    productos.delete(producto);
  }
}
