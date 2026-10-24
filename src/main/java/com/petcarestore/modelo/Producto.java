package com.petcarestore.modelo;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

@Entity
@Table(name = "producto")
public class Producto {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id_producto")
  private Integer id;

  @NotNull
  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "id_categoria", nullable = false)
  private Categoria categoria;

  @NotBlank
  @Size(max = 150)
  @Column(nullable = false, length = 150)
  private String nombre;

  @Size(max = 1000)
  @Column(length = 1000)
  private String descripcion;

  @NotNull
  @DecimalMin("0.00")
  @Digits(integer = 8, fraction = 2)
  @Column(nullable = false, precision = 10, scale = 2)
  private BigDecimal precio;

  @NotNull
  @Min(0)
  @Column(nullable = false)
  private Integer stock = 0;

  @Column(length = 255)
  private String imagen;

  @Column(nullable = false)
  private boolean estado = true;

  public Producto() {}

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public Categoria getCategoria() {
    return categoria;
  }

  public void setCategoria(Categoria categoria) {
    this.categoria = categoria;
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

  public BigDecimal getPrecio() {
    return precio;
  }

  public void setPrecio(BigDecimal precio) {
    this.precio = precio;
  }

  public Integer getStock() {
    return stock;
  }

  public void setStock(Integer stock) {
    this.stock = stock;
  }

  public String getImagen() {
    return imagen;
  }

  public void setImagen(String imagen) {
    this.imagen = imagen;
  }

  public boolean isEstado() {
    return estado;
  }

  public void setEstado(boolean estado) {
    this.estado = estado;
  }
}
