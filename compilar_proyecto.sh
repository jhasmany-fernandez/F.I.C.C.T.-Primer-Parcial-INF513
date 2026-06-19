#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$ROOT_DIR"

CLASSES_DIR="out/classes"
DIST_DIR="dist"
JAR_FILE="$DIST_DIR/ProyectoEmail.jar"
MANIFEST_FILE="$DIST_DIR/ProyectoEmail.MF"
POSTGRES_JAR="lib/postgresql-42.7.7.jar"
SOURCES_FILE="tmp/sources.list"
MAIN_CLASS="proyectoemail.ProyectoEmail"

if ! command -v javac >/dev/null 2>&1; then
    echo "Error: no se encontro javac. Instala o carga JDK 25 antes de compilar."
    exit 1
fi

if ! command -v jar >/dev/null 2>&1; then
    echo "Error: no se encontro jar. Instala o carga JDK 25 antes de empaquetar."
    exit 1
fi

if [[ ! -f "$POSTGRES_JAR" ]]; then
    echo "Error: no se encontro $POSTGRES_JAR"
    echo "Agrega el driver JDBC de PostgreSQL en la carpeta lib/."
    exit 1
fi

echo "Preparando directorios..."
mkdir -p "$CLASSES_DIR" "$DIST_DIR" tmp
find "$CLASSES_DIR" -type f -name '*.class' -delete

echo "Buscando fuentes Java..."
find src -name '*.java' | sort > "$SOURCES_FILE"

if [[ ! -s "$SOURCES_FILE" ]]; then
    echo "Error: no se encontraron archivos .java en src/."
    exit 1
fi

echo "Compilando proyecto..."
javac -encoding UTF-8 -source 25 -target 25 -cp "$POSTGRES_JAR" -d "$CLASSES_DIR" @"$SOURCES_FILE"

echo "Generando manifiesto..."
{
    echo "Manifest-Version: 1.0"
    echo "Main-Class: $MAIN_CLASS"
    echo "Class-Path: ../$POSTGRES_JAR"
    echo
} > "$MANIFEST_FILE"

echo "Empaquetando $JAR_FILE..."
jar cfm "$JAR_FILE" "$MANIFEST_FILE" -C "$CLASSES_DIR" .

echo "Compilacion correcta."
echo "JAR generado: $JAR_FILE"
