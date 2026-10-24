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
@RequestMapping("/usuarios")
public class UsuarioControlador {
  private final UsuarioServicio servicio;

  public UsuarioControlador(UsuarioServicio servicio) {
    this.servicio = servicio;
  }

  @InitBinder("usuario")
  public void campos(org.springframework.web.bind.WebDataBinder binder) {
    binder.setAllowedFields("id", "nombre", "correo", "nuevaPassword", "rol", "estado");
  }

  @GetMapping
  public String listar(Model model) {
    model.addAttribute("registros", servicio.listar());
    return "usuarios/listar";
  }

  @GetMapping("/nuevo")
  public String nuevo(Model model) {
    model.addAttribute("usuario", new Usuario());
    model.addAttribute("roles", Rol.values());
    return "usuarios/formulario";
  }

  @GetMapping("/editar/{id}")
  public String editar(@PathVariable Integer id, Model model) {
    model.addAttribute("usuario", servicio.buscarPorId(id));
    model.addAttribute("roles", Rol.values());
    return "usuarios/formulario";
  }

  @PostMapping("/guardar")
  public String guardar(
      @Valid @ModelAttribute("usuario") Usuario registro,
      BindingResult resultado,
      Model model,
      RedirectAttributes mensajes) {
    if (!resultado.hasErrors()) {
      try {
        servicio.guardar(registro);
        mensajes.addFlashAttribute("exito", "Registro guardado correctamente.");
        return "redirect:/usuarios";
      } catch (ReglaNegocioException e) {
        resultado.reject("negocio", e.getMessage());
      } catch (org.springframework.dao.DataIntegrityViolationException e) {
        resultado.reject("duplicado", "Los datos ya existen o tienen referencias asociadas.");
      }
    }
    model.addAttribute("roles", Rol.values());
    return "usuarios/formulario";
  }
}
