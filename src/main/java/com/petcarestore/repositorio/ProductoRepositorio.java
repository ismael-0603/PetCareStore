package com.petcarestore.repositorio;

import com.petcarestore.modelo.Producto;
import jakarta.persistence.LockModeType;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductoRepositorio extends JpaRepository<Producto, Integer> {
  // Condiciones de filtro compartidas por la consulta de lista y la de paginación.
  String FILTRO =
      " where (:nombre is null or lower(p.nombre) like lower(concat('%',:nombre,'%')))"
          + " and (:categoria is null or p.categoria.id=:categoria)"
          + " and (:disponible is null or (:disponible=true and p.stock>0)"
          + " or (:disponible=false and p.stock=0))"
          + " and (:minimo is null or p.precio>=:minimo)"
          + " and (:maximo is null or p.precio<=:maximo)";

  boolean existsByCategoriaId(Integer categoriaId);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select p from Producto p where p.id=:id")
  Optional<Producto> bloquearPorId(@Param("id") Integer id);

  @Query("select p from Producto p" + FILTRO + " order by p.id")
  List<Producto> filtrar(
      @Param("nombre") String nombre,
      @Param("categoria") Integer categoria,
      @Param("disponible") Boolean disponible,
      @Param("minimo") BigDecimal minimo,
      @Param("maximo") BigDecimal maximo);

  // Misma consulta, paginada (guía "Registro con modal, paginación y búsqueda").
  @Query(
      value = "select p from Producto p" + FILTRO + " order by p.id",
      countQuery = "select count(p) from Producto p" + FILTRO)
  Page<Producto> filtrarPagina(
      @Param("nombre") String nombre,
      @Param("categoria") Integer categoria,
      @Param("disponible") Boolean disponible,
      @Param("minimo") BigDecimal minimo,
      @Param("maximo") BigDecimal maximo,
      Pageable pageable);

  long countByEstadoTrue();

  @Query(
      "select c.nombre, count(p.id) from Categoria c left join Producto p on p.categoria.id = c.id"
          + " group by c.id, c.nombre order by count(p.id) desc, c.nombre asc")
  List<Object[]> contarPorCategoria();
}
