package com.petcarestore.repositorio;

import com.petcarestore.modelo.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductoRepositorio extends JpaRepository<Producto, Integer> {
  boolean existsByCategoriaId(Integer categoriaId);

  @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
  @org.springframework.data.jpa.repository.Query("select p from Producto p where p.id=:id")
  java.util.Optional<Producto> bloquearPorId(
      @org.springframework.data.repository.query.Param("id") Integer id);

  @org.springframework.data.jpa.repository.Query(
      "select p from Producto p where (:nombre is null or lower(p.nombre) like"
          + " lower(concat('%',:nombre,'%'))) and (:categoria is null or p.categoria.id=:categoria)"
          + " and (:disponible is null or (:disponible=true and p.stock>0) or (:disponible=false"
          + " and p.stock=0)) and (:minimo is null or p.precio>=:minimo) and (:maximo is null or"
          + " p.precio<=:maximo) order by p.id")
  java.util.List<Producto> filtrar(
      @org.springframework.data.repository.query.Param("nombre") String nombre,
      @org.springframework.data.repository.query.Param("categoria") Integer categoria,
      @org.springframework.data.repository.query.Param("disponible") Boolean disponible,
      @org.springframework.data.repository.query.Param("minimo") java.math.BigDecimal minimo,
      @org.springframework.data.repository.query.Param("maximo") java.math.BigDecimal maximo);
}
