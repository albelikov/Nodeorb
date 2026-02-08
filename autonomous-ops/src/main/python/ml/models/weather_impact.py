class WeatherImpactModel:
    def __init__(self):
        self.weather_delay = {
            "CLEAR": 0,
            "CLOUDY": 0.1,
            "RAINY": 0.3,
            "SNOWY": 0.5,
            "STORM": 0.8
        }
        self.weather_energy = {
            "CLEAR": 0,
            "CLOUDY": 0.05,
            "RAINY": 0.15,
            "SNOWY": 0.25,
            "STORM": 0.4
        }

    def predict(self, request):
        weather = request.weather_condition.upper()
        distance = sum(
            ((w.longitude - prev.longitude)**2 + (w.latitude - prev.latitude)**2)**0.5
            for w, prev in zip(request.waypoints[1:], request.waypoints[:-1])
        ) * 1000  # Convert to meters

        delay_factor = self.weather_delay.get(weather, 0.1)
        energy_factor = self.weather_energy.get(weather, 0.05)

        delay_seconds = distance * 0.001 * delay_factor  # 1 second per meter with delay factor
        energy_increase = energy_factor

        return delay_seconds, energy_increase