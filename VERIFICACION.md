# Verificación de PetCare Store

Fecha: 23 de octubre de 2026, America/Lima.

## Entorno utilizado

- Java Temurin 17.0.20.1, compilación con `release 17`.
- Maven 3.9.11 y Maven Wrapper 3.3.4.
- Spring Boot 3.5.7; Bootstrap 5.3.8 local.
- MySQL Community 8.4.7, instancia portátil local en `127.0.0.1:3307`.
- Base de la aplicación: `petcare_db`. Base exclusiva de pruebas: `petcare_test`.

## Resultados automatizados

| Comprobación | Resultado |
|---|---|
| Compilación Java 17 | Correcta |
| Suite de integración contra MySQL 8.4.7 | 26 pruebas, 0 fallos, 0 errores |
| Maven Wrapper `verify`, suite H2 de pruebas | BUILD SUCCESS; 26 pruebas, 0 fallos, 0 errores |
| Empaquetado Spring Boot | JAR ejecutable generado |
| SQL sobre MySQL real | Tablas y registros creados correctamente |
| Segunda ejecución del SQL | No duplica ni borra los datos existentes |
| Inicio con `ddl-auto=validate` contra el SQL entregado | Correcto; Tomcat en 8081 |

Los comandos usaron el repositorio Maven local `.tools/m2` para aislar las descargas de verificación. Los registros completos se conservan localmente en `.tools/mysql-entrega.log` y `.tools/verify-entrega.log`.

La suite comprueba renderizado de formularios/listas, permisos de rutas y servicios, CSRF, validaciones, registros inexistentes, filtros combinados, CRUD, categorías con productos, documentos duplicados, BCrypt, conservación de contraseña, último administrador, sesiones desactivadas, cambios de rol, imágenes válidas e inválidas, alertas y sincronización del inventario. Una prueba ejecuta dos transacciones concurrentes y comprueba que actualizar cantidades no revierta un cambio simultáneo del stock mínimo. H2 está limitado al alcance de pruebas en Maven; la aplicación real usa MySQL.

## Comprobaciones en navegador

- Inicio de sesión y cierre de sesión con las dos cuentas del SQL.
- Dashboard obtenido de MySQL: 5 productos, 2 clientes, 5 categorías y 3 productos con stock bajo en los datos iniciales.
- Registro y edición desde el formulario real de productos.
- Combinación de nombre, categoría, disponibilidad y precios.
- Actualización de inventario por empleado; mínimo mostrado en modo de lectura.
- Acceso directo del empleado a `/usuarios`: pantalla de acceso restringido; la suite también verifica estado HTTP 403.
- Menú móvil funcional y ausencia de desbordamiento horizontal en dashboard y productos a 390 × 844 píxeles; dashboard revisado también a 1280 × 800 píxeles. Las tablas se desplazan dentro de su contenedor.
- Persistencia tras detener e iniciar el JAR final: el registro temporal conservó precio y stock 13. MySQL confirmó `producto.stock = inventario.cantidad = 13`.
- El registro temporal se retiró al terminar. La consulta final detectó **0 inconsistencias** y mantuvo los cinco productos de ejemplo.

Las capturas están en `docs/capturas`.

## Particularidades del entorno local

La terminal inicialmente solo encontraba Java 8. Se descargaron herramientas de verificación oficiales en `.tools`, ignorada por Git y excluida del ZIP de fuentes. No se cambió el Java global ni se instaló un servicio MySQL del sistema.

El aislamiento de este entorno requirió permisos de red para Maven y conexiones locales. Además, los sockets Unix internos de Java fallaban en Windows; el inicio verificado usó `-Djdk.net.unixdomain.tmpdir=./.tools/no-sockets`, una ruta inexistente que hace que Java utilice TCP para sus comunicaciones internas. La opción está documentada también en el README para reproducir este entorno si fuera necesario.

La instancia de demostración se dejó iniciada en http://localhost:8081 con MySQL en 3307. Para reutilizar esa base desde IntelliJ, seleccionar el JDK 17 descargado y definir:

```text
DB_URL=jdbc:mysql://127.0.0.1:3307/petcare_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=America/Lima
DB_USERNAME=root
DB_PASSWORD=
```

La cuenta MySQL sin contraseña corresponde únicamente a esta instancia portátil de prueba, vinculada a loopback. Para una instalación propia, seguir el README y configurar sus credenciales. La aplicación y esta instancia local deben estar detenidas antes de volver a generar o reemplazar sus archivos ejecutables en Windows.

## Archivos de entrega

- Fuentes y recursos completos en la carpeta del proyecto.
- `entrega/PetCareStore.zip`: proyecto Maven para abrir en IntelliJ, sin herramientas temporales ni base de datos física.
- `target/PetCareStore-1.0.0.jar`: ejecutable construido y arrancado durante la verificación.
- `sql/petcare_db.sql`: esquema y datos de demostración con hashes BCrypt.
- `README.md`: instalación, configuración, credenciales y guion de demostración.

El ZIP requiere un JDK 17 y una instalación/configuración de MySQL en el equipo de destino; las tablas no se sustituyen por datos simulados.
