# PetCare Store

Aplicación académica de Lenguaje de Programación II: productos, categorías, clientes, inventario y usuarios. Implementa el procedimiento MVC de la Guía 4 del profesor, adaptado al modelo del proyecto PetCare Store.

## Tecnologías y organización

Java 17, Spring Boot 3.5.7, Spring Web, Spring Data JPA, Spring Security, Thymeleaf, MySQL y Bootstrap 5.3.8. Maven administra las dependencias. No requiere Node.js.

```text
src/main/java/com/petcarestore/
  PetCareStoreApplication.java
  modelo/        Entidades JPA, constructores y accesores explícitos
  repositorio/   Interfaces JpaRepository
  servicio/      Interfaces e implementaciones ServicioImp
  controlador/   Controladores MVC y manejo de errores
  configuracion/ Seguridad y recursos locales
src/main/resources/
  templates/     Login, dashboard, fragmentos, errores y módulos
  static/        CSS, JavaScript, SVG y Bootstrap local
  application.properties
src/test/        Pruebas de integración y perfil exclusivo de pruebas
sql/petcare_db.sql
```

Las vistas usan `listar.html` y `formulario.html`, reutilizando el formulario para registrar y editar. Las modificaciones usan POST y CSRF; el documento académico menciona React/API REST, pero esta implementación aplica la solicitud de Spring MVC con Thymeleaf.

## 1. Abrir en IntelliJ IDEA

1. Instalar un **JDK 17** y **MySQL 8.0.16 o superior** (recomendado MySQL 8.4).
2. Abrir esta carpeta o su `pom.xml` con **File > Open** como proyecto Maven.
3. En **Project Structure > Project SDK**, seleccionar JDK 17. En **Settings > Build Tools > Maven > Runner**, usar ese mismo JDK.
4. Pulsar **Reload All Maven Projects** en el panel Maven. Puede utilizarse Maven incluido en IntelliJ o el Maven Wrapper del proyecto. La primera descarga requiere Internet.

En esta máquina se descargó un JDK 17 para la verificación en `.tools/jdk-17.0.20.1+1`; es una herramienta local ignorada por Git. No es parte de las dependencias de la aplicación.

## 2. Crear la base de datos

Abrir `sql/petcare_db.sql` en MySQL Workbench y ejecutar el script completo. También se puede usar el cliente MySQL:

```sql
SOURCE C:/ruta/al/proyecto/sql/petcare_db.sql;
```

El script crea `petcare_db`, las cinco tablas, claves y restricciones e inserta datos de demostración. Puede ejecutarse nuevamente sin duplicar los ejemplos ni borrar registros. No actualiza datos existentes ni migra esquemas antiguos. `ddl-auto=validate` verifica el esquema: la aplicación no crea ni elimina tablas automáticamente.

## 3. Configurar conexión e imágenes

Editar `src/main/resources/application.properties` o establecer estas variables en **Run > Edit Configurations > Environment variables**:

| Variable | Valor predeterminado |
|---|---|
| `DB_URL` | `jdbc:mysql://localhost:3306/petcare_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=America/Lima` |
| `DB_USERNAME` | `root` |
| `DB_PASSWORD` | Vacío: reemplazar por la contraseña de MySQL |
| `PETCARE_UPLOAD_DIR` | `./uploads` |

La URL predeterminada corresponde al entorno local de demostración. La cuenta MySQL debe tener acceso a `petcare_db`.

Las imágenes son opcionales: JPG o PNG de hasta 2 MB y 6000 × 6000 píxeles. Se verifican y reescriben con un nombre UUID en el directorio de imágenes; la base guarda solo el nombre. Al editar sin seleccionar un archivo, se conserva la imagen. Una imagen genérica aparece cuando no hay archivo. Hacer copia de seguridad de la base y de `uploads` juntas. Los archivos anteriores se conservan al sustituir o eliminar productos y pueden limpiarse manualmente tras comprobar que no tienen referencias.

## 4. Ejecutar

Ejecutar `PetCareStoreApplication.java` desde IntelliJ. Abrir **http://localhost:8081**.

En PowerShell, con `JAVA_HOME` apuntando al JDK 17:

```powershell
.\mvnw.cmd clean verify
.\mvnw.cmd spring-boot:run
```

En Linux/macOS:

```sh
sh mvnw clean verify
sh mvnw spring-boot:run
```

Para generar el ejecutable: `mvnw.cmd package`. Después: `java -jar target/PetCareStore-1.0.0.jar`.

## 5. Credenciales de demostración

| Rol | Correo | Contraseña |
|---|---|---|
| Administrador | admin@petcarestore.com | Admin123! |
| Empleado | empleado@petcarestore.com | Empleado123! |

**Cambiar estas contraseñas fuera del entorno de pruebas**, desde Usuarios > Editar. El script almacena hashes BCrypt, nunca contraseñas en texto plano. Al editar un usuario, dejar la contraseña vacía conserva la actual. No hay registro público ni recuperación de contraseñas.

## Permisos

