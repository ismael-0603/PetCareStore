package com.petcarestore.repositorio;

import com.petcarestore.modelo.Usuario;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface UsuarioRepositorio extends JpaRepository<Usuario, Integer> {
  Optional<Usuario> findByCorreoIgnoreCase(String correo);

  boolean existsByCorreoIgnoreCaseAndIdNot(String correo, Integer id);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select u from Usuario u order by u.id")
  List<Usuario> bloquearUsuarios();
}
