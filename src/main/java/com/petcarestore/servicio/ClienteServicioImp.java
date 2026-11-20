package com.petcarestore.servicio;

import com.petcarestore.modelo.*;
import com.petcarestore.repositorio.*;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClienteServicioImp implements ClienteServicio {
  private final ClienteRepositorio repositorio;

  public ClienteServicioImp(ClienteRepositorio repositorio) {
    this.repositorio = repositorio;
  }

  @Override
  public List<Cliente> listar() {
    return repositorio.findAll();
  }

  @Override
  public Cliente buscarPorId(Integer id) {
    return repositorio.findById(id).orElseThrow(RegistroNoEncontradoException::new);
  }

  @Override
  @Transactional
  @PreAuthorize("hasRole('ADMINISTRADOR')")
  public Cliente guardar(Cliente registro) {
    if (registro.getId() != null) buscarPorId(registro.getId());
    Integer id = registro.getId() == null ? -1 : registro.getId();
    if (repositorio.existsByDocumentoAndIdNot(registro.getDocumento(), id))
      throw new ReglaNegocioException("Ya existe un registro con ese documento.");
    if (repositorio.existsByCorreoIgnoreCaseAndIdNot(registro.getCorreo(), id))
      throw new ReglaNegocioException("Ya existe un cliente con ese correo.");
    registro.setCorreo(registro.getCorreo().toLowerCase(java.util.Locale.ROOT));
    return repositorio.save(registro);
  }

  @Override
  @Transactional
  @PreAuthorize("hasRole('ADMINISTRADOR')")
  public void eliminar(Integer id) {
    buscarPorId(id);
    repositorio.deleteById(id);
  }
}