| Función | Administrador | Empleado |
|---|---|---|
| Dashboard | Sí | Sí |
| Consultar y filtrar productos | Sí | Sí |
| Registrar, editar, eliminar productos | Sí | No |
| Gestionar categorías, clientes y usuarios | Sí | No |
| Consultar inventario y actualizar cantidad | Sí | Sí |
| Modificar stock mínimo | Sí | No |

Spring Security protege las rutas y los servicios de escritura. Los botones reflejan esos permisos. Se bloquea el inicio de sesión de cuentas inactivas y se protege al último administrador activo. Los cambios de rol o estado se aplican en la siguiente solicitud de una sesión existente.

## Reglas de datos

- Un producto pertenece a una categoría y tiene un único inventario, creado por el servicio al registrarlo.
- Producto e inventario se actualizan en la misma transacción, bloqueando primero el producto y después su registro de inventario. Usar los formularios de la aplicación para modificar existencias; editar tablas manualmente omite estas reglas.
- El stock mínimo inicial es 10. Stock bajo significa cantidad estrictamente menor al mínimo; cero es una cantidad válida. Disponibilidad significa stock mayor que cero, independientemente de si el producto está activo.
- Los filtros se combinan y aceptan límites de precio inclusivos. Los rangos invertidos muestran un error.
- Una categoría con productos no puede eliminarse. Al eliminar un producto se elimina también su inventario.
- Correo de usuario, nombre de categoría y documento de cliente son únicos. Los precios usan decimal de dos posiciones; no se aceptan valores negativos.
- El dashboard cuenta todos los productos, categorías y clientes, incluidos los inactivos, y calcula sus valores desde la base en cada consulta.

## 6. Guion de demostración

1. Entrar como administrador y observar las cuatro tarjetas del dashboard.
2. Crear una categoría y un producto, con stock 5 e imagen; comprobar que aparece en inventario con mínimo 10 y alerta STOCK BAJO.
3. Editar el producto, buscarlo por nombre y combinar categoría, disponibilidad y precio.
4. Cambiar inventario a 15; comprobar el stock del producto y la desaparición de su alerta.
5. Intentar eliminar la categoría mientras tiene productos y comprobar el aviso.
6. Registrar, editar y eliminar un cliente; intentar duplicar su documento.
7. Crear un usuario empleado y desactivarlo. Comprobar que no puede iniciar sesión. Intentar desactivar al último administrador.
8. Entrar como empleado: consultar productos y actualizar inventario. Acceder directamente a `/usuarios` y `/productos/nuevo` para comprobar el rechazo del servidor.
9. Reiniciar la aplicación y comprobar que permanecen los registros. Eliminar el producto y después su categoría de prueba.

## Pruebas

`mvnw.cmd test` ejecuta la suite con H2 en memoria, **solo en el perfil de pruebas**. H2 no se usa en la aplicación ni simula sus pantallas. Las pruebas incluyen renderizado Thymeleaf, permisos, CSRF, validaciones, filtros, CRUD, BCrypt, archivos e inventario.

Para ejecutar la misma suite contra **una base MySQL vacía y exclusiva de pruebas**:

```sql
CREATE DATABASE petcare_test CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

```powershell
$env:TEST_DB_URL='jdbc:mysql://localhost:3306/petcare_test?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=America/Lima'
$env:TEST_DB_USERNAME='root'
$env:TEST_DB_PASSWORD='tu_clave'
$env:TEST_DB_DRIVER='com.mysql.cj.jdbc.Driver'
.\mvnw.cmd test
```

El perfil usa `create-drop`: crea y elimina las tablas de la base de pruebas. **Nunca apuntar TEST_DB_URL a petcare_db ni a una base con información que deba conservarse.** Quitar estas variables al volver a las pruebas con H2.

Consultar `VERIFICACION.md` para los resultados realmente obtenidos en este entorno.

## Capturas

Las capturas de escritorio y móvil están en `docs/capturas`. Se obtuvieron de la aplicación conectada a MySQL.

## Solución de problemas

- **Unsupported class version / release 17:** corregir JDK de proyecto, Maven Runner y JAVA_HOME.
- **Access denied / Communications link failure:** comprobar servicio MySQL, puerto, usuario, contraseña y DB_URL.
- **Schema validation / missing table:** ejecutar el SQL completo en la misma base indicada por DB_URL.
- **Puerto 8081 ocupado:** detener la otra aplicación antes de iniciar PetCare Store.
- **No se guarda una imagen:** comprobar permisos de PETCARE_UPLOAD_DIR y límites del archivo.
- **403:** comprobar rol y sesión; los formularios deben enviarse desde la aplicación para incluir CSRF.

Bootstrap se distribuye bajo licencia MIT; se conserva su encabezado de licencia en los archivos locales.

Si este entorno Windows muestra `Unable to establish loopback connection` al iniciar Tomcat, la verificación utilizó la opción de JVM `-Djdk.net.unixdomain.tmpdir=./.tools/no-sockets` (una ruta inexistente), que permite a Java utilizar TCP para su comunicación interna. Es una particularidad del entorno de ejecución, no un cambio del esquema ni de la seguridad de la aplicación. Puede añadirse en **Run Configuration > VM options** si el mismo error se reproduce.
