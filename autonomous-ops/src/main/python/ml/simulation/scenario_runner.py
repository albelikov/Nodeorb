from monte_carlo import MonteCarloSimulation
from models.risk_model import RiskModel
from models.weather_impact import WeatherImpactModel

class ScenarioRunner:
    def __init__(self, iterations=1000):
        self.simulator = MonteCarloSimulation(iterations)
        self.risk_model = RiskModel()
        self.weather_model = WeatherImpactModel()

    def run_scenario(self, scenario):
        mission_id = scenario['mission_id']
        parameters = scenario['parameters']

        print(f"Running scenario for mission: {mission_id}")
        print(f"Parameters: {parameters}")

        # Run Monte Carlo simulation
        results = self.simulator.simulate_mission(mission_id, parameters)

        # Analyze results
        analysis = self.simulator.analyze_results(results)

        # Predict risk using trained model
        risk_score = self.risk_model.predict(parameters)

        # Predict weather impact
        weather_delay, weather_energy = self.weather_model.predict(parameters)

        print(f"Simulation completed for mission: {mission_id}")
        print(f"Risk score: {risk_score:.2f}")
        print(f"Weather delay: {weather_delay:.2f} seconds")
        print(f"Weather energy increase: {weather_energy:.2f}%")
        print(f"Analysis: {analysis}")

        return {
            'mission_id': mission_id,
            'parameters': parameters,
            'risk_score': risk_score,
            'weather_delay': weather_delay,
            'weather_energy': weather_energy,
            'analysis': analysis,
            'results': results
        }

def main():
    # Example scenario
    scenario = {
        'mission_id': 'MISSION-001',
        'parameters': {
            'max_speed_kmh': 100,
            'max_voltage': 24,
            'payload_kg': 5,
            'weather_condition': 'CLEAR',
            'waypoints': [
                {'latitude': 51.5074, 'longitude': -0.1278},
                {'latitude': 51.5074, 'longitude': -0.1278 + 0.1}
            ]
        }
    }

    # Create scenario runner
    runner = ScenarioRunner(iterations=1000)

    # Run scenario
    results = runner.run_scenario(scenario)

    # Print results
    print("\n--- SCENARIO RESULTS ---")
    print(f"Mission ID: {results['mission_id']}")
    print(f"Risk Score: {results['risk_score']:.2f}")
    print(f"Weather Delay: {results['weather_delay']:.2f} seconds")
    print(f"Weather Energy Increase: {results['weather_energy']:.2f}%")

    print("\n--- ANALYSIS ---")
    analysis = results['analysis']
    print(f"Average Duration: {analysis['average_duration']:.2f} seconds")
    print(f"Minimum Duration: {analysis['min_duration']:.2f} seconds")
    print(f"Maximum Duration: {analysis['max_duration']:.2f} seconds")
    print(f"Duration Std: {analysis['std_duration']:.2f} seconds")

    print(f"Average Energy: {analysis['average_energy']:.2f} Wh")
    print(f"Minimum Energy: {analysis['min_energy']:.2f} Wh")
    print(f"Maximum Energy: {analysis['max_energy']:.2f} Wh")
    print(f"Energy Std: {analysis['std_energy']:.2f} Wh")

    print(f"Average Risk: {analysis['average_risk']:.2f}")
    print(f"Minimum Risk: {analysis['min_risk']:.2f}")
    print(f"Maximum Risk: {analysis['max_risk']:.2f}")
    print(f"Risk Std: {analysis['std_risk']:.2f}")

    print(f"High Risk Count: {analysis['high_risk_count']}")
    print(f"Low Risk Count: {analysis['low_risk_count']}")

if __name__ == "__main__":
    main()