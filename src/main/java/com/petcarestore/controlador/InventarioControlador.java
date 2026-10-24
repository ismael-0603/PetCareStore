package com.petcarestore.controlador;

import com.petcarestore.modelo.*;
import com.petcarestore.servicio.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/inventario")
public class InventarioControlador {
  private final InventarioServicio servicio;

  public InventarioControlador(InventarioServicio servicio) {
    this.servicio = servicio;
  }

  @GetMapping
  public String listar(Model model) {
    model.addAttribute("registros", servicio.listar());
    return "inventario/listar";
  }

  @GetMapping("/editar/{id}")
  public String editar(@PathVariable Integer id, Model model) {
    model.addAttribute("inventario", servicio.buscarPorId(id));
    return "inventario/formulario";
  }

  @PostMapping("/guardar")
  public String guardar(
      @RequestParam Integer id,
      @RequestParam Integer cantidad,
      @RequestParam(required = false) Integer stockMinimo,
      Model model,
      RedirectAttributes mensajes) {
    try {
      servicio.actualizar(id, cantidad, stockMinimo);
      mensajes.addFlashAttribute("exito", "Existencias actualizadas correctamente.");
      return "redirect:/inventario";
    } catch (ReglaNegocioException e) {
      model.addAttribute("error", e.getMessage());
      model.addAttribute("inventario", servicio.buscarPorId(id));
      return "inventario/formulario";
    }
  }
}
