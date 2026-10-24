package com.petcarestore.controlador;

import com.petcarestore.servicio.*;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@ControllerAdvice
public class ErroresControlador {
  @ExceptionHandler(RegistroNoEncontradoException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public String inexistente(RegistroNoEncontradoException e, Model model) {
    model.addAttribute("mensaje", e.getMessage());
    return "error/404";
  }

  @ExceptionHandler({
    org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class,
    org.springframework.web.bind.MissingServletRequestParameterException.class
  })
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public String datoInvalido(Exception e, Model model) {
    model.addAttribute(
        "mensaje",
        "La solicitud contiene datos invalidos. Revise los campos e intente nuevamente.");
    return "error/400";
  }

  @ExceptionHandler(org.springframework.web.multipart.MaxUploadSizeExceededException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public String imagenGrande(Exception e, Model model) {
    model.addAttribute(
        "mensaje", "La imagen supera el limite de 2 MB. Regrese al formulario y seleccione otra.");
    return "error/400";
  }
}
