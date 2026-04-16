#!/usr/bin/env python3
import math
import os
import random
import subprocess
import tempfile
from datetime import datetime, timedelta


DB_HOST = os.environ.get("GREENHOUSE_DB_HOST", "127.0.0.1")
DB_PORT = os.environ.get("GREENHOUSE_DB_PORT", "3306")
DB_USER = os.environ.get("GREENHOUSE_DB_USER", "root")
DB_PASSWORD = os.environ.get("GREENHOUSE_DB_PASSWORD", "123456")
DB_NAME = os.environ.get("GREENHOUSE_DB_NAME", "greenhouse")

DEVICE_COUNT = 20
DAYS = 30
INTERVAL_HOURS = 2
RANDOM_SEED = 20260416


def esc(value):
    return str(value).replace("\\", "\\\\").replace("'", "''")


def ts(dt):
    return dt.strftime("%Y-%m-%d %H:%M:%S")


def build_sql():
    random.seed(RANDOM_SEED)
    now = datetime.now().replace(minute=0, second=0, microsecond=0)
    start = now - timedelta(days=DAYS)

    lines = []
    lines.append("SET NAMES utf8mb4;")
    lines.append("CREATE DATABASE IF NOT EXISTS greenhouse DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;")
    lines.append("USE greenhouse;")
    lines.append("START TRANSACTION;")
    lines.append("DELETE FROM sensor_data WHERE device_id LIKE 'gh-esp32-%';")
    lines.append("DELETE FROM control_command WHERE device_id LIKE 'gh-esp32-%';")
    lines.append("DELETE FROM alarm_record WHERE device_id LIKE 'gh-esp32-%';")
    lines.append("DELETE FROM control_rule WHERE device_id LIKE 'gh-esp32-%';")
    lines.append("DELETE FROM device WHERE device_id LIKE 'gh-esp32-%';")

    for idx in range(1, DEVICE_COUNT + 1):
      device_id = "gh-esp32-%02d" % idx
      greenhouse_name = "%d号实验温室" % (((idx - 1) // 5) + 1)
      control_mode = "AUTO" if idx % 3 else "MANUAL"
      last_heartbeat = now - timedelta(minutes=idx * 3)
      online_status = 0 if idx in (4, 9, 17) else 1
      lines.append(
          "INSERT INTO device (device_id, name, greenhouse_name, control_mode, online_status, last_heartbeat) "
          "VALUES ('%s', '%s', '%s', '%s', %d, '%s');"
          % (
              device_id,
              esc("ESP32节点%02d" % idx),
              esc(greenhouse_name),
              control_mode,
              online_status,
              ts(last_heartbeat),
          )
      )

      fan_threshold = 29.0 + (idx % 3)
      pump_threshold = 28.0 + (idx % 4)
      lines.append(
          "INSERT INTO control_rule "
          "(rule_name, device_id, metric_type, operator, threshold_value, action_type, action_payload, enabled, cooldown_seconds) "
          "VALUES ('%s', '%s', 'temperature', '>', %.1f, 'FAN_ON', JSON_OBJECT('action', 'ON', 'target', 'fan'), 1, 120);"
          % (esc("高温启动风机-%02d" % idx), device_id, fan_threshold)
      )
      lines.append(
          "INSERT INTO control_rule "
          "(rule_name, device_id, metric_type, operator, threshold_value, action_type, action_payload, enabled, cooldown_seconds) "
          "VALUES ('%s', '%s', 'soil_moisture', '<', %.1f, 'PUMP_ON', JSON_OBJECT('action', 'ON', 'target', 'pump'), 1, 180);"
          % (esc("土壤过干启动水泵-%02d" % idx), device_id, pump_threshold)
      )

      hours = 0
      point_index = 0
      while hours <= DAYS * 24:
        collect_time = start + timedelta(hours=hours)
        day_ratio = float(point_index) / float((DAYS * 24 // INTERVAL_HOURS) + 1)
        season = math.sin(day_ratio * math.pi * 2.0)
        intra_day = math.sin((collect_time.hour / 24.0) * math.pi * 2.0)

        temperature = 24.0 + idx * 0.18 + season * 3.8 + intra_day * 4.5 + random.uniform(-0.8, 0.8)
        humidity = 63.0 - intra_day * 8.5 - season * 4.5 + random.uniform(-2.0, 2.0)
        soil = 36.0 - (point_index % 14) * 0.6 + season * 2.2 + random.uniform(-1.2, 1.2)

        temperature = max(15.0, min(39.0, temperature))
        humidity = max(28.0, min(92.0, humidity))
        soil = max(12.0, min(58.0, soil))

        lines.append(
            "INSERT INTO sensor_data (device_id, metric_type, metric_value, collect_time) VALUES "
            "('%s', 'temperature', %.3f, '%s'), "
            "('%s', 'humidity', %.3f, '%s'), "
            "('%s', 'soil_moisture', %.3f, '%s');"
            % (
                device_id, temperature, ts(collect_time),
                device_id, humidity, ts(collect_time),
                device_id, soil, ts(collect_time),
            )
        )

        if point_index % 48 == 10:
          command_time = collect_time + timedelta(minutes=5)
          lines.append(
              "INSERT INTO control_command (device_id, command_type, command_payload, status, sent_time, ack_time, source) "
              "VALUES ('%s', 'FAN_ON', JSON_OBJECT('action', 'ON', 'target', 'fan'), 'ACKED', '%s', '%s', 'RULE:高温联动');"
              % (device_id, ts(command_time), ts(command_time + timedelta(seconds=12)))
          )
        if point_index % 72 == 20:
          command_time = collect_time + timedelta(minutes=8)
          lines.append(
              "INSERT INTO control_command (device_id, command_type, command_payload, status, sent_time, ack_time, source) "
              "VALUES ('%s', 'PUMP_ON', JSON_OBJECT('action', 'ON', 'target', 'pump'), 'ACKED', '%s', '%s', 'MANUAL');"
              % (device_id, ts(command_time), ts(command_time + timedelta(seconds=10)))
          )
        if point_index in (30, 160):
          alarm_time = collect_time + timedelta(minutes=3)
          trigger_value = temperature if point_index == 30 else soil
          threshold = 31.0 if point_index == 30 else 25.0
          alarm_type = "TEMP_HIGH" if point_index == 30 else "SOIL_LOW"
          message = "温度超过阈值" if point_index == 30 else "土壤湿度低于阈值"
          lines.append(
              "INSERT INTO alarm_record (device_id, alarm_type, alarm_level, trigger_value, threshold_value, status, message, triggered_at, resolved_at) "
              "VALUES ('%s', '%s', 'WARN', %.3f, %.3f, 'RESOLVED', '%s', '%s', '%s');"
              % (
                  device_id,
                  alarm_type,
                  trigger_value,
                  threshold,
                  esc(message),
                  ts(alarm_time),
                  ts(alarm_time + timedelta(minutes=20)),
              )
          )

        hours += INTERVAL_HOURS
        point_index += 1

    lines.append("COMMIT;")
    return "\n".join(lines) + "\n"


def main():
    sql = build_sql()
    with tempfile.NamedTemporaryFile("w", suffix=".sql", delete=False, encoding="utf-8") as handle:
        handle.write(sql)
        temp_path = handle.name

    try:
        command = [
            "mysql",
            "--default-character-set=utf8mb4",
            "-h%s" % DB_HOST,
            "-P%s" % DB_PORT,
            "-u%s" % DB_USER,
            "-p%s" % DB_PASSWORD,
            DB_NAME,
        ]
        with open(temp_path, "r", encoding="utf-8") as sql_file:
            subprocess.check_call(command, stdin=sql_file)
    finally:
        if os.path.exists(temp_path):
            os.remove(temp_path)


if __name__ == "__main__":
    main()
