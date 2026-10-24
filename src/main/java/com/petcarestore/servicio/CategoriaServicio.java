package com.petcarestore.servicio;

import com.petcarestore.modelo.Categoria;

public interface CategoriaServicio {
  java.util.List<Categoria> listar();

  Categoria guardar(Categoria registro);

  Categoria buscarPorId(Integer id);

  void eliminar(Integer id);
}
