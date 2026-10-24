package com.petcarestore.servicio;

import com.petcarestore.modelo.Cliente;

public interface ClienteServicio {
  java.util.List<Cliente> listar();

  Cliente guardar(Cliente registro);

  Cliente buscarPorId(Integer id);

  void eliminar(Integer id);
}
