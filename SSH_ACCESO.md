# Acceso al Servidor

## Datos compartidos

- Host web: `https://tecnoweb.org.bo:9090/grupo17sa`
- Host SSH: `tecnoweb.org.bo`
- Usuario SSH: `grupo17sa`
- Contrasena del servidor segun la imagen: `grup017grup017*`
- Nota: `2307` corresponde a la maquina local Arch del usuario, no al servidor remoto.

## Verificaciones realizadas

- El host `tecnoweb.org.bo` responde por SSH.
- La URL `https://tecnoweb.org.bo:9090/grupo17sa` responde con `HTTP 200 OK`.
- La URL HTTPS presenta un problema de certificado TLS para ese hostname/puerto.
- La autenticacion SSH con `2307` fue rechazada.

## Configuracion local preparada

Se agrego esta entrada a `~/.ssh/config`:

```sshconfig
Host tecnoweb-grupo17
    HostName tecnoweb.org.bo
    User grupo17sa
    Port 22
    PreferredAuthentications password
    PubkeyAuthentication no
    ServerAliveInterval 30
    ServerAliveCountMax 3
```

## Uso desde terminal

```bash
ssh tecnoweb-grupo17
```

Cuando pida contrasena, usar:

```text
grup017grup017*
```

## Uso desde VS Code

1. Abrir VS Code.
2. Presionar `F1`.
3. Ejecutar `Remote-SSH: Connect to Host`.
4. Elegir `tecnoweb-grupo17`.
5. Ingresar la contrasena del servidor cuando la solicite.

## Contexto de la imagen

- Se indico ingresar por el enlace `https://tecnoweb.org.bo:9090/grupo17sa`.
- Se menciono que dentro de la carpeta `proyecto2` se encuentra el contenido a subir o revisar en el servidor.
