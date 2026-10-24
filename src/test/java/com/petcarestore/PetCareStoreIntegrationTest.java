package com.petcarestore;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.petcarestore.modelo.*;
import com.petcarestore.repositorio.*;
import com.petcarestore.servicio.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class PetCareStoreIntegrationTest {
  @Autowired MockMvc mvc;
  @Autowired ProductoServicio productos;
  @Autowired CategoriaServicio categorias;
  @Autowired ClienteServicio clientes;
  @Autowired InventarioServicio inventarios;
  @Autowired UsuarioServicio usuarios;
  @Autowired ProductoRepositorio productoRepositorio;
  @Autowired CategoriaRepositorio categoriaRepositorio;
  @Autowired InventarioRepositorio inventarioRepositorio;
  @Autowired UsuarioRepositorio usuarioRepositorio;
  @Autowired PasswordEncoder encoder;
  @Autowired ImagenServicio imagenes;
  @Autowired org.springframework.transaction.PlatformTransactionManager transacciones;
  Categoria categoria;
  Producto producto;

  @BeforeEach
  void datos() {
    categoria = new Categoria();
    categoria.setNombre("Accesorios prueba");
    categoria = categoriaRepositorio.save(categoria);
    producto = new Producto();
    producto.setNombre("Collar de prueba");
    producto.setCategoria(categoria);
    producto.setPrecio(new BigDecimal("25.00"));
    producto.setStock(5);
    producto = productoRepositorio.saveAndFlush(producto);
    Inventario i = new Inventario();
    i.setProducto(producto);
    i.setCantidad(5);
    i.setFechaActualizacion(LocalDateTime.now());
    inventarioRepositorio.saveAndFlush(i);
  }

  @Test
  void anonimoEsRedirigido() throws Exception {
    mvc.perform(get("/dashboard")).andExpect(status().is3xxRedirection());
    mvc.perform(get("/login")).andExpect(status().isOk());
  }

  @Test
  @WithMockUser(roles = "EMPLEADO")
  void empleadoConsultaVistas() throws Exception {
    for (String ruta :
        new String[] {
          "/dashboard",
          "/productos",
          "/inventario",
          "/inventario/editar/"
              + inventarioRepositorio.findByProductoId(producto.getId()).orElseThrow().getId()
        }) mvc.perform(get(ruta)).andExpect(status().isOk());
  }

  @Test
  @WithMockUser(roles = "EMPLEADO")
  void empleadoNoAccedeAModulosAdministrativos() throws Exception {
    for (String ruta :
        new String[] {
          "/usuarios",
          "/clientes",
          "/categorias",
          "/productos/nuevo",
          "/productos/editar/" + producto.getId()
        }) mvc.perform(get(ruta)).andExpect(status().isForbidden());
    mvc.perform(post("/productos/eliminar/" + producto.getId()).with(csrf()))
        .andExpect(status().isForbidden());
    mvc.perform(post("/productos/guardar").with(csrf())).andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(roles = "EMPLEADO")
  void servicioRechazaEmpleado() {
    assertThrows(
        org.springframework.security.access.AccessDeniedException.class,
        () -> productos.eliminar(producto.getId()));
  }

  @Test
  @WithMockUser(roles = "ADMINISTRADOR")
  void formulariosYListasRenderizan() throws Exception {
    for (String modulo : new String[] {"productos", "categorias", "clientes", "usuarios"}) {
      mvc.perform(get("/" + modulo)).andExpect(status().isOk());
      mvc.perform(get("/" + modulo + "/nuevo")).andExpect(status().isOk());
    }
    mvc.perform(get("/productos/editar/" + producto.getId())).andExpect(status().isOk());
    mvc.perform(get("/categorias/editar/" + categoria.getId())).andExpect(status().isOk());
  }

  @Test
  @WithMockUser(roles = "ADMINISTRADOR")
  void csrfObligatorio() throws Exception {
    mvc.perform(post("/productos/eliminar/" + producto.getId())).andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(roles = "ADMINISTRADOR")
  void crearYEditarSincronizaStock() {
    Producto nuevo = new Producto();
    nuevo.setNombre("Pelota");
    nuevo.setCategoria(categoria);
    nuevo.setPrecio(BigDecimal.TEN);
    nuevo.setStock(12);
    nuevo = productos.guardar(nuevo);
    assertEquals(
        12, inventarioRepositorio.findByProductoId(nuevo.getId()).orElseThrow().getCantidad());
    nuevo.setStock(3);
    productos.guardar(nuevo);
    assertEquals(
        3, inventarioRepositorio.findByProductoId(nuevo.getId()).orElseThrow().getCantidad());
  }

  @Test
  @WithMockUser(roles = "EMPLEADO")
  void empleadoActualizaCantidad() {
    Inventario i = inventarioRepositorio.findByProductoId(producto.getId()).orElseThrow();
    inventarios.actualizar(i.getId(), 15, null);
    assertEquals(15, productoRepositorio.findById(producto.getId()).orElseThrow().getStock());
    assertEquals(15, i.getCantidad());
    assertEquals(10, i.getStockMinimo());
  }

  @Test
  @WithMockUser(roles = "EMPLEADO")
  void empleadoNoModificaMinimo() {
    Integer id = inventarioRepositorio.findByProductoId(producto.getId()).orElseThrow().getId();
    assertThrows(
        org.springframework.security.access.AccessDeniedException.class,
        () -> inventarios.actualizar(id, 2, 1));
  }

  @Test
  @WithMockUser(roles = "ADMINISTRADOR")
  void inventarioRechazaNegativos() {
    assertThrows(
        ReglaNegocioException.class,
        () ->
            inventarios.actualizar(
                inventarioRepositorio.findByProductoId(producto.getId()).orElseThrow().getId(),
                -1,
                10));
  }

  @Test
  @WithMockUser(roles = "ADMINISTRADOR")
  void filtrosSeCombinan() {
    assertEquals(
        1,
        productos
            .filtrar("collar", categoria.getId(), true, new BigDecimal("20"), new BigDecimal("30"))
            .size());
    assertTrue(productos.filtrar("collar", categoria.getId(), false, null, null).isEmpty());
    assertThrows(
        ReglaNegocioException.class,
        () -> productos.filtrar(null, null, null, BigDecimal.TEN, BigDecimal.ONE));
  }

  @Test
  @WithMockUser(roles = "ADMINISTRADOR")
  void categoriaConProductosNoSeElimina() {
    assertThrows(ReglaNegocioException.class, () -> categorias.eliminar(categoria.getId()));
  }

  @Test
  @WithMockUser(roles = "ADMINISTRADOR")
  void eliminarProductoEliminaInventario() {
    Integer id = producto.getId();
    productos.eliminar(id);
    productoRepositorio.flush();
    assertFalse(productoRepositorio.existsById(id));
    assertTrue(inventarioRepositorio.findByProductoId(id).isEmpty());
  }

  @Test
  @WithMockUser(roles = "ADMINISTRADOR")
  void validacionesYRegistroInexistente() throws Exception {
    mvc.perform(
            post("/productos/guardar")
                .with(csrf())
                .param("nombre", "")
                .param("precio", "-2")
                .param("stock", "-1"))
        .andExpect(status().isOk())
        .andExpect(model().hasErrors());
    mvc.perform(get("/productos/editar/999999")).andExpect(status().isNotFound());
    mvc.perform(get("/productos").param("minimo", "abc")).andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser(roles = "ADMINISTRADOR")
  void categoriaDuplicadaSeRechaza() {
    Categoria c = new Categoria();
    c.setNombre(categoria.getNombre());
    assertThrows(ReglaNegocioException.class, () -> categorias.guardar(c));
  }

  @Test
  @WithMockUser(roles = "ADMINISTRADOR")
  void clienteCrudYDuplicados() {
    Cliente c = new Cliente();
    c.setNombre("Ana");
    c.setApellido("Rojas");
    c.setDocumento("12345678");
    c.setTelefono("987654321");
    c.setCorreo("ana@example.com");
    c.setDireccion("Lima");
    c = clientes.guardar(c);
    assertEquals("Ana", clientes.buscarPorId(c.getId()).getNombre());
    c.setApellido("Torres");
    clientes.guardar(c);
    assertEquals("Torres", clientes.buscarPorId(c.getId()).getApellido());
    Cliente duplicado = new Cliente();
    duplicado.setDocumento("12345678");
    assertThrows(ReglaNegocioException.class, () -> clientes.guardar(duplicado));
    clientes.eliminar(c.getId());
  }

  @Test
  @WithMockUser(roles = "ADMINISTRADOR")
  void usuarioCifraYConservaContrasena() {
    Usuario u = new Usuario();
    u.setNombre("Ana");
    u.setCorreo("ana@example.com");
    u.setRol(Rol.EMPLEADO);
    u.setNuevaPassword("Prueba123!");
    u = usuarios.guardar(u);
    assertTrue(encoder.matches("Prueba123!", u.getPassword()));
    String hash = u.getPassword();
    Usuario edicion = new Usuario();
    edicion.setId(u.getId());
    edicion.setNombre("Ana editada");
    edicion.setCorreo(u.getCorreo());
    edicion.setRol(Rol.EMPLEADO);
    usuarios.guardar(edicion);
    assertEquals(hash, u.getPassword());
  }

  @Test
  @WithMockUser(roles = "ADMINISTRADOR")
  void ultimoAdministradorProtegido() {
    Usuario u = new Usuario();
    u.setNombre("Admin");
    u.setCorreo("admin@example.com");
    u.setRol(Rol.ADMINISTRADOR);
    u.setNuevaPassword("Prueba123!");
    u = usuarios.guardar(u);
    Usuario edicion = new Usuario();
    edicion.setId(u.getId());
    edicion.setNombre(u.getNombre());
    edicion.setCorreo(u.getCorreo());
    edicion.setRol(Rol.EMPLEADO);
    assertThrows(ReglaNegocioException.class, () -> usuarios.guardar(edicion));
  }

  @Test
  void rechazaImagenFalsa() {
    assertThrows(
        ReglaNegocioException.class,
        () ->
            imagenes.guardar(
                new MockMultipartFile("archivo", "falso.png", "image/png", "texto".getBytes())));
  }

  @Test
  void guardaImagenValida() throws Exception {
    for (String formato : new String[] {"png", "jpg"}) {
      var buffer = new java.io.ByteArrayOutputStream();
      javax.imageio.ImageIO.write(
          new java.awt.image.BufferedImage(2, 2, java.awt.image.BufferedImage.TYPE_INT_RGB),
          formato,
          buffer);
      String nombre =
          imagenes.guardar(
              new MockMultipartFile(
                  "archivo", "foto." + formato, "image/" + formato, buffer.toByteArray()));
      assertTrue(nombre.endsWith("." + formato));
      assertTrue(java.nio.file.Files.exists(java.nio.file.Path.of("target/test-uploads", nombre)));
    }
  }

  @Test
  void usuarioInactivoNoPuedeIniciarSesion() throws Exception {
    Usuario u = new Usuario();
    u.setNombre("Inactivo");
    u.setCorreo("inactivo@example.com");
    u.setPassword(encoder.encode("Prueba123!"));
    u.setRol(Rol.EMPLEADO);
    u.setEstado(false);
    usuarioRepositorio.saveAndFlush(u);
    mvc.perform(
            post("/login")
                .with(csrf())
                .param("correo", u.getCorreo())
                .param("password", "Prueba123!"))
        .andExpect(redirectedUrl("/login?error"));
  }

  @Test
  @WithMockUser(roles = "ADMINISTRADOR")
  void alertaSoloBajoMinimo() {
    assertEquals(1, inventarioRepositorio.contarStockBajo());
    Inventario i = inventarioRepositorio.findByProductoId(producto.getId()).orElseThrow();
    inventarios.actualizar(i.getId(), 10, 10);
    inventarioRepositorio.flush();
    assertEquals(0, inventarioRepositorio.contarStockBajo());
  }

  @Test
  void desactivarCuentaCierraSesionExistente() throws Exception {
    Usuario u = new Usuario();
    u.setNombre("Sesion");
    u.setCorreo("sesion@example.com");
    u.setPassword(encoder.encode("Prueba123!"));
    u.setRol(Rol.EMPLEADO);
    u = usuarioRepositorio.saveAndFlush(u);
    var sesion = new org.springframework.mock.web.MockHttpSession();
    var principal = new com.petcarestore.configuracion.UsuarioAutenticado(u);
    var contexto =
        org.springframework.security.core.context.SecurityContextHolder.createEmptyContext();
    contexto.setAuthentication(
        org.springframework.security.authentication.UsernamePasswordAuthenticationToken
            .authenticated(principal, null, principal.getAuthorities()));
    sesion.setAttribute(
        org.springframework.security.web.context.HttpSessionSecurityContextRepository
            .SPRING_SECURITY_CONTEXT_KEY,
        contexto);
    u.setEstado(false);
    usuarioRepositorio.saveAndFlush(u);
    mvc.perform(get("/dashboard").session(sesion)).andExpect(redirectedUrl("/login?error"));
  }

  @Test
  void cambioDeRolSeAplicaEnSesionExistente() throws Exception {
    Usuario u = new Usuario();
    u.setNombre("Sesion");
    u.setCorreo("rol@example.com");
    u.setPassword(encoder.encode("Prueba123!"));
    u.setRol(Rol.ADMINISTRADOR);
    u = usuarioRepositorio.saveAndFlush(u);
    var principal = new com.petcarestore.configuracion.UsuarioAutenticado(u);
    u.setRol(Rol.EMPLEADO);
    usuarioRepositorio.saveAndFlush(u);
    mvc.perform(get("/usuarios").with(user(principal))).andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(roles = "ADMINISTRADOR")
  void formularioProductoCreaYActualizaEnBase() throws Exception {
    mvc.perform(
            post("/productos/guardar")
                .with(csrf())
                .param("nombre", "Producto desde formulario")
                .param("categoria.id", categoria.getId().toString())
                .param("precio", "12.50")
                .param("stock", "7")
                .param("estado", "true"))
        .andExpect(redirectedUrl("/productos"));
    Producto p =
        productoRepositorio.findAll().stream()
            .filter(x -> x.getNombre().equals("Producto desde formulario"))
            .findFirst()
            .orElseThrow();
    assertEquals(7, inventarioRepositorio.findByProductoId(p.getId()).orElseThrow().getCantidad());
    mvc.perform(
            post("/productos/guardar")
                .with(csrf())
                .param("id", p.getId().toString())
                .param("nombre", "Producto editado")
                .param("categoria.id", categoria.getId().toString())
                .param("precio", "15.00")
                .param("stock", "9")
                .param("estado", "true"))
        .andExpect(redirectedUrl("/productos"));
    assertEquals(9, inventarioRepositorio.findByProductoId(p.getId()).orElseThrow().getCantidad());
  }

  @Test
  @WithMockUser(roles = "ADMINISTRADOR")
  @Transactional(propagation = org.springframework.transaction.annotation.Propagation.NOT_SUPPORTED)
  void actualizacionesSimultaneasConservanStockYMinimo() throws Exception {
    var ejecutor = java.util.concurrent.Executors.newFixedThreadPool(2);
    var administradorBloqueo = new java.util.concurrent.CountDownLatch(1);
    var empleadoInicioLectura = new java.util.concurrent.CountDownLatch(1);
    Integer productoId = producto.getId();
    Integer inventarioId = inventarioRepositorio.findByProductoId(productoId).orElseThrow().getId();
    try {
      var admin =
          ejecutor.submit(
              () -> {
                autenticarHilo("ADMINISTRADOR");
                try {
                  new org.springframework.transaction.support.TransactionTemplate(transacciones)
                      .executeWithoutResult(
                          tx -> {
                            productoRepositorio.bloquearPorId(productoId).orElseThrow();
                            inventarios.actualizar(inventarioId, 5, 20);
                            administradorBloqueo.countDown();
                            esperar(empleadoInicioLectura);
                          });
                } finally {
                  org.springframework.security.core.context.SecurityContextHolder.clearContext();
                }
              });
      var empleado =
          ejecutor.submit(
              () -> {
                autenticarHilo("EMPLEADO");
                try {
                  esperar(administradorBloqueo);
                  new org.springframework.transaction.support.TransactionTemplate(transacciones)
                      .executeWithoutResult(
                          tx -> {
                            inventarioRepositorio.buscarProductoId(inventarioId).orElseThrow();
                            empleadoInicioLectura.countDown();
                            inventarios.actualizar(inventarioId, 13, null);
                          });
                } finally {
                  org.springframework.security.core.context.SecurityContextHolder.clearContext();
                }
              });
      admin.get(30, java.util.concurrent.TimeUnit.SECONDS);
      empleado.get(30, java.util.concurrent.TimeUnit.SECONDS);
      Inventario resultado = inventarioRepositorio.findByProductoId(productoId).orElseThrow();
      assertEquals(20, resultado.getStockMinimo());
      assertEquals(13, resultado.getCantidad());
      assertEquals(13, productoRepositorio.findById(productoId).orElseThrow().getStock());
    } finally {
      administradorBloqueo.countDown();
      empleadoInicioLectura.countDown();
      ejecutor.shutdownNow();
      ejecutor.awaitTermination(10, java.util.concurrent.TimeUnit.SECONDS);
      productos.eliminar(productoId);
      categorias.eliminar(categoria.getId());
    }
  }

  private void autenticarHilo(String rol) {
    var auth =
        org.springframework.security.authentication.UsernamePasswordAuthenticationToken
            .authenticated(
                "prueba",
                null,
                org.springframework.security.core.authority.AuthorityUtils.createAuthorityList(
                    "ROLE_" + rol));
    org.springframework.security.core.context.SecurityContextHolder.getContext()
        .setAuthentication(auth);
  }

  private void esperar(java.util.concurrent.CountDownLatch paso) {
    try {
      if (!paso.await(15, java.util.concurrent.TimeUnit.SECONDS))
        throw new IllegalStateException("No avanzo la transaccion concurrente");
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException(e);
    }
  }
}
