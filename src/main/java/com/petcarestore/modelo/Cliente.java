package com.petcarestore.modelo;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
@Table(name = "cliente")
public class Cliente {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id_cliente")
  private Integer id;

  @NotBlank
  @Size(max = 100)
  @Column(nullable = false, length = 100)
  private String nombre;

  @NotBlank
  @Size(max = 100)
  @Column(nullable = false, length = 100)
  private String apellido;

  @NotBlank
  @Pattern(
      regexp = "[A-Za-z0-9-]{6,20}",
      message = "Debe tener entre 6 y 20 letras, numeros o guiones")
  @Column(nullable = false, unique = true, length = 20)
  private String documento;

  @NotBlank
  @Pattern(regexp = "[+0-9 ()-]{6,20}", message = "Telefono no valido")
  @Column(nullable = false, length = 20)
  private String telefono;

  @NotBlank
  @Email
  @Size(max = 150)
  @Column(nullable = false, length = 150)
  private String correo;

  @NotBlank
  @Size(max = 250)
  @Column(nullable = false, length = 250)
  private String direccion;

  public Cliente() {}

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

  public String getApellido() {
    return apellido;
  }

  public void setApellido(String apellido) {
    this.apellido = apellido;
  }

  public String getDocumento() {
    return documento;
  }

  public void setDocumento(String documento) {
    this.documento = documento;
  }

  public String getTelefono() {
    return telefono;
  }

  public void setTelefono(String telefono) {
    this.telefono = telefono;
  }

  public String getCorreo() {
    return correo;
  }

  public void setCorreo(String correo) {
    this.correo = correo;
  }

  public String getDireccion() {
    return direccion;
  }

  public void setDireccion(String direccion) {
    this.direccion = direccion;
  }
}
