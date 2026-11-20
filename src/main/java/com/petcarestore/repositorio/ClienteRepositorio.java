package com.petcarestore.repositorio;

import com.petcarestore.modelo.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClienteRepositorio extends JpaRepository<Cliente, Integer> {
  boolean existsByCorreoIgnoreCaseAndIdNot(String correo, Integer id);

  boolean existsByDocumentoAndIdNot(String documento, Integer id);
}
