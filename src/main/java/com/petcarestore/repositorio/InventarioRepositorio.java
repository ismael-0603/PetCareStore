package com.petcarestore.repositorio;

import com.petcarestore.modelo.Inventario;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InventarioRepositorio extends JpaRepository<Inventario, Integer> {
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select i from Inventario i where i.producto.id=:productoId")
  Optional<Inventario> bloquearPorProductoId(@Param("productoId") Integer productoId);

  Optional<Inventario> findByProductoId(Integer productoId);

  @Query("select i.producto.id from Inventario i where i.id=:id")
  Optional<Integer> buscarProductoId(@Param("id") Integer id);

  void deleteByProductoId(Integer productoId);

  @Query("select count(i) from Inventario i where i.cantidad < i.stockMinimo")
  long contarStockBajo();

  @Query(
      "select i from Inventario i join fetch i.producto p join fetch p.categoria"
          + " where i.cantidad < i.stockMinimo order by i.cantidad asc, i.id asc")
  List<Inventario> listarStockBajo();
}
