package com.petcarestore.modelo;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
@Table(name = "categoria")
public class Categoria {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id_categoria")
  private Integer id;

  @NotBlank
  @Size(max = 100)
  @Column(nullable = false, unique = true, length = 100)
  private String nombre;

  @Size(max = 500)
  @Column(length = 500)
  private String descripcion;

  @Column(nullable = false)
  private boolean estado = true;

  public Categoria() {}

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public String getNombre() {
    return nombre;
  }

  public void setNombre(String nombre) {
    this.nombre = nombre;
  }

  public String getDescripcion() {
    return descripcion;
  }

  public void setDescripcion(String descripcion) {
    this.descripcion = descripcion;
  }

  public boolean isEstado() {
    return estado;
  }

  public void setEstado(boolean estado) {
    this.estado = estado;
  }
}
