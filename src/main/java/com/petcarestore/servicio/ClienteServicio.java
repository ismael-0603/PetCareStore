package com.petcarestore.servicio;

import com.petcarestore.modelo.Cliente;
import java.util.List;

public interface ClienteServicio {
  List<Cliente> listar();

  Cliente guardar(Cliente registro);

  Cliente buscarPorId(Integer id);

  void eliminar(Integer id);
}
