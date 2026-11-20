package com.petcarestore.servicio;

import com.petcarestore.modelo.Producto;
import java.math.BigDecimal;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import org.springframework.data.domain.Page;

public interface ProductoServicio {
  Producto guardar(Producto registro);

  Producto guardarConImagen(Producto registro, MultipartFile archivo);

  Producto buscarPorId(Integer id);

  void eliminar(Integer id);

  List<Producto> filtrar(
      String nombre, Integer categoria, Boolean disponible, BigDecimal minimo, BigDecimal maximo);

  Page<Producto> filtrarPagina(
      String nombre,
      Integer categoria,
      Boolean disponible,
      BigDecimal minimo,
      BigDecimal maximo,
      int pagina,
      int tamano);

  // Reportes: exportan los productos que cumplen los filtros (sin filtros, todos).
  byte[] exportarExcel(
      String nombre, Integer categoria, Boolean disponible, BigDecimal minimo, BigDecimal maximo);

  byte[] exportarPdf(
      String nombre, Integer categoria, Boolean disponible, BigDecimal minimo, BigDecimal maximo);
}
