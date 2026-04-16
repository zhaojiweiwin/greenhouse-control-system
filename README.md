# Greenhouse Control System Skeleton

Graduation-project-oriented skeleton covering telemetry ingestion, command dispatch, realtime dashboard, and demo scripts.

## Structure

- `docs/`: architecture decisions and demo docs
- `db/`: MySQL schema
- `backend/`: Spring Boot REST + MQTT + MySQL skeleton
- `frontend/`: Vue 3 dashboard and control pages
- `scripts/`: simulation and strategy validation helpers

## Quick Start

1. Start dependencies:
   - `docker compose up -d mysql mqtt`
2. Initialize backend:
   - `cd backend && mvn spring-boot:run`
3. Initialize frontend:
   - `cd frontend && npm install && npm run dev`
4. Simulate device telemetry:
   - `python3 scripts/simulate_device.py`

## Implemented TODO Mapping

- `confirm-hardware-stack`: `docs/hardware-stack.md`
- `define-db-schema`: `db/schema.sql`, `docs/db-design.md`
- `build-backend-skeleton`: `backend/` project scaffold
- `build-frontend-skeleton`: `frontend/` project scaffold
- `integrate-and-demo`: `docker-compose.yml`, `scripts/`, `docs/demo-script.md`
