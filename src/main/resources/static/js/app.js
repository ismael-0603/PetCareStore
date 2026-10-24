"use strict";
document.querySelectorAll("form[data-confirmar]").forEach(formulario => {
  formulario.addEventListener("submit", evento => {
    if (!window.confirm(formulario.dataset.confirmar)) evento.preventDefault();
  });
});

const imagen = document.getElementById("archivo");
if (imagen) imagen.addEventListener("change", () => {
  const archivo = imagen.files[0];
  imagen.setCustomValidity(archivo && archivo.size > 2 * 1024 * 1024 ? "La imagen debe pesar como máximo 2 MB." : "");
  imagen.reportValidity();
});

const rutaActual = window.location.pathname;
document.querySelectorAll(".barra .nav-link").forEach(enlace => {
  const destino = enlace.getAttribute("href");
  if (destino && (rutaActual === destino || (destino !== "/dashboard" && rutaActual.startsWith(destino)))) {
    enlace.classList.add("active");
    enlace.setAttribute("aria-current", "page");
  }
});
