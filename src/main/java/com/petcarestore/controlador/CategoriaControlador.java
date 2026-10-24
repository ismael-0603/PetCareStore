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
@RequestMapping("/categorias")
public class CategoriaControlador {
  private final CategoriaServicio servicio;

  public CategoriaControlador(CategoriaServicio servicio) {
    this.servicio = servicio;
  }

  @InitBinder("categoria")
  public void campos(org.springframework.web.bind.WebDataBinder binder) {
    binder.setAllowedFields("id", "nombre", "descripcion", "estado");
  }

  @GetMapping
  public String listar(Model model) {
    model.addAttribute("registros", servicio.listar());
    return "categorias/listar";
  }

  @GetMapping("/nuevo")
  public String nuevo(Model model) {
    model.addAttribute("categoria", new Categoria());
    return "categorias/formulario";
  }

  @GetMapping("/editar/{id}")
  public String editar(@PathVariable Integer id, Model model) {
    model.addAttribute("categoria", servicio.buscarPorId(id));
    return "categorias/formulario";
  }

  @PostMapping("/guardar")
  public String guardar(
      @Valid @ModelAttribute("categoria") Categoria registro,
      BindingResult resultado,
      Model model,
      RedirectAttributes mensajes) {
    if (!resultado.hasErrors()) {
      try {
        servicio.guardar(registro);
        mensajes.addFlashAttribute("exito", "Registro guardado correctamente.");
        return "redirect:/categorias";
      } catch (ReglaNegocioException e) {
        resultado.reject("negocio", e.getMessage());
      } catch (org.springframework.dao.DataIntegrityViolationException e) {
        resultado.reject("duplicado", "Los datos ya existen o tienen referencias asociadas.");
      }
    }
    return "categorias/formulario";
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
    return "redirect:/categorias";
  }
}
