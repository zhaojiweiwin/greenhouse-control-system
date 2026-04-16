CREATE DATABASE IF NOT EXISTS greenhouse DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE greenhouse;

CREATE TABLE IF NOT EXISTS device (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  device_id VARCHAR(64) NOT NULL UNIQUE,
  name VARCHAR(128) NOT NULL,
  greenhouse_name VARCHAR(128) NOT NULL,
  control_mode VARCHAR(16) NOT NULL DEFAULT 'AUTO',
  online_status TINYINT NOT NULL DEFAULT 0,
  last_heartbeat DATETIME NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS sensor_data (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  device_id VARCHAR(64) NOT NULL,
  metric_type VARCHAR(32) NOT NULL,
  metric_value DECIMAL(10, 3) NOT NULL,
  collect_time DATETIME NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_sensor_device_metric_time (device_id, metric_type, collect_time DESC),
  INDEX idx_sensor_collect_time (collect_time DESC)
);

CREATE TABLE IF NOT EXISTS control_command (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  device_id VARCHAR(64) NOT NULL,
  command_type VARCHAR(32) NOT NULL,
  command_payload JSON NOT NULL,
  status VARCHAR(32) NOT NULL DEFAULT 'SENT',
  sent_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  ack_time DATETIME NULL,
  source VARCHAR(32) NOT NULL DEFAULT 'MANUAL',
  INDEX idx_cmd_device_time (device_id, sent_time DESC),
  INDEX idx_cmd_status_time (status, sent_time DESC)
);

CREATE TABLE IF NOT EXISTS alarm_record (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  device_id VARCHAR(64) NOT NULL,
  alarm_type VARCHAR(32) NOT NULL,
  alarm_level VARCHAR(16) NOT NULL,
  trigger_value DECIMAL(10, 3) NULL,
  threshold_value DECIMAL(10, 3) NULL,
  status VARCHAR(16) NOT NULL DEFAULT 'OPEN',
  message VARCHAR(255) NOT NULL,
  triggered_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  resolved_at DATETIME NULL,
  INDEX idx_alarm_device_time (device_id, triggered_at DESC),
  INDEX idx_alarm_status_level_time (status, alarm_level, triggered_at DESC)
);

CREATE TABLE IF NOT EXISTS control_rule (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  rule_name VARCHAR(128) NOT NULL,
  device_id VARCHAR(64) NOT NULL,
  metric_type VARCHAR(32) NOT NULL,
  operator VARCHAR(8) NOT NULL,
  threshold_value DECIMAL(10, 3) NOT NULL,
  action_type VARCHAR(32) NOT NULL,
  action_payload JSON NOT NULL,
  enabled TINYINT NOT NULL DEFAULT 1,
  cooldown_seconds INT NOT NULL DEFAULT 60,
  last_triggered_at DATETIME NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_rule_device_metric_enabled (device_id, metric_type, enabled)
);

INSERT INTO device (device_id, name, greenhouse_name, control_mode, online_status, last_heartbeat)
SELECT 'gh-esp32-01', 'ESP32主控节点', '1号实验温室', 'AUTO', 0, NULL
WHERE NOT EXISTS (
  SELECT 1 FROM device WHERE device_id = 'gh-esp32-01'
);

INSERT INTO control_rule (
  rule_name, device_id, metric_type, operator, threshold_value, action_type, action_payload, enabled, cooldown_seconds
)
SELECT
  '高温启动风机',
  'gh-esp32-01',
  'temperature',
  '>',
  30.0,
  'FAN_ON',
  JSON_OBJECT('action', 'ON', 'target', 'fan'),
  1,
  60
WHERE NOT EXISTS (
  SELECT 1 FROM control_rule WHERE rule_name = '高温启动风机'
);

INSERT INTO control_rule (
  rule_name, device_id, metric_type, operator, threshold_value, action_type, action_payload, enabled, cooldown_seconds
)
SELECT
  '土壤过干启动水泵',
  'gh-esp32-01',
  'soil_moisture',
  '<',
  30.0,
  'PUMP_ON',
  JSON_OBJECT('action', 'ON', 'target', 'pump'),
  1,
  60
WHERE NOT EXISTS (
  SELECT 1 FROM control_rule WHERE rule_name = '土壤过干启动水泵'
);
