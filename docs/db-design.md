# MySQL Schema and Index Strategy

## Core Tables

- `sensor_data`: time-series sensor metrics
- `control_command`: command logs and acknowledgments
- `alarm_record`: alert lifecycle records

## Index Strategy

- `sensor_data(device_id, metric_type, collect_time DESC)`
  - for "device + metric + range time" historical chart queries.
- `sensor_data(collect_time DESC)`
  - for recent global timeline and retention tasks.
- `control_command(device_id, sent_time DESC)`
  - for command history by device.
- `control_command(status, sent_time DESC)`
  - for failed/unacked command retry queues.
- `alarm_record(device_id, triggered_at DESC)`
  - for alarm timeline per device.
- `alarm_record(status, alarm_level, triggered_at DESC)`
  - for alarm center filtering and unresolved-first views.

## Data Lifecycle Suggestions

- Keep hot data in MySQL for latest 3-6 months.
- Archive old `sensor_data` monthly if write volume grows.
- Add partitioning by month for `sensor_data` in production scale.
