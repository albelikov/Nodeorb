class XAISupport:
    def __init__(self):
        self.weather_explanations = {
            "CLEAR": "Weather is clear, minimal impact on mission",
            "CLOUDY": "Cloudy conditions, slight impact on visibility",
            "RAINY": "Rainy conditions, moderate impact on mission",
            "SNOWY": "Snowy conditions, high impact on mission",
            "STORM": "Storm conditions, severe impact on mission"
        }
        self.traffic_explanations = {
            "LIGHT": "Light traffic, minimal delay",
            "MODERATE": "Moderate traffic, slight delay",
            "HEAVY": "Heavy traffic, significant delay",
            "JAM": "Traffic jam, severe delay"
        }
        self.battery_explanations = {
            "HIGH": "Battery level is high, sufficient for mission",
            "MEDIUM": "Battery level is medium, monitor for recharge",
            "LOW": "Battery level is low, consider recharge"
        }
        self.payload_explanations = {
            "LIGHT": "Payload is light, minimal impact on energy consumption",
            "MEDIUM": "Payload is medium, moderate impact on energy consumption",
            "HEAVY": "Payload is heavy, significant impact on energy consumption"
        }

    def explain_risk(self, risk_score, factors):
        explanation = f"Mission risk score is {risk_score:.2f} based on the following factors:\n"

        # Weather explanation
        if 'weather' in factors:
            weather = factors['weather']
            explanation += f"\n- Weather: {weather} ({self.weather_explanations.get(weather, 'Unknown weather condition')})"

        # Traffic explanation
        if 'traffic' in factors:
            traffic = factors['traffic']
            explanation += f"\n- Traffic: {traffic} ({self.traffic_explanations.get(traffic, 'Unknown traffic condition')})"

        # Battery explanation
        if 'battery' in factors:
            battery = factors['battery']
            explanation += f"\n- Battery: {battery} ({self.battery_explanations.get(battery, 'Unknown battery condition')})"

        # Payload explanation
        if 'payload' in factors:
            payload = factors['payload']
            explanation += f"\n- Payload: {payload} ({self.payload_explanations.get(payload, 'Unknown payload condition')})"

        # Risk level explanation
        if risk_score < 0.3:
            explanation += "\n\nThe mission is considered low risk."
        elif risk_score < 0.6:
            explanation += "\n\nThe mission is considered medium risk."
        else:
            explanation += "\n\nThe mission is considered high risk."

        return explanation

    def explain_maintenance(self, maintenance_probability, factors):
        explanation = f"Maintenance probability is {maintenance_probability:.2f} based on the following factors:\n"

        # Distance explanation
        if 'distance' in factors:
            distance = factors['distance']
            explanation += f"\n- Distance: {distance:.2f} km"

        # Payload explanation
        if 'payload' in factors:
            payload = factors['payload']
            explanation += f"\n- Payload: {payload} ({self.payload_explanations.get(payload, 'Unknown payload condition')})"

        # Age explanation
        if 'age' in factors:
            age = factors['age']
            explanation += f"\n- Age: {age} days"

        # Maintenance level explanation
        if maintenance_probability < 0.2:
            explanation += "\n\nMaintenance is not required at this time."
        elif maintenance_probability < 0.5:
            explanation += "\n\nMaintenance is recommended soon."
        else:
            explanation += "\n\nMaintenance is required immediately."

        return explanation

    def explain_weather_impact(self, weather, delay, energy_increase):
        explanation = f"Weather condition '{weather}' has the following impact:\n"
        explanation += f"\n- Delay: {delay:.2f} seconds"
        explanation += f"\n- Energy increase: {energy_increase:.2f}%"
        explanation += f"\n\n{self.weather_explanations.get(weather, 'Unknown weather condition')}"

        return explanation

    def explain_traffic_impact(self, traffic, delay, energy_increase):
        explanation = f"Traffic condition '{traffic}' has the following impact:\n"
        explanation += f"\n- Delay: {delay:.2f} seconds"
        explanation += f"\n- Energy increase: {energy_increase:.2f}%"
        explanation += f"\n\n{self.traffic_explanations.get(traffic, 'Unknown traffic condition')}"

        return explanation

    def explain_decision(self, decision, factors):
        explanation = f"Decision: {decision}\n"
        explanation += "Based on the following factors:\n"

        for key, value in factors.items():
            explanation += f"\n- {key}: {value}"

        return explanation