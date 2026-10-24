package com.petcarestore.controlador;

import com.petcarestore.modelo.*;
import com.petcarestore.servicio.*;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/productos")
public class ProductoControlador {
  private final ProductoServicio servicio;
  private final CategoriaServicio categorias;
  private final ImagenServicio imagenes;

  public ProductoControlador(
      ProductoServicio servicio, CategoriaServicio categorias, ImagenServicio imagenes) {
    this.servicio = servicio;
    this.categorias = categorias;
    this.imagenes = imagenes;
  }

  @InitBinder("producto")
  public void campos(org.springframework.web.bind.WebDataBinder binder) {
    binder.setAllowedFields(
        "id", "nombre", "descripcion", "categoria.id", "precio", "stock", "estado");
  }

  @GetMapping
  public String listar(
      @RequestParam(required = false) String nombre,
      @RequestParam(required = false) Integer categoria,
      @RequestParam(required = false) Boolean disponible,
      @RequestParam(required = false) BigDecimal minimo,
      @RequestParam(required = false) BigDecimal maximo,
      Model model) {
    model.addAttribute("categorias", categorias.listar());
    model.addAttribute("nombre", nombre);
    model.addAttribute("categoriaFiltro", categoria);
    model.addAttribute("disponible", disponible);
    model.addAttribute("minimo", minimo);
    model.addAttribute("maximo", maximo);
    try {
      model.addAttribute(
          "registros", servicio.filtrar(nombre, categoria, disponible, minimo, maximo));
    } catch (ReglaNegocioException e) {
      model.addAttribute("error", e.getMessage());
      model.addAttribute("registros", java.util.List.of());
    }
    return "productos/listar";
  }

  @GetMapping("/nuevo")
  public String nuevo(Model model) {
    model.addAttribute("producto", new Producto());
    model.addAttribute("categorias", categorias.listar());
    return "productos/formulario";
  }

  @GetMapping("/editar/{id}")
  public String editar(@PathVariable Integer id, Model model) {
    model.addAttribute("producto", servicio.buscarPorId(id));
    model.addAttribute("categorias", categorias.listar());
    return "productos/formulario";
  }

  @PostMapping("/guardar")
  public String guardar(
      @Valid @ModelAttribute("producto") Producto producto,
      BindingResult resultado,
      @RequestParam(required = false) MultipartFile archivo,
      Model model,
      RedirectAttributes mensajes) {
    if (!resultado.hasErrors()) {
      try {
        producto.setImagen(imagenes.guardar(archivo));
        servicio.guardar(producto);
        mensajes.addFlashAttribute("exito", "Producto guardado e inventario sincronizado.");
        return "redirect:/productos";
      } catch (ReglaNegocioException e) {
        resultado.reject("negocio", e.getMessage());
      } catch (org.springframework.dao.DataIntegrityViolationException e) {
        resultado.reject("datos", "No se pudo guardar. Revise la categoria seleccionada.");
      }
    }
    model.addAttribute("categorias", categorias.listar());
    return "productos/formulario";
  }

  @PostMapping("/eliminar/{id}")
  public String eliminar(@PathVariable Integer id, RedirectAttributes mensajes) {
    servicio.eliminar(id);
    mensajes.addFlashAttribute("exito", "Producto e inventario eliminados.");
    return "redirect:/productos";
  }
}
