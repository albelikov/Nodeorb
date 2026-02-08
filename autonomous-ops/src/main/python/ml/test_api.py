from api.inference_server import MLBridgeServicer
from gen import ml_bridge_pb2
from gen import ml_bridge_pb2_grpc
from models.risk_model import RiskModel
from models.maintenance_model import MaintenanceModel
from models.weather_impact import WeatherImpactModel

def test_models():
    print("Testing models...")
    
    # Test risk model
    risk_model = RiskModel()
    risk_request = ml_bridge_pb2.PredictMissionRiskRequest()
    risk_request.mission_id = "TEST-MISSION-001"
    risk_request.mission_type = "DELIVERY"
    risk_request.parameters.weather_condition = "CLEAR"
    risk_request.parameters.max_speed_kmh = 100
    risk_request.parameters.payload_kg = 5
    
    risk_score = risk_model.predict(risk_request)
    print(f"Risk score: {risk_score:.2f}")
    
    # Test weather impact model
    weather_model = WeatherImpactModel()
    weather_request = ml_bridge_pb2.PredictWeatherImpactRequest()
    weather_request.mission_id = "TEST-MISSION-001"
    weather_request.weather_condition = "RAINY"
    weather_request.waypoints = [
        ml_bridge_pb2.Waypoint(latitude=51.5074, longitude=-0.1278),
        ml_bridge_pb2.Waypoint(latitude=51.5074, longitude=-0.1278 + 0.1)
    ]
    
    delay, energy_increase = weather_model.predict(weather_request)
    print(f"Weather delay: {delay:.2f} seconds")
    print(f"Energy increase: {energy_increase:.2f}%")
    
    # Test maintenance model
    maintenance_model = MaintenanceModel()
    maintenance_request = ml_bridge_pb2.PredictMissionRiskRequest()
    maintenance_request.parameters.payload_kg = 10
    maintenance_request.waypoints = [
        ml_bridge_pb2.Waypoint(latitude=51.5074, longitude=-0.1278),
        ml_bridge_pb2.Waypoint(latitude=51.5074, longitude=-0.1278 + 0.1)
    ]
    
    maintenance_probability = maintenance_model.predict(maintenance_request)
    print(f"Maintenance probability: {maintenance_probability:.2f}")

    print("Models test completed")

def test_simulation():
    from simulation.monte_carlo import MonteCarloSimulation

    print("Testing simulation...")
    
    simulator = MonteCarloSimulation(iterations=100)
    
    parameters = ml_bridge_pb2.MissionParameters()
    parameters.max_speed_kmh = 100
    parameters.max_altitude_m = 100
    parameters.payload_kg = 5
    parameters.weather_condition = "CLEAR"
    
    # Run simulation
    results = simulator.simulate_mission("TEST-MISSION-001", parameters)
    
    # Analyze results
    analysis = simulator.analyze_results(results)
    
    print("Simulation results:")
    print(f"Average duration: {analysis['average_duration']:.2f} seconds")
    print(f"Min duration: {analysis['min_duration']:.2f} seconds")
    print(f"Max duration: {analysis['max_duration']:.2f} seconds")
    print(f"Duration std: {analysis['std_duration']:.2f} seconds")
    
    print(f"Average energy: {analysis['average_energy']:.2f} Wh")
    print(f"Min energy: {analysis['min_energy']:.2f} Wh")
    print(f"Max energy: {analysis['max_energy']:.2f} Wh")
    print(f"Energy std: {analysis['std_energy']:.2f} Wh")
    
    print(f"Average risk: {analysis['average_risk']:.2f}")
    print(f"Min risk: {analysis['min_risk']:.2f}")
    print(f"Max risk: {analysis['max_risk']:.2f}")
    print(f"Risk std: {analysis['std_risk']:.2f}")
    
    print(f"High risk count: {analysis['high_risk_count']}")
    print(f"Low risk count: {analysis['low_risk_count']}")

    print("Simulation test completed")

if __name__ == "__main__":
    print("Running tests...")
    
    try:
        test_models()
        print()
        test_simulation()
    except Exception as e:
        print(f"Error: {e}")
        import traceback
        print(traceback.format_exc())