package com.petcarestore.repositorio;

import com.petcarestore.modelo.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepositorio extends JpaRepository<Usuario, Integer> {
  java.util.Optional<Usuario> findByCorreoIgnoreCase(String correo);

  boolean existsByCorreoIgnoreCaseAndIdNot(String correo, Integer id);

  @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
  @org.springframework.data.jpa.repository.Query("select u from Usuario u order by u.id")
  java.util.List<Usuario> bloquearUsuarios();
}
