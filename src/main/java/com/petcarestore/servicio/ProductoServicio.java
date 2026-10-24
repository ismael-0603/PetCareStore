package com.petcarestore.servicio;

import com.petcarestore.modelo.Producto;

public interface ProductoServicio {
  java.util.List<Producto> listar();

  Producto guardar(Producto registro);

  Producto buscarPorId(Integer id);

  void eliminar(Integer id);

  java.util.List<Producto> filtrar(
      String nombre,
      Integer categoria,
      Boolean disponible,
      java.math.BigDecimal minimo,
      java.math.BigDecimal maximo);
}
