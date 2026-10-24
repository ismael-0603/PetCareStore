package com.petcarestore.configuracion;

import java.nio.file.Path;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

@Configuration
public class RecursosConfiguracion implements WebMvcConfigurer {
  private final String directorio;

  public RecursosConfiguracion(@Value("${petcare.imagenes.directorio}") String directorio) {
    this.directorio = directorio;
  }

  public void addResourceHandlers(ResourceHandlerRegistry registro) {
    registro
        .addResourceHandler("/imagenes/**")
        .addResourceLocations(
            Path.of(directorio).toAbsolutePath().normalize().toUri().toString() + "/");
  }
}
