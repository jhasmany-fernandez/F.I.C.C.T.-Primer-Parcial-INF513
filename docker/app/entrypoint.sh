#!/usr/bin/env bash
set -euo pipefail

LOCAL_LIB_DIR="/app/local-lib"
BUILD_DIR="/app/out/classes"
CLASSPATH="/app/lib/*"

if [ -d "$LOCAL_LIB_DIR" ]; then
  CLASSPATH="$CLASSPATH:$LOCAL_LIB_DIR/*"
fi

if [ ! -f "$LOCAL_LIB_DIR/Interpreter.jar" ]; then
  echo "Falta lib/Interpreter.jar para compilar la aplicacion."
  echo "Coloca el archivo en ./lib/Interpreter.jar y vuelve a levantar docker compose."
  exit 1
fi

rm -rf "$BUILD_DIR"
mkdir -p "$BUILD_DIR"

find /app/src -name '*.java' > /tmp/java_sources.txt
javac -cp "$CLASSPATH" -d "$BUILD_DIR" @/tmp/java_sources.txt

exec java -cp "$BUILD_DIR:$CLASSPATH" proyectoemail.ProyectoEmail
