package com.petcarestore.servicio;

import com.petcarestore.modelo.Inventario;
import java.util.List;

public interface InventarioServicio {
  List<Inventario> listar();

  Inventario buscarPorId(Integer id);

  void actualizar(Integer id, Integer cantidad, Integer stockMinimo);
}
