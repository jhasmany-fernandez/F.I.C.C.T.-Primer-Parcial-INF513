# Imagen base con Java 21 y Maven
FROM mcr.microsoft.com/devcontainers/java:21-bullseye

# Instala cualquier herramienta adicional si deseas (ej. PostgreSQL client, git, etc.)
RUN apt-get update && apt-get install -y postgresql-client

# Instala Maven
RUN apt update && apt install -y maven

# Configura carpeta de trabajo
WORKDIR /workspace
