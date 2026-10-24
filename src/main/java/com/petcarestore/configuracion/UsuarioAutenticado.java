package com.petcarestore.configuracion;

import com.petcarestore.modelo.Usuario;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;

public class UsuarioAutenticado extends User {
  private final Integer id;

  public UsuarioAutenticado(Usuario usuario) {
    super(
        usuario.getCorreo(),
        usuario.getPassword(),
        usuario.isEstado(),
        true,
        true,
        true,
        AuthorityUtils.createAuthorityList("ROLE_" + usuario.getRol().name()));
    this.id = usuario.getId();
  }

  public Integer getId() {
    return id;
  }
}
