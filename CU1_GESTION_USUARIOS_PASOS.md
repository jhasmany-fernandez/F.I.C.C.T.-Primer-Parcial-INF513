# CU1 Gestion de Usuarios - Pasos

Este documento resume los pasos para ejecutar y probar el caso de uso CU1 Gestion de Usuarios en el proyecto de correo.

## 1. Preparar la base de datos

Ejecutar el script SQL del gimnasio en PostgreSQL:

```bash
psql "host=localhost port=5432 user=grupo27sa dbname=db_grupo27sa" \
  -f sql/001_gimnasio_schema.sql
```

El script crea las tablas necesarias y registra los roles iniciales:

- Propietario
- Secretaria
- Instructor
- Cliente

## 2. Verificar variables de entorno

Configurar `.env` con los datos de PostgreSQL y correo:

```env
PROYECTOEMAIL_DB_HOST=localhost
PROYECTOEMAIL_DB_PORT=5432
PROYECTOEMAIL_DB_NAME=db_grupo27sa
PROYECTOEMAIL_DB_USER=grupo27sa
PROYECTOEMAIL_DB_PASSWORD=tu_contrasena

PROYECTOEMAIL_POP3_HOST=localhost
PROYECTOEMAIL_POP3_PORT=110
PROYECTOEMAIL_POP3_USER=grupo27sa
PROYECTOEMAIL_POP3_PASSWORD=tu_contrasena

PROYECTOEMAIL_SMTP_HOST=localhost
PROYECTOEMAIL_SMTP_PORT=25
PROYECTOEMAIL_SMTP_FROM=grupo27sa@tecnoweb.org.bo
```

## 3. Crear un usuario Propietario inicial

CU1 solo permite gestionar usuarios si el correo remitente pertenece a un usuario activo con rol `Propietario`.

Si la tabla `usuario` esta vacia, insertar manualmente el primer propietario:

```sql
INSERT INTO usuario (id_rol, nombre, email, contrasena, estado)
SELECT id_rol, 'Propietario Inicial', 'correo_propietario@ejemplo.com', 'admin123', 'ACTIVO'
FROM rol
WHERE nombre_rol = 'Propietario';
```

Importante: el correo usado aqui debe ser el mismo desde el que se enviaran los comandos.

## 4. Compilar el proyecto

Desde la raiz del proyecto:

```bash
mkdir -p out/classes
javac -cp "lib/postgresql-42.7.7.jar" -d out/classes $(find src -name '*.java')
```

## 5. Levantar la aplicacion

Cargar variables y ejecutar:

```bash
set -a
source .env
set +a
java -cp "out/classes:lib/postgresql-42.7.7.jar" proyectoemail.ProyectoEmail
```

La aplicacion revisa la bandeja POP3 cada 10 segundos.

## 6. Enviar comandos por correo

Enviar un correo a la cuenta configurada en POP3. El comando va en el asunto del correo.

Formato:

```text
usuario accion [parametro1; parametro2; parametro3]
```

## 7. Comandos disponibles

### Agregar usuario

```text
usuario agregar [nombre; email; contrasena; nombre_rol]
```

Ejemplo:

```text
usuario agregar [Juan Perez; juan@mail.com; secreto123; Cliente]
```

### Modificar usuario

```text
usuario modificar [id_usuario; nombre; email; contrasena; nombre_rol]
```

Ejemplo:

```text
usuario modificar [5; Ana Lopez; ana@mail.com; nuevoPass; Instructor]
```

### Eliminar usuario

Este comando realiza baja logica: cambia el estado a `INACTIVO`.

```text
usuario eliminar [id_usuario]
```

Ejemplo:

```text
usuario eliminar [5]
```

### Ver usuario

```text
usuario ver [id_usuario]
```

Ejemplo:

```text
usuario ver [10]
```

### Mostrar usuarios

```text
usuario mostrar
```

### Ayuda de usuarios

```text
usuario ayuda
```

## 8. Validar la respuesta

La aplicacion responde por SMTP al correo remitente.

Resultados esperados:

- Si el remitente es Propietario: ejecuta el comando y responde con exito o tabla HTML.
- Si el remitente no es Propietario: responde `Acceso denegado`.
- Si faltan parametros: responde indicando el formato correcto.
- Si el rol no existe: responde `No existe el rol`.

## 9. Archivos relacionados

- `src/proyectoemail/MailAplication.java`: recibe y enruta el comando `usuario`.
- `src/Metodos/Usuarios.java`: ejecuta acciones del CU1 y envia respuestas.
- `src/Negocio/NUsuario.java`: valida parametros y reglas de negocio.
- `src/Datos/DUsuario.java`: ejecuta consultas SQL.
- `sql/001_gimnasio_schema.sql`: crea tablas y roles base.

