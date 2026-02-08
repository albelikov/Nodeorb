import random
import numpy as np

class MonteCarloSimulation:
    def __init__(self, iterations=1000):
        self.iterations = iterations
        self.weather_conditions = ['CLEAR', 'CLOUDY', 'RAINY', 'SNOWY', 'STORM']
        self.weather_probabilities = [0.4, 0.3, 0.2, 0.08, 0.02]
        self.traffic_conditions = ['LIGHT', 'MODERATE', 'HEAVY', 'JAM']
        self.traffic_probabilities = [0.5, 0.3, 0.15, 0.05]
        self.battery_levels = ['HIGH', 'MEDIUM', 'LOW']
        self.battery_probabilities = [0.6, 0.3, 0.1]
        self.payload_levels = ['LIGHT', 'MEDIUM', 'HEAVY']
        self.payload_probabilities = [0.5, 0.3, 0.2]

    def simulate_mission(self, mission_id, parameters):
        results = []
        for i in range(self.iterations):
            iteration_result = self._simulate_single_iteration(mission_id, parameters)
            results.append(iteration_result)
        return results

    def _simulate_single_iteration(self, mission_id, parameters):
        weather = random.choices(self.weather_conditions, self.weather_probabilities)[0]
        traffic = random.choices(self.traffic_conditions, self.traffic_probabilities)[0]
        battery = random.choices(self.battery_levels, self.battery_probabilities)[0]
        payload = random.choices(self.payload_levels, self.payload_probabilities)[0]

        # Calculate distance
        distance = sum(
            ((w.longitude - prev.longitude)**2 + (w.latitude - prev.latitude)**2)**0.5
            for w, prev in zip(parameters.waypoints[1:], parameters.waypoints[:-1])
        ) * 1000  # Convert to meters

        # Calculate duration
        base_speed = parameters.max_speed_kmh / 3.6  # Convert to m/s
        weather_factor = self._get_weather_factor(weather)
        traffic_factor = self._get_traffic_factor(traffic)
        duration = distance / (base_speed * weather_factor * traffic_factor)

        # Calculate energy consumption
        base_energy = distance * 0.001  # 1 Wh per meter
        payload_factor = self._get_payload_factor(payload)
        weather_energy_factor = self._get_weather_energy_factor(weather)
        energy = base_energy * (1 + payload_factor + weather_energy_factor)

        # Calculate risk score
        risk_score = self._calculate_risk_score(weather, traffic, battery, payload)

        return {
            'iteration': i,
            'weather': weather,
            'traffic': traffic,
            'battery': battery,
            'payload': payload,
            'distance': distance,
            'duration': duration,
            'energy': energy,
            'risk_score': risk_score
        }

    def _get_weather_factor(self, weather):
        factors = {
            'CLEAR': 1.0,
            'CLOUDY': 0.95,
            'RAINY': 0.8,
            'SNOWY': 0.6,
            'STORM': 0.4
        }
        return factors.get(weather, 1.0)

    def _get_traffic_factor(self, traffic):
        factors = {
            'LIGHT': 1.0,
            'MODERATE': 0.8,
            'HEAVY': 0.6,
            'JAM': 0.4
        }
        return factors.get(traffic, 1.0)

    def _get_payload_factor(self, payload):
        factors = {
            'LIGHT': 0.0,
            'MEDIUM': 0.1,
            'HEAVY': 0.2
        }
        return factors.get(payload, 0.0)

    def _get_weather_energy_factor(self, weather):
        factors = {
            'CLEAR': 0.0,
            'CLOUDY': 0.05,
            'RAINY': 0.15,
            'SNOWY': 0.25,
            'STORM': 0.4
        }
        return factors.get(weather, 0.0)

    def _calculate_risk_score(self, weather, traffic, battery, payload):
        weather_risk = {
            'CLEAR': 0.1,
            'CLOUDY': 0.2,
            'RAINY': 0.5,
            'SNOWY': 0.7,
            'STORM': 0.9
        }
        traffic_risk = {
            'LIGHT': 0.1,
            'MODERATE': 0.3,
            'HEAVY': 0.6,
            'JAM': 0.9
        }
        battery_risk = {
            'HIGH': 0.1,
            'MEDIUM': 0.3,
            'LOW': 0.6
        }
        payload_risk = {
            'LIGHT': 0.1,
            'MEDIUM': 0.2,
            'HEAVY': 0.4
        }

        total_risk = (
            weather_risk.get(weather, 0.5) +
            traffic_risk.get(traffic, 0.3) +
            battery_risk.get(battery, 0.3) +
            payload_risk.get(payload, 0.2)
        ) / 4

        return total_risk

    def analyze_results(self, results):
        durations = [r['duration'] for r in results]
        energies = [r['energy'] for r in results]
        risks = [r['risk_score'] for r in results]

        return {
            'average_duration': np.mean(durations),
            'min_duration': np.min(durations),
            'max_duration': np.max(durations),
            'std_duration': np.std(durations),
            'average_energy': np.mean(energies),
            'min_energy': np.min(energies),
            'max_energy': np.max(energies),
            'std_energy': np.std(energies),
            'average_risk': np.mean(risks),
            'min_risk': np.min(risks),
            'max_risk': np.max(risks),
            'std_risk': np.std(risks),
            'high_risk_count': sum(1 for r in risks if r > 0.6),
            'low_risk_count': sum(1 for r in risks if r < 0.3)
        }