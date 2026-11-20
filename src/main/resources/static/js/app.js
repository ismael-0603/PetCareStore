"use strict";

/* Confirmación de eliminación con modal Bootstrap reutilizable */
const modalConfirmarEl = document.getElementById("modalConfirmar");
const modalConfirmarTexto = document.getElementById("modalConfirmarTexto");
const modalConfirmarBtn = document.getElementById("modalConfirmarBtn");
let formularioPendiente = null;

if (modalConfirmarEl && typeof bootstrap !== "undefined") {
  const modalConfirmar = new bootstrap.Modal(modalConfirmarEl);
  document.querySelectorAll("form[data-confirmar]").forEach(formulario => {
    formulario.addEventListener("submit", evento => {
      if (formulario.dataset.confirmado === "true") return;
      evento.preventDefault();
      formularioPendiente = formulario;
      if (modalConfirmarTexto) {
        modalConfirmarTexto.textContent = formulario.dataset.confirmar;
      }
      modalConfirmar.show();
    });
  });

  if (modalConfirmarBtn) {
    modalConfirmarBtn.addEventListener("click", () => {
      if (!formularioPendiente) return;
      formularioPendiente.dataset.confirmado = "true";
      modalConfirmar.hide();
      formularioPendiente.submit();
      formularioPendiente = null;
    });
  }
} else {
  document.querySelectorAll("form[data-confirmar]").forEach(formulario => {
    formulario.addEventListener("submit", evento => {
      if (!window.confirm(formulario.dataset.confirmar)) evento.preventDefault();
    });
  });
}

/* Validación de tamaño (<= 2 MB) y previsualización en vivo de imagen */
document.querySelectorAll('input[type="file"][name="archivo"]').forEach(input => {
  input.addEventListener("change", () => {
    const archivo = input.files[0];
    const excede = archivo && archivo.size > 2 * 1024 * 1024;
    input.setCustomValidity(excede ? "La imagen debe pesar como máximo 2 MB." : "");
    input.reportValidity();

    const previewSelector = input.dataset.preview;
    if (!previewSelector) return;
    const previewImg = document.querySelector(previewSelector);
    if (!previewImg) return;
    if (archivo && !excede && archivo.type.startsWith("image/")) {
      const url = URL.createObjectURL(archivo);
      previewImg.src = url;
      previewImg.onload = () => URL.revokeObjectURL(url);
    } else if (previewImg.dataset.defaultSrc) {
      previewImg.src = previewImg.dataset.defaultSrc;
    }
  });
});

/* Resaltar enlace activo en la barra de navegación */
const rutaActual = window.location.pathname;
document.querySelectorAll(".barra .nav-link").forEach(enlace => {
  const destino = enlace.getAttribute("href");
  if (destino && (rutaActual === destino || (destino !== "/dashboard" && rutaActual.startsWith(destino)))) {
    enlace.classList.add("active");
    enlace.setAttribute("aria-current", "page");
  }
});

/* Mostrar / ocultar contraseña */
document.querySelectorAll("[data-toggle-password]").forEach(boton => {
  boton.addEventListener("click", () => {
    const campo = document.getElementById(boton.dataset.togglePassword);
    if (!campo) return;
    const visible = campo.type === "text";
    campo.type = visible ? "password" : "text";
    boton.setAttribute("aria-pressed", String(!visible));
  });
});

/* Filtro instantáneo de estado en Inventario (Todos / Stock bajo / Normal) */
const botonesInv = document.querySelectorAll("[data-inv-filtro]");
if (botonesInv.length > 0) {
  const filas = document.querySelectorAll("[data-inv-estado]");
  const vacioFiltro = document.getElementById("invVacioFiltro");
  const aplicarFiltroInv = filtro => {
    let visibles = 0;
    botonesInv.forEach(b => b.classList.toggle("active", b.dataset.invFiltro === filtro));
    filas.forEach(fila => {
      const mostrar = filtro === "todos" || fila.dataset.invEstado === filtro;
      fila.classList.toggle("d-none", !mostrar);
      if (mostrar) visibles++;
    });
    if (vacioFiltro) {
      vacioFiltro.classList.toggle("d-none", visibles > 0);
    }
  };
  botonesInv.forEach(boton => {
    boton.addEventListener("click", () => aplicarFiltroInv(boton.dataset.invFiltro));
  });
  const params = new URLSearchParams(window.location.search);
  if (params.get("filtro") === "bajo") {
    aplicarFiltroInv("bajo");
  }
}

/* Botones stepper rápidos en formulario de Inventario (-5, -1, +1, +5) */
const inputCantidad = document.getElementById("cantidad");
const estadoPreview = document.getElementById("estadoInventarioPreview");
if (inputCantidad) {
  const actualizarPreviewInventario = () => {
    if (!estadoPreview) return;
    const minEl = document.getElementById("stockMinimo") || document.getElementById("minimoLectura");
    const minVal = minEl ? parseInt(minEl.value, 10) || 0 : 10;
    const cantVal = parseInt(inputCantidad.value, 10) || 0;
    const bajo = cantVal < minVal;
    estadoPreview.className = bajo
      ? "rounded-4 p-3 small fw-semibold bg-warning-subtle text-warning-emphasis border border-warning-subtle"
      : "rounded-4 p-3 small fw-semibold bg-success-subtle text-success-emphasis border border-success-subtle";
    estadoPreview.textContent = bajo
      ? `Estado resultante: STOCK BAJO (faltan ${minVal - cantVal} unidades para alcanzar el mínimo de ${minVal}).`
      : `Estado resultante: NORMAL (${cantVal} unidades disponibles, mínimo ${minVal}).`;
  };

  document.querySelectorAll("[data-step-delta]").forEach(btn => {
    btn.addEventListener("click", () => {
      const delta = parseInt(btn.dataset.stepDelta, 10) || 0;
      const actual = parseInt(inputCantidad.value, 10) || 0;
      inputCantidad.value = Math.max(0, actual + delta);
      actualizarPreviewInventario();
    });
  });

  inputCantidad.addEventListener("input", actualizarPreviewInventario);
  const inputMinimo = document.getElementById("stockMinimo");
  if (inputMinimo) inputMinimo.addEventListener("input", actualizarPreviewInventario);
  actualizarPreviewInventario();
}
