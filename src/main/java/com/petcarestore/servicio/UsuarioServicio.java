package com.petcarestore.servicio;

import com.petcarestore.modelo.Usuario;

public interface UsuarioServicio {
  java.util.List<Usuario> listar();

  Usuario guardar(Usuario registro);

  Usuario buscarPorId(Integer id);
}
