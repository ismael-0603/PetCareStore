package com.petcarestore.servicio;

import com.petcarestore.modelo.*;
import com.petcarestore.repositorio.*;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CategoriaServicioImp implements CategoriaServicio {
  private final CategoriaRepositorio repositorio;
  private final ProductoRepositorio productos;

  public CategoriaServicioImp(CategoriaRepositorio repositorio, ProductoRepositorio productos) {
    this.repositorio = repositorio;
    this.productos = productos;
  }

  public List<Categoria> listar() {
    return repositorio.findAll();
  }

  public Categoria buscarPorId(Integer id) {
    return repositorio.findById(id).orElseThrow(RegistroNoEncontradoException::new);
  }

  @Transactional
  @PreAuthorize("hasRole('ADMINISTRADOR')")
  public Categoria guardar(Categoria registro) {
    if (registro.getId() != null) buscarPorId(registro.getId());
    Integer id = registro.getId() == null ? -1 : registro.getId();
    if (repositorio.existsByNombreIgnoreCaseAndIdNot(registro.getNombre(), id))
      throw new ReglaNegocioException("Ya existe un registro con ese nombre.");
    return repositorio.save(registro);
  }

  @Transactional
  @PreAuthorize("hasRole('ADMINISTRADOR')")
  public void eliminar(Integer id) {
    buscarPorId(id);
    if (productos.existsByCategoriaId(id))
      throw new ReglaNegocioException(
          "La categoria tiene productos asociados. Reasignelos antes de eliminarla.");
    repositorio.deleteById(id);
  }
}
