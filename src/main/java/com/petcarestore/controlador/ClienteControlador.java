package com.petcarestore.controlador;

import com.petcarestore.modelo.*;
import com.petcarestore.servicio.*;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/clientes")
public class ClienteControlador {
  private final ClienteServicio servicio;

  public ClienteControlador(ClienteServicio servicio) {
    this.servicio = servicio;
  }

  @InitBinder("cliente")
  public void campos(org.springframework.web.bind.WebDataBinder binder) {
    binder.setAllowedFields(
        "id", "nombre", "apellido", "documento", "telefono", "correo", "direccion");
  }

  @GetMapping
  public String listar(Model model) {
    model.addAttribute("registros", servicio.listar());
    return "clientes/listar";
  }

  @GetMapping("/nuevo")
  public String nuevo(Model model) {
    model.addAttribute("cliente", new Cliente());
    return "clientes/formulario";
  }

  @GetMapping("/editar/{id}")
  public String editar(@PathVariable Integer id, Model model) {
    model.addAttribute("cliente", servicio.buscarPorId(id));
    return "clientes/formulario";
  }

  @PostMapping("/guardar")
  public String guardar(
      @Valid @ModelAttribute("cliente") Cliente registro,
      BindingResult resultado,
      Model model,
      RedirectAttributes mensajes) {
    if (!resultado.hasErrors()) {
      try {
        servicio.guardar(registro);
        mensajes.addFlashAttribute("exito", "Registro guardado correctamente.");
        return "redirect:/clientes";
      } catch (ReglaNegocioException e) {
        resultado.reject("negocio", e.getMessage());
      } catch (org.springframework.dao.DataIntegrityViolationException e) {
        resultado.reject("duplicado", "Los datos ya existen o tienen referencias asociadas.");
      }
    }
    return "clientes/formulario";
  }

  @PostMapping("/eliminar/{id}")
  public String eliminar(@PathVariable Integer id, RedirectAttributes mensajes) {
    try {
      servicio.eliminar(id);
      mensajes.addFlashAttribute("exito", "Registro eliminado correctamente.");
    } catch (ReglaNegocioException e) {
      mensajes.addFlashAttribute("error", e.getMessage());
    } catch (org.springframework.dao.DataIntegrityViolationException e) {
      mensajes.addFlashAttribute(
          "error", "El registro tiene referencias asociadas y no se puede eliminar.");
    }
    return "redirect:/clientes";
  }
}
