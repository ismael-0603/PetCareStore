package com.petcarestore.repositorio;

import com.petcarestore.modelo.Inventario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventarioRepositorio extends JpaRepository<Inventario, Integer> {
  @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
  @org.springframework.data.jpa.repository.Query(
      "select i from Inventario i where i.producto.id=:productoId")
  java.util.Optional<Inventario> bloquearPorProductoId(
      @org.springframework.data.repository.query.Param("productoId") Integer productoId);

  java.util.Optional<Inventario> findByProductoId(Integer productoId);

  @org.springframework.data.jpa.repository.Query(
      "select i.producto.id from Inventario i where i.id=:id")
  java.util.Optional<Integer> buscarProductoId(
      @org.springframework.data.repository.query.Param("id") Integer id);

  void deleteByProductoId(Integer productoId);

  @org.springframework.data.jpa.repository.Query(
      "select count(i) from Inventario i where i.cantidad < i.stockMinimo")
  long contarStockBajo();
}
