#!/usr/bin/env python3
"""Simple strategy validation for defense demo."""

RULES = {
    "temperature_upper": 30.0,
    "soil_moisture_lower": 30.0,
}


def evaluate(metrics):
    actions = []
    if metrics["temperature"] > RULES["temperature_upper"]:
        actions.append("FAN_ON")
    if metrics["soil_moisture"] < RULES["soil_moisture_lower"]:
        actions.append("PUMP_ON")
    return actions


def run_demo():
    cases = [
        {"temperature": 31.2, "soil_moisture": 42.0},
        {"temperature": 28.0, "soil_moisture": 25.0},
        {"temperature": 33.4, "soil_moisture": 23.1},
    ]
    for i, case in enumerate(cases, start=1):
        actions = evaluate(case)
        print(f"Case {i}: metrics={case}, actions={actions}")


if __name__ == "__main__":
    run_demo()
