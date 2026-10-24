package com.petcarestore.repositorio;

import com.petcarestore.modelo.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoriaRepositorio extends JpaRepository<Categoria, Integer> {
  boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Integer id);
}
