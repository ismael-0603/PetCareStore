package com.petcarestore.servicio;

import com.petcarestore.modelo.Producto;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

/**
 * Genera los reportes de productos en Excel (Apache POI) y PDF (Apache PDFBox), siguiendo el
 * manual de exportación de la Semana 12. Recibe la lista ya filtrada por {@link ProductoServicio}.
 */
@Component
public class ReporteProductos {
  private static final String[] COLUMNAS = {
    "ID", "Nombre", "Categoría", "Precio (S/)", "Stock", "Estado"
  };
  private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

  // Posición X de cada columna en el PDF (A4 vertical mide 595 puntos de ancho).
  private static final float[] X_PDF = {40, 101, 280, 397, 464, 520};
  private static final float MARGEN_SUPERIOR = 790;
  private static final float MARGEN_INFERIOR = 50;

  public byte[] excel(List<Producto> productos) {
    try (Workbook libro = new XSSFWorkbook();
        ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
      Sheet hoja = libro.createSheet("Productos");

      Font fuente = libro.createFont();
      fuente.setBold(true);
      fuente.setColor(IndexedColors.WHITE.getIndex());
      CellStyle encabezado = libro.createCellStyle();
      encabezado.setFont(fuente);
      encabezado.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
      encabezado.setFillPattern(FillPatternType.SOLID_FOREGROUND);

      // El precio se guarda como número (no texto) para que se pueda sumar en Excel.
      CellStyle moneda = libro.createCellStyle();
      moneda.setDataFormat(libro.createDataFormat().getFormat("#,##0.00"));

      Row filaEncabezado = hoja.createRow(0);
      for (int i = 0; i < COLUMNAS.length; i++) {
        Cell celda = filaEncabezado.createCell(i);
        celda.setCellValue(COLUMNAS[i]);
        celda.setCellStyle(encabezado);
      }

      int numero = 1;
      for (Producto p : productos) {
        Row fila = hoja.createRow(numero++);
        fila.createCell(0).setCellValue(p.getId());
        fila.createCell(1).setCellValue(texto(p.getNombre()));
        fila.createCell(2).setCellValue(nombreCategoria(p));
        Cell precio = fila.createCell(3);
        precio.setCellValue(p.getPrecio() == null ? 0 : p.getPrecio().doubleValue());
        precio.setCellStyle(moneda);
        fila.createCell(4).setCellValue(p.getStock() == null ? 0 : p.getStock());
        fila.createCell(5).setCellValue(p.isEstado() ? "Activo" : "Inactivo");
      }

      for (int i = 0; i < COLUMNAS.length; i++) hoja.autoSizeColumn(i);
      libro.write(salida);
      return salida.toByteArray();
    } catch (IOException e) {
      throw new UncheckedIOException("No se pudo generar el reporte Excel.", e);
    }
  }

  public byte[] pdf(List<Producto> productos) {
    try (PDDocument documento = new PDDocument();
        ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
      PDPage pagina = new PDPage(PDRectangle.A4);
      documento.addPage(pagina);
      PDPageContentStream contenido = new PDPageContentStream(documento, pagina);
      try {
        float y = MARGEN_SUPERIOR;
        escribir(
            contenido,
            PDType1Font.HELVETICA_BOLD,
            16,
            40,
            y,
            "PetCare Store - Reporte de productos");
        y -= 18;
        escribir(
            contenido,
            PDType1Font.HELVETICA,
            9,
            40,
            y,
            "Generado: "
                + LocalDateTime.now().format(FECHA)
                + "   |   Productos: "
                + productos.size());
        y -= 28;
        y = encabezadoPdf(contenido, y);

        for (Producto p : productos) {
          if (y < MARGEN_INFERIOR) { // sin espacio: nueva página con encabezado repetido
            contenido.close();
            pagina = new PDPage(PDRectangle.A4);
            documento.addPage(pagina);
            contenido = new PDPageContentStream(documento, pagina);
            y = encabezadoPdf(contenido, MARGEN_SUPERIOR);
          }
          String[] valores = {
            String.valueOf(p.getId()),
            texto(p.getNombre()),
            nombreCategoria(p),
            formatoPrecio(p.getPrecio()),
            String.valueOf(p.getStock() == null ? 0 : p.getStock()),
            p.isEstado() ? "Activo" : "Inactivo"
          };
          for (int i = 0; i < valores.length; i++) {
            float borde = i + 1 < X_PDF.length ? X_PDF[i + 1] : 560;
            String valor = ajustarColumna(valores[i], borde - X_PDF[i] - 4);
            escribir(contenido, PDType1Font.HELVETICA, 9, X_PDF[i], y, valor);
          }
          y -= 16;
        }
        if (productos.isEmpty())
          escribir(
              contenido,
              PDType1Font.HELVETICA,
              9,
              40,
              y,
              "No hay productos para los filtros aplicados.");
      } finally {
        contenido.close();
      }
      documento.save(salida);
      return salida.toByteArray();
    } catch (IOException e) {
      throw new UncheckedIOException("No se pudo generar el reporte PDF.", e);
    }
  }

  private float encabezadoPdf(PDPageContentStream contenido, float y) throws IOException {
    for (int i = 0; i < COLUMNAS.length; i++)
      escribir(contenido, PDType1Font.HELVETICA_BOLD, 9, X_PDF[i], y, COLUMNAS[i]);
    contenido.moveTo(40, y - 4);
    contenido.lineTo(555, y - 4);
    contenido.stroke();
    return y - 18;
  }

  private void escribir(
      PDPageContentStream contenido, PDFont fuente, float tamano, float x, float y, String valor)
      throws IOException {
    contenido.beginText();
    contenido.setFont(fuente, tamano);
    contenido.newLineAtOffset(x, y);
    contenido.showText(valor);
    contenido.endText();
  }

  /**
   * Las fuentes estándar del PDF solo cubren el alfabeto latino (WinAnsi). Tildes y ñ funcionan,
   * pero un carácter fuera de ese juego (emoji, etc.) haría fallar showText: se cambia por "?".
   */
  private String compatible(PDFont fuente, String valor) {
    StringBuilder resultado = new StringBuilder();
    valor
        .codePoints()
        .forEach(
            c -> {
              String caracter = new String(Character.toChars(c));
              try {
                fuente.encode(caracter);
                resultado.append(caracter);
              } catch (IllegalArgumentException | IOException e) {
                resultado.append('?');
              }
            });
    return resultado.toString();
  }

  private String nombreCategoria(Producto p) {
    return p.getCategoria() == null ? "" : texto(p.getCategoria().getNombre());
  }

  private String formatoPrecio(BigDecimal precio) {
    return precio == null ? "0.00" : String.format(Locale.US, "%,.2f", precio);
  }

  private String texto(String valor) {
    return valor == null ? "" : valor;
  }

  private String ajustarColumna(String valor, float ancho) throws IOException {
    PDFont fuente = PDType1Font.HELVETICA;
    String texto = compatible(fuente, valor);
    float limite = ancho * 1000 / 9;
    if (fuente.getStringWidth(texto) <= limite) return texto;
    float ocupado = fuente.getStringWidth("…");
    int fin = 0;
    while (fin < texto.length()) {
      ocupado += fuente.getStringWidth(texto.substring(fin, fin + 1));
      if (ocupado > limite) break;
      fin++;
    }
    return texto.substring(0, fin) + "…";
  }
}
