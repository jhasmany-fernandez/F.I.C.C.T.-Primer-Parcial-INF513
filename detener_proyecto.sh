#!/usr/bin/env bash
set -euo pipefail

PROCESS_PATTERN="proyectoemail.ProyectoEmail"
TIMEOUT_SECONDS=10

mapfile -t PIDS < <(pgrep -f "$PROCESS_PATTERN" || true)

if [[ ${#PIDS[@]} -eq 0 ]]; then
    echo "ProyectoEmail no esta en ejecucion."
    exit 0
fi

echo "Deteniendo ProyectoEmail..."
for PID in "${PIDS[@]}"; do
    if [[ "$PID" != "$$" ]]; then
        echo "Enviando SIGTERM al proceso $PID"
        kill "$PID" 2>/dev/null || true
    fi
done

for _ in $(seq 1 "$TIMEOUT_SECONDS"); do
    RUNNING=false
    for PID in "${PIDS[@]}"; do
        if [[ "$PID" != "$$" ]] && kill -0 "$PID" 2>/dev/null; then
            RUNNING=true
            break
        fi
    done

    if [[ "$RUNNING" == false ]]; then
        echo "ProyectoEmail detenido correctamente."
        exit 0
    fi

    sleep 1
done

echo "Algunos procesos no terminaron. Forzando cierre..."
for PID in "${PIDS[@]}"; do
    if [[ "$PID" != "$$" ]] && kill -0 "$PID" 2>/dev/null; then
        echo "Enviando SIGKILL al proceso $PID"
        kill -9 "$PID" 2>/dev/null || true
    fi
done

echo "ProyectoEmail detenido."
