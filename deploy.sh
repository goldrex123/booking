#!/bin/bash
set -e

docker compose -f docker-compose.prod.yml build
docker compose -f docker-compose.prod.yml -f docker-compose.monitoring.yml up -d
