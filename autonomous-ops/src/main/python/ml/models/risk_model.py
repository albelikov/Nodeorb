class RiskModel:
    def __init__(self):
        self.weather_risk = {
            "CLEAR": 0.1,
            "CLOUDY": 0.2,
            "RAINY": 0.5,
            "SNOWY": 0.7,
            "STORM": 0.9
        }
        self.traffic_risk = {
            "LIGHT": 0.1,
            "MODERATE": 0.3,
            "HEAVY": 0.6,
            "JAM": 0.9
        }
        self.battery_risk = {
            "HIGH": 0.1,
            "MEDIUM": 0.3,
            "LOW": 0.6
        }
        self.payload_risk = {
            "LIGHT": 0.1,
            "MEDIUM": 0.2,
            "HEAVY": 0.4
        }

    def predict(self, request):
        # Calculate weather risk
        weather = request.parameters.weather_condition.upper()
        weather_risk = self.weather_risk.get(weather, 0.5)

        # Calculate traffic risk
        traffic = "MODERATE"  # Default traffic condition
        traffic_risk = self.traffic_risk.get(traffic, 0.3)

        # Calculate battery risk
        battery = self._get_battery_level(request.parameters.max_voltage)
        battery_risk = self.battery_risk.get(battery, 0.3)

        # Calculate payload risk
        payload = self._get_payload_level(request.parameters.payload_kg)
        payload_risk = self.payload_risk.get(payload, 0.2)

        # Calculate total risk
        total_risk = (weather_risk + traffic_risk + battery_risk + payload_risk) / 4

        return total_risk

    def _get_battery_level(self, voltage):
        if voltage > 24:
            return "HIGH"
        elif voltage > 20:
            return "MEDIUM"
        else:
            return "LOW"

    def _get_payload_level(self, payload):
        if payload < 5:
            return "LIGHT"
        elif payload < 10:
            return "MEDIUM"
        else:
            return "HEAVY"