#!/usr/bin/env sh
set -eu

cd "$(dirname "$0")/.."
if [ ! -f .env ]; then
  password="$(openssl rand -base64 32 | tr '+/' '_-' | tr -d '\n')"
  printf 'DB_PASSWORD=%s\n' "$password" > .env
  chmod 600 .env
  echo "Created .env with a generated local database password."
fi

docker compose --env-file .env up --build -d --wait
docker compose --env-file .env ps
