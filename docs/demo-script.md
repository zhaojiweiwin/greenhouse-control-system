# Defense Demo Script

## 1. Startup

1. Start base services: `docker compose up -d mysql mqtt`
2. Run backend: `cd backend && mvn spring-boot:run`
3. Run frontend: `cd frontend && npm install && npm run dev`
4. Simulate device data: `python3 scripts/simulate_device.py`

## 2. Live Dashboard

1. Open `http://localhost:5173`.
2. Show overview cards, trend lists, latest telemetry, and alarm panel refreshing every 5 seconds.
3. Explain metric meaning: temperature, humidity, soil moisture.

## 3. Manual Control

1. Open "设备控制" page.
2. Send command `FAN_ON` to `gh-esp32-01`.
3. Show backend command API response, frontend command log, and simulator terminal command receipt.

## 4. Auto Strategy Validation

1. Open "策略管理" page and show the preset threshold rules.
2. Run: `python3 scripts/validate_strategy.py`
3. Explain threshold rule:
   - temperature > 30 -> fan on
   - soil_moisture < 30 -> pump on
4. Relate rule actions with command pipeline.

## 5. Q&A Ready Points

- Why ESP32: lower complexity and stable demo chain.
- Why MQTT: lightweight and IoT standard protocol.
- Why schema/index: supports recent dashboards and history queries.
