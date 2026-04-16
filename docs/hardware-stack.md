# Hardware Stack Decision

## Selected Option

- MCU: ESP32 (direct Wi-Fi connection)
- Network: ESP32 -> MQTT Broker (Mosquitto/EMQX)
- Protocol: MQTT with JSON payload
- Telemetry interval: 5-10 seconds
- Command delivery: immediate publish/subscribe

## Why ESP32 Direct Connect

- Faster implementation: no extra gateway service is required.
- Lower integration risk: one firmware node can publish telemetry and receive commands.
- Better demo reliability: fewer moving parts for graduation defense.
- Future-proof: MQTT topic and payload contract is identical to STM32+gateway path.

## Reserved Compatibility Path

- If later switching to STM32:
  - keep MQTT topics unchanged;
  - add lightweight edge gateway on Raspberry Pi/ESP32 bridge;
  - map serial/CAN data to same telemetry JSON format.

## MQTT Topic Contract

- `greenhouse/{deviceId}/telemetry`
- `greenhouse/{deviceId}/command`
- `greenhouse/{deviceId}/status`
- `greenhouse/{deviceId}/command_ack`

## Minimal Sensor/Actuator Set

- Sensors: temperature, humidity, soil_moisture
- Actuators: fan, pump

## Example Telemetry Payload

```json
{
  "deviceId": "gh-esp32-01",
  "timestamp": "2026-04-13T12:00:00Z",
  "metrics": {
    "temperature": 28.4,
    "humidity": 63.1,
    "soil_moisture": 35.2
  }
}
```
