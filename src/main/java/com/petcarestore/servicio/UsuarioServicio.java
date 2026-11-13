package com.petcarestore.servicio;

import com.petcarestore.modelo.Usuario;
import java.util.List;

public interface UsuarioServicio {
  List<Usuario> listar();

  Usuario guardar(Usuario registro);

  Usuario buscarPorId(Integer id);
}
