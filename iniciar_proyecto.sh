#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$ROOT_DIR"

CLASSES_DIR="out/classes"
POSTGRES_JAR="lib/postgresql-42.7.7.jar"
MAIN_CLASS="proyectoemail.ProyectoEmail"

if [[ ! -f "$POSTGRES_JAR" ]]; then
    echo "Error: no se encontro $POSTGRES_JAR"
    echo "Agrega el driver JDBC de PostgreSQL en la carpeta lib/."
    exit 1
fi

echo "Compilando proyecto..."
mkdir -p "$CLASSES_DIR"
javac -cp "$POSTGRES_JAR" -d "$CLASSES_DIR" $(find src -name '*.java')

if [[ -f ".env" ]]; then
    echo "Cargando variables desde .env..."
    set -a
    # shellcheck disable=SC1091
    source .env
    set +a
else
    echo "Aviso: no existe .env. Se usaran variables del sistema o valores por defecto."
fi

echo "Iniciando ProyectoEmail..."
echo "Para detener la aplicacion presiona Ctrl+C."
java -cp "$CLASSES_DIR:$POSTGRES_JAR" "$MAIN_CLASS"
