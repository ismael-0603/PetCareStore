package com.petcarestore.modelo;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "inventario")
public class Inventario {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id_inventario")
  private Integer id;

  @NotNull
  @OneToOne
  @JoinColumn(name = "id_producto", nullable = false, unique = true)
  private Producto producto;

  @NotNull
  @Min(0)
  @Column(nullable = false)
  private Integer cantidad = 0;

  @NotNull
  @Min(0)
  @Column(name = "stock_minimo", nullable = false)
  private Integer stockMinimo = 10;

  @Column(name = "fecha_actualizacion", nullable = false)
  private LocalDateTime fechaActualizacion;

  public Inventario() {}

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public Producto getProducto() {
    return producto;
  }

  public void setProducto(Producto producto) {
    this.producto = producto;
  }

  public Integer getCantidad() {
    return cantidad;
  }

  public void setCantidad(Integer cantidad) {
    this.cantidad = cantidad;
  }

  public Integer getStockMinimo() {
    return stockMinimo;
  }

  public void setStockMinimo(Integer stockMinimo) {
    this.stockMinimo = stockMinimo;
  }

  public LocalDateTime getFechaActualizacion() {
    return fechaActualizacion;
  }

  public void setFechaActualizacion(LocalDateTime fechaActualizacion) {
    this.fechaActualizacion = fechaActualizacion;
  }

  public boolean isStockBajo() {
    return cantidad < stockMinimo;
  }
}
