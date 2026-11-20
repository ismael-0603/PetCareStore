package com.petcarestore.controlador;

import com.petcarestore.modelo.*;
import com.petcarestore.servicio.*;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/productos")
public class ProductoControlador {
  private static final int TAMANO = 5; // productos por página
  private final ProductoServicio servicio;
  private final CategoriaServicio categorias;

  public ProductoControlador(
      ProductoServicio servicio, CategoriaServicio categorias) {
    this.servicio = servicio;
    this.categorias = categorias;
  }

  @InitBinder("producto")
  public void campos(WebDataBinder binder) {
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
      @RequestParam(defaultValue = "0") int pagina,
      Model model) {
    model.addAttribute("categorias", categorias.listar());
    model.addAttribute("nombre", nombre);
    model.addAttribute("categoriaFiltro", categoria);
    model.addAttribute("disponible", disponible);
    model.addAttribute("minimo", minimo);
    model.addAttribute("maximo", maximo);
    // Objeto vacío para el formulario del modal de registro.
    if (!model.containsAttribute("producto")) model.addAttribute("producto", new Producto());
    try {
      Page<Producto> resultado =
          servicio.filtrarPagina(nombre, categoria, disponible, minimo, maximo, pagina, TAMANO);
      model.addAttribute("registros", resultado.getContent());
      model.addAttribute("paginaActual", resultado.getNumber());
      model.addAttribute("totalPaginas", resultado.getTotalPages());
      model.addAttribute("totalRegistros", resultado.getTotalElements());
    } catch (ReglaNegocioException e) {
      model.addAttribute("error", e.getMessage());
      model.addAttribute("registros", List.of());
      model.addAttribute("paginaActual", 0);
      model.addAttribute("totalPaginas", 0);
      model.addAttribute("totalRegistros", 0L);
    }
    return "productos/listar";
  }

  // Descarga el reporte Excel con los filtros activos de la lista.
  @GetMapping("/exportar/excel")
  public Object exportarExcel(
      @RequestParam(required = false) String nombre,
      @RequestParam(required = false) Integer categoria,
      @RequestParam(required = false) Boolean disponible,
      @RequestParam(required = false) BigDecimal minimo,
      @RequestParam(required = false) BigDecimal maximo,
      RedirectAttributes mensajes) {
    try {
      return descarga(
          servicio.exportarExcel(nombre, categoria, disponible, minimo, maximo),
          "xlsx",
          MediaType.parseMediaType(
              "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
    } catch (ReglaNegocioException e) {
      mensajes.addFlashAttribute("error", e.getMessage());
      return "redirect:/productos";
    }
  }

  // Descarga el reporte PDF con los filtros activos de la lista.
  @GetMapping("/exportar/pdf")
  public Object exportarPdf(
      @RequestParam(required = false) String nombre,
      @RequestParam(required = false) Integer categoria,
      @RequestParam(required = false) Boolean disponible,
      @RequestParam(required = false) BigDecimal minimo,
      @RequestParam(required = false) BigDecimal maximo,
      RedirectAttributes mensajes) {
    try {
      return descarga(
          servicio.exportarPdf(nombre, categoria, disponible, minimo, maximo),
          "pdf",
          MediaType.APPLICATION_PDF);
    } catch (ReglaNegocioException e) {
      mensajes.addFlashAttribute("error", e.getMessage());
      return "redirect:/productos";
    }
  }

  private ResponseEntity<byte[]> descarga(byte[] archivo, String extension, MediaType tipo) {
    String nombreArchivo = "productos_" + LocalDate.now() + "." + extension;
    return ResponseEntity.ok()
        .header(
            HttpHeaders.CONTENT_DISPOSITION,
            ContentDisposition.attachment().filename(nombreArchivo).build().toString())
        .contentType(tipo)
        .contentLength(archivo.length)
        .body(archivo);
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
        servicio.guardarConImagen(producto, archivo);
        mensajes.addFlashAttribute("exito", "Producto guardado e inventario sincronizado.");
        return "redirect:/productos";
      } catch (ReglaNegocioException e) {
        resultado.reject("negocio", e.getMessage());
      } catch (DataIntegrityViolationException e) {
        resultado.reject("datos", "No se pudo guardar. Revise la categoría seleccionada.");
      }
    }
    model.addAttribute("categorias", categorias.listar());
    return "productos/formulario";
  }

  @PostMapping("/eliminar/{id}")
  public String eliminar(
      @PathVariable Integer id, @RequestParam java.util.Map<String, String> parametros,
      RedirectAttributes mensajes) {
    servicio.eliminar(id);
    for (String filtro : List.of("pagina", "nombre", "categoria", "disponible", "minimo", "maximo")) {
      String valor = parametros.get(filtro);
      if (valor != null && !valor.isBlank()) mensajes.addAttribute(filtro, valor);
    }
    mensajes.addFlashAttribute("exito", "Producto e inventario eliminados.");
    return "redirect:/productos";
  }
}
