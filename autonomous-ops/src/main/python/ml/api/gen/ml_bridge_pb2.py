# Simple stub file for ml_bridge
from google.protobuf import timestamp_pb2

class MissionParameters:
    def __init__(self):
        self.max_speed_kmh = 0.0
        self.max_altitude_m = 0.0
        self.payload_kg = 0.0
        self.weather_condition = ""
        self.risk_level = ""

class Waypoint:
    def __init__(self):
        self.waypoint_id = ""
        self.latitude = 0.0
        self.longitude = 0.0
        self.altitude = 0.0
        self.waypoint_type = ""

class PredictMissionRiskRequest:
    def __init__(self):
        self.mission_id = ""
        self.mission_type = ""
        self.parameters = MissionParameters()
        self.waypoints = []
        self.request_time = timestamp_pb2.Timestamp()

class PredictMissionRiskResponse:
    def __init__(self):
        self.mission_id = ""
        self.mission_type = ""
        self.risk_score = 0.0
        self.risk_level = ""
        self.risk_analysis = ""
        self.predicted_at = timestamp_pb2.Timestamp()

class PredictBatteryLevelRequest:
    def __init__(self):
        self.mission_id = ""
        self.current_battery_voltage = 0.0
        self.payload_kg = 0.0
        self.waypoints = []
        self.request_time = timestamp_pb2.Timestamp()

class PredictBatteryLevelResponse:
    def __init__(self):
        self.mission_id = ""
        self.predicted_battery_voltage = 0.0
        self.battery_level = ""
        self.battery_analysis = ""
        self.predicted_at = timestamp_pb2.Timestamp()

class PredictEnergyConsumptionRequest:
    def __init__(self):
        self.mission_id = ""
        self.payload_kg = 0.0
        self.waypoints = []
        self.request_time = timestamp_pb2.Timestamp()

class PredictEnergyConsumptionResponse:
    def __init__(self):
        self.mission_id = ""
        self.predicted_energy_consumption_wh = 0.0
        self.energy_analysis = ""
        self.predicted_at = timestamp_pb2.Timestamp()

class PredictWeatherImpactRequest:
    def __init__(self):
        self.mission_id = ""
        self.weather_condition = ""
        self.waypoints = []
        self.request_time = timestamp_pb2.Timestamp()

class PredictWeatherImpactResponse:
    def __init__(self):
        self.mission_id = ""
        self.weather_condition = ""
        self.delay_seconds = 0.0
        self.energy_increase_percentage = 0.0
        self.weather_analysis = ""
        self.predicted_at = timestamp_pb2.Timestamp()

class PredictTrafficImpactRequest:
    def __init__(self):
        self.mission_id = ""
        self.traffic_condition = ""
        self.waypoints = []
        self.request_time = timestamp_pb2.Timestamp()

class PredictTrafficImpactResponse:
    def __init__(self):
        self.mission_id = ""
        self.traffic_condition = ""
        self.delay_seconds = 0.0
        self.energy_increase_percentage = 0.0
        self.traffic_analysis = ""
        self.predicted_at = timestamp_pb2.Timestamp()