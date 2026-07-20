#!/bin/bash
set -e

git pull
docker compose -f docker-compose.prod.yml build
docker compose -f docker-compose.prod.yml -f docker-compose.monitoring.yml up -d
