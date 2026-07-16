#!/bin/bash
set -e

git pull
docker compose -f docker-compose.prod.yml build
docker compose -f docker-compose.prod.yml up -d
