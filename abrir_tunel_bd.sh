#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$ROOT_DIR"

if [[ -f ".env" ]]; then
    set -a
    # shellcheck disable=SC1091
    source .env
    set +a
fi

LOCAL_PORT="${PROYECTOEMAIL_DB_PORT:-15432}"
LOCAL_BIND_HOST="${PROYECTOEMAIL_DB_TUNNEL_BIND_HOST:-127.0.0.1}"
REMOTE_HOST="${PROYECTOEMAIL_REMOTE_DB_HOST:-127.0.0.1}"
REMOTE_PORT="${PROYECTOEMAIL_REMOTE_DB_PORT:-5432}"
SSH_HOST="${PROYECTOEMAIL_SSH_HOST:-tecnoweb.org.bo}"
SSH_PORT="${PROYECTOEMAIL_SSH_PORT:-22}"
SSH_USER="${PROYECTOEMAIL_SSH_USER:-grupo27sa}"

echo "Abriendo tunel SSH para PostgreSQL..."
echo "Local : ${LOCAL_BIND_HOST}:${LOCAL_PORT}"
echo "Remoto: ${REMOTE_HOST}:${REMOTE_PORT}"
echo "SSH   : ${SSH_USER}@${SSH_HOST}:${SSH_PORT}"
echo "Para cerrar el tunel presiona Ctrl+C."

SSH_ARGS=()
if [[ "${LOCAL_BIND_HOST}" != "127.0.0.1" && "${LOCAL_BIND_HOST}" != "localhost" ]]; then
    SSH_ARGS+=("-g")
fi

exec ssh -N \
    "${SSH_ARGS[@]}" \
    -L "${LOCAL_BIND_HOST}:${LOCAL_PORT}:${REMOTE_HOST}:${REMOTE_PORT}" \
    -p "${SSH_PORT}" \
    "${SSH_USER}@${SSH_HOST}"
