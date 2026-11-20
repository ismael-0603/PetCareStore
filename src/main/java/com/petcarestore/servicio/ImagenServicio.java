package com.petcarestore.servicio;

import java.io.*;
import java.nio.file.*;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ImagenServicio {
  private static final Logger LOG = LoggerFactory.getLogger(ImagenServicio.class);
  private final Path directorio;

  public ImagenServicio(@Value("${petcare.imagenes.directorio}") String directorio) {
    this.directorio = Path.of(directorio).toAbsolutePath().normalize();
  }

  private void descartar(Path destino) {
    try {
      Files.deleteIfExists(destino);
    } catch (IOException e) {
      LOG.error("No se pudo retirar la imagen {}", destino.getFileName(), e);
    }
  }

  public String guardar(MultipartFile archivo) {
    if (archivo == null || archivo.isEmpty()) return null;
    if (archivo.getSize() > 2 * 1024 * 1024)
      throw new ReglaNegocioException("La imagen debe pesar como máximo 2 MB.");
    try (InputStream datos = archivo.getInputStream();
        ImageInputStream entrada = ImageIO.createImageInputStream(datos)) {
      var lectores = ImageIO.getImageReaders(entrada);
      if (!lectores.hasNext())
        throw new ReglaNegocioException("Seleccione una imagen JPG o PNG válida.");
      ImageReader lector = lectores.next();
      String formato = lector.getFormatName().toLowerCase(Locale.ROOT);
      try {
        lector.setInput(entrada);
        if (!Set.of("jpeg", "jpg", "png").contains(formato)
            || lector.getWidth(0) > 6000
            || lector.getHeight(0) > 6000)
          throw new ReglaNegocioException("Use JPG o PNG de hasta 6000 x 6000 píxeles.");
        var imagen = lector.read(0);
        String extension = formato.equals("png") ? "png" : "jpg";
        Files.createDirectories(directorio);
        String nombre = UUID.randomUUID() + "." + extension;
        Path destino = directorio.resolve(nombre);
        try {
          if (!ImageIO.write(imagen, extension, destino.toFile()))
            throw new IOException("No hay codificador disponible para la imagen.");
        } catch (IOException | RuntimeException e) {
          descartar(destino);
          throw e;
        }
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
          TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int estado) {
              if (estado == STATUS_ROLLED_BACK) descartar(destino);
            }
          });
        }
        return nombre;
      } finally {
        lector.dispose();
      }
    } catch (IOException e) {
      throw new ReglaNegocioException(
          "No se pudo guardar la imagen. Revise el archivo y los permisos de la carpeta.");
    }
  }
}
