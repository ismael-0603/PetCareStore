package com.petcarestore.modelo;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
@Table(name = "usuario")
public class Usuario {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id_usuario")
  private Integer id;

  @NotBlank
  @Size(max = 100)
  @Column(nullable = false, length = 100)
  private String nombre;

  @NotBlank
  @Email
  @Size(max = 150)
  @Column(nullable = false, unique = true, length = 150)
  private String correo;

  @Column(nullable = false, length = 100)
  private String password;

  @NotNull
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private Rol rol;

  @Column(nullable = false)
  private boolean estado = true;

  @Transient private String nuevaPassword;

  public Usuario() {}

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

  public String getCorreo() {
    return correo;
  }

  public void setCorreo(String correo) {
    this.correo = correo;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public Rol getRol() {
    return rol;
  }

  public void setRol(Rol rol) {
    this.rol = rol;
  }

  public boolean isEstado() {
    return estado;
  }

  public void setEstado(boolean estado) {
    this.estado = estado;
  }

  public String getNuevaPassword() {
    return nuevaPassword;
  }

  public void setNuevaPassword(String nuevaPassword) {
    this.nuevaPassword = nuevaPassword;
  }
}
