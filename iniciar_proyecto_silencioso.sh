#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$ROOT_DIR"

CLASSES_DIR="out/classes"
POSTGRES_JAR="lib/postgresql-42.7.7.jar"
MAIN_CLASS="proyectoemail.ProyectoEmail"
LOG_DIR="logs"
OUT_LOG="$LOG_DIR/server.out.log"
ERR_LOG="$LOG_DIR/server.err.log"
PID_FILE="$LOG_DIR/server.pid"

if pgrep -f "$MAIN_CLASS" >/dev/null; then
    echo "ProyectoEmail ya esta en ejecucion."
    pgrep -af "$MAIN_CLASS"
    exit 0
fi

if [[ ! -f "$POSTGRES_JAR" ]]; then
    echo "Error: no se encontro $POSTGRES_JAR"
    echo "Agrega el driver JDBC de PostgreSQL en la carpeta lib/."
    exit 1
fi

echo "Compilando proyecto..."
mkdir -p "$CLASSES_DIR" "$LOG_DIR"
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

echo "Iniciando ProyectoEmail en segundo plano..."
nohup java -cp "$CLASSES_DIR:$POSTGRES_JAR" "$MAIN_CLASS" > "$OUT_LOG" 2> "$ERR_LOG" &
PID=$!
echo "$PID" > "$PID_FILE"

sleep 1
if kill -0 "$PID" 2>/dev/null; then
    echo "ProyectoEmail iniciado correctamente. PID: $PID"
    echo "Salida guardada en: $OUT_LOG"
    echo "Errores guardados en: $ERR_LOG"
    echo "Para detener: ./detener_proyecto.sh"
else
    echo "ProyectoEmail no pudo mantenerse en ejecucion. Revisa $ERR_LOG"
    exit 1
fi
