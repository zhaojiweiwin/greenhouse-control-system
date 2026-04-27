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

# gh-esp32-01..N：0=空气温度传感器（同时上报温度+空气湿度）、1=土壤湿度传感器、2=光照传感器
SENSOR_DEVICE_NAMES = ("空气温度传感器", "土壤湿度传感器", "光照传感器")
SENSOR_KIND_TEMP = 0
SENSOR_KIND_SOIL = 1
SENSOR_KIND_LIGHT = 2


def device_sensor_kind(idx):
    return (idx - 1) % 3


def device_display_name(idx):
    zero = idx - 1
    kind = zero % 3
    serial = zero // 3 + 1
    return "%s-%02d" % (SENSOR_DEVICE_NAMES[kind], serial)


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
        kind = device_sensor_kind(idx)
        lines.append(
            "INSERT INTO device (device_id, name, greenhouse_name, control_mode, online_status, last_heartbeat) "
            "VALUES ('%s', '%s', '%s', '%s', %d, '%s');"
            % (
                device_id,
                esc(device_display_name(idx)),
                esc(greenhouse_name),
                control_mode,
                online_status,
                ts(last_heartbeat),
            )
        )

        fan_threshold = 29.0 + (idx % 3)
        pump_threshold = 28.0 + (idx % 4)
        light_threshold = 3200.0
        if kind == SENSOR_KIND_TEMP:
            lines.append(
                "INSERT INTO control_rule "
                "(rule_name, device_id, metric_type, operator, threshold_value, action_type, action_payload, enabled, cooldown_seconds) "
                "VALUES ('%s', '%s', 'temperature', '>', %.1f, 'FAN_ON', JSON_OBJECT('action', 'ON', 'target', 'fan'), 1, 120);"
                % (esc("高温启动风机-%02d" % idx), device_id, fan_threshold)
            )
        elif kind == SENSOR_KIND_SOIL:
            lines.append(
                "INSERT INTO control_rule "
                "(rule_name, device_id, metric_type, operator, threshold_value, action_type, action_payload, enabled, cooldown_seconds) "
                "VALUES ('%s', '%s', 'soil_moisture', '<', %.1f, 'PUMP_ON', JSON_OBJECT('action', 'ON', 'target', 'pump'), 1, 180);"
                % (esc("土壤过干启动水泵-%02d" % idx), device_id, pump_threshold)
            )
        else:
            lines.append(
                "INSERT INTO control_rule "
                "(rule_name, device_id, metric_type, operator, threshold_value, action_type, action_payload, enabled, cooldown_seconds) "
                "VALUES ('%s', '%s', 'illuminance', '<', %.1f, 'LIGHT_ON', JSON_OBJECT('action', 'ON', 'target', 'grow_light'), 1, 240);"
                % (esc("光照不足补光-%02d" % idx), device_id, light_threshold)
            )

        hours = 0
        point_index = 0
        while hours <= DAYS * 24:
            collect_time = start + timedelta(hours=hours)
            day_ratio = float(point_index) / float((DAYS * 24 // INTERVAL_HOURS) + 1)
            season = math.sin(day_ratio * math.pi * 2.0)
            intra_day = math.sin((collect_time.hour / 24.0) * math.pi * 2.0)

            if kind == SENSOR_KIND_TEMP:
                temperature = (
                    24.0
                    + idx * 0.18
                    + season * 3.8
                    + intra_day * 4.5
                    + random.uniform(-0.8, 0.8)
                )
                tval = max(15.0, min(39.0, temperature))
                humidity = 63.0 - intra_day * 8.5 - season * 4.5 + random.uniform(-2.0, 2.0)
                hval = max(28.0, min(92.0, humidity))
                lines.append(
                    "INSERT INTO sensor_data (device_id, metric_type, metric_value, collect_time) VALUES "
                    "('%s', 'temperature', %.3f, '%s');"
                    % (device_id, tval, ts(collect_time))
                )
                lines.append(
                    "INSERT INTO sensor_data (device_id, metric_type, metric_value, collect_time) VALUES "
                    "('%s', 'humidity', %.3f, '%s');"
                    % (device_id, hval, ts(collect_time))
                )
                metric_value = tval
            elif kind == SENSOR_KIND_SOIL:
                soil = 36.0 - (point_index % 14) * 0.6 + season * 2.2 + random.uniform(-1.2, 1.2)
                metric_value = max(12.0, min(58.0, soil))
                lines.append(
                    "INSERT INTO sensor_data (device_id, metric_type, metric_value, collect_time) VALUES "
                    "('%s', 'soil_moisture', %.3f, '%s');"
                    % (device_id, metric_value, ts(collect_time))
                )
            else:
                hour_frac = collect_time.hour + collect_time.minute / 60.0
                sun = max(
                    0.0,
                    math.sin(((hour_frac / 24.0) - 0.2) * math.pi * 2.0),
                )
                lux = 400.0 + sun * 58000.0 + season * 6000.0 + random.uniform(-2500.0, 2500.0)
                metric_value = max(80.0, min(92000.0, lux))
                lines.append(
                    "INSERT INTO sensor_data (device_id, metric_type, metric_value, collect_time) VALUES "
                    "('%s', 'illuminance', %.3f, '%s');"
                    % (device_id, metric_value, ts(collect_time))
                )

            if kind == SENSOR_KIND_TEMP and point_index % 48 == 10:
                command_time = collect_time + timedelta(minutes=5)
                lines.append(
                    "INSERT INTO control_command (device_id, command_type, command_payload, status, sent_time, ack_time, source) "
                    "VALUES ('%s', 'FAN_ON', JSON_OBJECT('action', 'ON', 'target', 'fan'), 'ACKED', '%s', '%s', 'RULE:高温联动');"
                    % (device_id, ts(command_time), ts(command_time + timedelta(seconds=12)))
                )
            if kind == SENSOR_KIND_SOIL and point_index % 72 == 20:
                command_time = collect_time + timedelta(minutes=8)
                lines.append(
                    "INSERT INTO control_command (device_id, command_type, command_payload, status, sent_time, ack_time, source) "
                    "VALUES ('%s', 'PUMP_ON', JSON_OBJECT('action', 'ON', 'target', 'pump'), 'ACKED', '%s', '%s', 'MANUAL');"
                    % (device_id, ts(command_time), ts(command_time + timedelta(seconds=10)))
                )
            if kind == SENSOR_KIND_LIGHT and point_index % 56 == 14:
                command_time = collect_time + timedelta(minutes=6)
                lines.append(
                    "INSERT INTO control_command (device_id, command_type, command_payload, status, sent_time, ack_time, source) "
                    "VALUES ('%s', 'LIGHT_ON', JSON_OBJECT('action', 'ON', 'target', 'grow_light'), 'ACKED', '%s', '%s', 'RULE:弱光补光');"
                    % (device_id, ts(command_time), ts(command_time + timedelta(seconds=8)))
                )

            if kind == SENSOR_KIND_TEMP and point_index == 30:
                alarm_time = collect_time + timedelta(minutes=3)
                trigger_value = metric_value
                threshold = 31.0
                lines.append(
                    "INSERT INTO alarm_record (device_id, alarm_type, alarm_level, trigger_value, threshold_value, status, message, triggered_at, resolved_at) "
                    "VALUES ('%s', 'TEMP_HIGH', 'WARN', %.3f, %.3f, 'RESOLVED', '%s', '%s', '%s');"
                    % (
                        device_id,
                        trigger_value,
                        threshold,
                        esc("温度超过阈值"),
                        ts(alarm_time),
                        ts(alarm_time + timedelta(minutes=20)),
                    )
                )
            if kind == SENSOR_KIND_SOIL and point_index == 160:
                alarm_time = collect_time + timedelta(minutes=3)
                trigger_value = metric_value
                threshold = 25.0
                lines.append(
                    "INSERT INTO alarm_record (device_id, alarm_type, alarm_level, trigger_value, threshold_value, status, message, triggered_at, resolved_at) "
                    "VALUES ('%s', 'SOIL_LOW', 'WARN', %.3f, %.3f, 'RESOLVED', '%s', '%s', '%s');"
                    % (
                        device_id,
                        trigger_value,
                        threshold,
                        esc("土壤湿度低于阈值"),
                        ts(alarm_time),
                        ts(alarm_time + timedelta(minutes=20)),
                    )
                )
            if kind == SENSOR_KIND_LIGHT and point_index == 200:
                alarm_time = collect_time + timedelta(minutes=2)
                trigger_value = metric_value
                threshold = 3500.0
                lines.append(
                    "INSERT INTO alarm_record (device_id, alarm_type, alarm_level, trigger_value, threshold_value, status, message, triggered_at, resolved_at) "
                    "VALUES ('%s', 'LIGHT_LOW', 'WARN', %.3f, %.3f, 'RESOLVED', '%s', '%s', '%s');"
                    % (
                        device_id,
                        trigger_value,
                        threshold,
                        esc("光照度低于阈值"),
                        ts(alarm_time),
                        ts(alarm_time + timedelta(minutes=25)),
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
