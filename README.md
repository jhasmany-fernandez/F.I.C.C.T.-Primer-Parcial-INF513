# ProyectoEmail

Proyecto Java por correo (POP3/SMTP + hilos + capas propias), sin Spring Boot.

## Que es este proyecto

Es un backend que se ejecuta en segundo plano y procesa comandos recibidos por correo.

- Lee correos por POP3.
- Interpreta el asunto del correo como comando.
- Ejecuta operaciones de negocio (usuarios, casos, mapas, reportes, etc.).
- Responde por SMTP.

No tiene interfaz grafica web ni desktop. Se opera por correo y por logs.

## Requisitos

- JDK 25 (`javac 25.0.2` o compatible).
- Docker Desktop.
- PostgreSQL accesible en `localhost:5432` (segun `src/Conexion/DBConnection.java`):
  - DB: `tecnito`
  - USER: `postgres`
  - PASS: `toor`
- Librerias en `lib/`:
  - `postgresql-42.2.2.jre8.jar`
  - `jbcrypt.jar`
  - `Interpreter.jar`
  - `javax.mail.jar`

## Servidor de correo local (GreenMail)

Levantar GreenMail:

```bash
docker compose -f docker/docker-compose.mail.yml up -d
```

Verificar:

```bash
docker ps
```

Debe exponer:

- SMTP: `localhost:3025`
- POP3: `localhost:3110`

Apagar GreenMail:

```bash
docker compose -f docker/docker-compose.mail.yml down
```

## Docker completo

El repositorio ahora incluye un `docker-compose.yml` principal para levantar:

- `postgres`
- `greenmail`
- `app`

Antes de usarlo, coloca las dependencias locales faltantes en `lib/`:

- `Interpreter.jar` obligatorio
- cualquier otro `.jar` propio que no este publicado en Maven

Copiar variables de entorno:

```bash
cp .env.example .env
```

Levantar todo:

```bash
docker compose up --build
```

Notas:

- El contenedor `app` descarga durante el build las dependencias publicas de PostgreSQL, jBCrypt y JavaMail.
- `Interpreter.jar` no se puede reconstruir desde este repositorio, por eso debe existir en `./lib/Interpreter.jar`.
- La conexion a PostgreSQL ahora soporta variables de entorno `PROYECTOEMAIL_DB_*`.

## Credenciales locales de correo

Credenciales validadas para POP3 en entorno local:

- `PROYECTOEMAIL_POP3_USER=grupo13sc`
- `PROYECTOEMAIL_POP3_PASSWORD=123456`

Remitente SMTP configurado:

- `PROYECTOEMAIL_SMTP_FROM=grupo13sc@tecnoweb.org.bo`

## Ejecutar la aplicacion (modo terminal)

Compilar desde fuentes:

```bash
javac -cp "lib/*" -d out/classes src/**/*.java
```

En PowerShell, una forma compatible es:

```powershell
$srcFiles = Get-ChildItem -Path src -Recurse -Filter *.java | ForEach-Object { $_.FullName }
javac -cp "lib/*" -d out/classes $srcFiles
```

Iniciar la app con variables de entorno:

```powershell
Start-Process -FilePath "C:\Program Files\Java\jdk-25\bin\java.exe" `
  -ArgumentList '-cp','out/classes;lib/*','proyectoemail.ProyectoEmail' `
  -WorkingDirectory "d:\Downloads\ProyectoEmail\ProyectoEmail" `
  -RedirectStandardOutput "logs/server.out.log" `
  -RedirectStandardError "logs/server.err.log" `
  -Environment @{
    PROYECTOEMAIL_POP3_HOST='localhost'
    PROYECTOEMAIL_POP3_PORT='3110'
    PROYECTOEMAIL_POP3_USER='grupo13sc'
    PROYECTOEMAIL_POP3_PASSWORD='123456'
    PROYECTOEMAIL_SMTP_HOST='localhost'
    PROYECTOEMAIL_SMTP_PORT='3025'
    PROYECTOEMAIL_SMTP_FROM='grupo13sc@tecnoweb.org.bo'
  }
```

## Ejecutar la aplicacion en IntelliJ IDEA

1. Abrir IntelliJ IDEA y seleccionar `Open`, luego la carpeta:
   - `d:\Downloads\ProyectoEmail\ProyectoEmail`

2. Configurar el JDK del proyecto:
   - `File > Project Structure > Project`
   - `Project SDK = JDK 25`

3. Agregar dependencias JAR del proyecto:
   - `File > Project Structure > Modules > Dependencies > + > JARs or directories`
   - Seleccionar todos los `.jar` de `lib/`.

4. Crear una configuracion de ejecucion:
   - `Run > Edit Configurations > + > Application`
   - `Main class`: `proyectoemail.ProyectoEmail`
   - `Use classpath of module`: modulo del proyecto
   - `Working directory`: `d:\Downloads\ProyectoEmail\ProyectoEmail`

5. Agregar variables de entorno en esa configuracion:
   - `PROYECTOEMAIL_POP3_HOST=localhost`
   - `PROYECTOEMAIL_POP3_PORT=3110`
   - `PROYECTOEMAIL_POP3_USER=grupo13sc`
   - `PROYECTOEMAIL_POP3_PASSWORD=123456`
   - `PROYECTOEMAIL_SMTP_HOST=localhost`
   - `PROYECTOEMAIL_SMTP_PORT=3025`
   - `PROYECTOEMAIL_SMTP_FROM=grupo13sc@tecnoweb.org.bo`

6. Levantar GreenMail antes de ejecutar:

```bash
docker compose -f docker/docker-compose.mail.yml up -d
```

7. Verificar PostgreSQL en `localhost:5432` con estos datos:
   - DB: `tecnito`
   - USER: `postgres`
   - PASS: `toor`

8. Ejecutar desde IntelliJ (`Run`).

## Logs y verificacion

Archivos:

- `logs/server.out.log`
- `logs/server.err.log`

Ver en vivo:

```powershell
Get-Content logs/server.out.log -Wait
```

Salida esperada cuando esta bien:

- `POP3 CONFIG HOST: localhost:3110`
- `POP3 USER RESPONSE: +OK`
- `POP3 PASS RESPONSE: +OK`

## Detener la aplicacion

Buscar PID:

```powershell
Get-CimInstance Win32_Process -Filter "name='java.exe'" | Where-Object { $_.CommandLine -like '*proyectoemail.ProyectoEmail*' } | Select-Object ProcessId,CommandLine
```

Detener:

```powershell
Stop-Process -Id <PID> -Force
```
