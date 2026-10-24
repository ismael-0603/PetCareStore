package com.petcarestore.servicio;

public class RegistroNoEncontradoException extends RuntimeException {
  public RegistroNoEncontradoException() {
    super("El registro solicitado no existe.");
  }
}
