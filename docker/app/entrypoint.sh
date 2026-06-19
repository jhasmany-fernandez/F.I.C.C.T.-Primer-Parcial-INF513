#!/usr/bin/env sh
set -eu

echo "Iniciando ProyectoEmail"
echo "DB: ${PROYECTOEMAIL_DB_HOST:-unset}:${PROYECTOEMAIL_DB_PORT:-unset}/${PROYECTOEMAIL_DB_NAME:-unset}"
echo "POP3: ${PROYECTOEMAIL_POP3_HOST:-unset}:${PROYECTOEMAIL_POP3_PORT:-unset}"
echo "SMTP: ${PROYECTOEMAIL_SMTP_HOST:-unset}:${PROYECTOEMAIL_SMTP_PORT:-unset}"

exec java -cp "/app/ProyectoEmail.jar:/app/lib/postgresql-42.7.7.jar" proyectoemail.ProyectoEmail
