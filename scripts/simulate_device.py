#!/usr/bin/env python3
import json
import random
import time
from datetime import datetime, timezone

import paho.mqtt.client as mqtt

DEVICE_ID = "gh-esp32-01"
BROKER = "localhost"
PORT = 1883
TOPIC = f"greenhouse/{DEVICE_ID}/telemetry"
COMMAND_TOPIC = f"greenhouse/{DEVICE_ID}/command"


def payload():
    return {
        "deviceId": DEVICE_ID,
        "timestamp": datetime.now(timezone.utc).isoformat(),
        # 与「空气温度传感器」节点一致：温度 + 空气湿度
        "metrics": {
            "temperature": round(random.uniform(20.0, 34.0), 1),
            "humidity": round(random.uniform(40.0, 85.0), 1),
        },
    }


def on_connect(client, userdata, flags, rc):
    client.subscribe(COMMAND_TOPIC, qos=1)


def on_message(client, userdata, msg):
    print("received command", msg.topic, msg.payload.decode())


def main():
    client = mqtt.Client()
    client.on_connect = on_connect
    client.on_message = on_message
    client.connect(BROKER, PORT, keepalive=60)
    client.loop_start()
    while True:
      data = payload()
      client.publish(TOPIC, json.dumps(data), qos=1)
      print("published", data)
      time.sleep(5)


if __name__ == "__main__":
    main()
