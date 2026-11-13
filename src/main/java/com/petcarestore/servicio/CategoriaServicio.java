package com.petcarestore.servicio;

import com.petcarestore.modelo.Categoria;
import java.util.List;

public interface CategoriaServicio {
  List<Categoria> listar();

  Categoria guardar(Categoria registro);

  Categoria buscarPorId(Integer id);

  void eliminar(Integer id);
}
