# Acceso al Servidor

## Datos del grupo 27

- Host actual del servidor: `web.tecnoweb.org.bo`
- Usuario SSH: `grupo27sa`
- Puerto SSH: `22`
- Ruta del proyecto en el servidor: `/home/grupo27sa/F.I.C.C.T.-Primer-Parcial-INF513`

Nota: el host escrito como `wwww.tecnoweb.org.bo` no resolvio desde este servidor. El host confirmado por `hostname` es `web.tecnoweb.org.bo`.

## Verificaciones realizadas

- La sesion actual ya esta dentro del servidor `web.tecnoweb.org.bo`.
- El usuario actual es `grupo27sa`.
- La aplicacion Java ya fue generada como `dist/ProyectoEmail.jar`.
- POP3 local en `localhost:110` responde con Dovecot.
- La autenticacion POP3 con `grupo27sa` funciona usando la contrasena del grupo 27 configurada en `.env`.

## Uso desde terminal

Desde una maquina externa:

```bash
ssh grupo27sa@web.tecnoweb.org.bo -p 22
```

Luego ingresar la contrasena del grupo 27 cuando SSH la solicite.

## Ejecutar la aplicacion

Dentro del proyecto:

```bash
set -a
source .env
set +a
java -jar dist/ProyectoEmail.jar
```

