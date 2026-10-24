package com.petcarestore.servicio;

import com.petcarestore.modelo.*;
import com.petcarestore.repositorio.UsuarioRepositorio;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class UsuarioServicioImp implements UsuarioServicio {
  private final UsuarioRepositorio repositorio;
  private final PasswordEncoder encoder;

  public UsuarioServicioImp(UsuarioRepositorio repositorio, PasswordEncoder encoder) {
    this.repositorio = repositorio;
    this.encoder = encoder;
  }

  public List<Usuario> listar() {
    return repositorio.findAll();
  }

  public Usuario buscarPorId(Integer id) {
    return repositorio.findById(id).orElseThrow(RegistroNoEncontradoException::new);
  }

  @Transactional
  public Usuario guardar(Usuario datos) {
    List<Usuario> usuarios = repositorio.bloquearUsuarios();
    Usuario usuario =
        datos.getId() == null
            ? new Usuario()
            : usuarios.stream()
                .filter(u -> u.getId().equals(datos.getId()))
                .findFirst()
                .orElseThrow(RegistroNoEncontradoException::new);
    String correo = datos.getCorreo().trim().toLowerCase(java.util.Locale.ROOT);
    if (repositorio.existsByCorreoIgnoreCaseAndIdNot(
        correo, datos.getId() == null ? -1 : datos.getId()))
      throw new ReglaNegocioException("Ya existe un usuario con ese correo.");
    if (usuario.getId() != null
        && usuario.isEstado()
        && usuario.getRol() == Rol.ADMINISTRADOR
        && (!datos.isEstado() || datos.getRol() != Rol.ADMINISTRADOR)
        && usuarios.stream().filter(u -> u.isEstado() && u.getRol() == Rol.ADMINISTRADOR).count()
            <= 1)
      throw new ReglaNegocioException("Debe conservar al menos un administrador activo.");
    String nueva = datos.getNuevaPassword();
    if (usuario.getId() == null && (nueva == null || nueva.isBlank()))
      throw new ReglaNegocioException("La contrasena es obligatoria para usuarios nuevos.");
    if (nueva != null && !nueva.isBlank()) {
      if (nueva.length() < 8 || nueva.getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72)
        throw new ReglaNegocioException(
            "La contrasena debe tener al menos 8 caracteres y como maximo 72 bytes.");
      usuario.setPassword(encoder.encode(nueva));
    }
    usuario.setNombre(datos.getNombre());
    usuario.setCorreo(correo);
    usuario.setRol(datos.getRol());
    usuario.setEstado(datos.isEstado());
    return repositorio.save(usuario);
  }
}
