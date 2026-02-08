import grpc
from concurrent import futures
import time
from gen import ml_bridge_pb2
from gen import ml_bridge_pb2_grpc
from models.risk_model import RiskModel
from models.maintenance_model import MaintenanceModel
from models.weather_impact import WeatherImpactModel

class MLBridgeServicer(ml_bridge_pb2_grpc.MLBridgeServiceServicer):
    def __init__(self):
        self.risk_model = RiskModel()
        self.maintenance_model = MaintenanceModel()
        self.weather_impact_model = WeatherImpactModel()

    def PredictMissionRisk(self, request, context):
        risk_score = self.risk_model.predict(request)
        return ml_bridge_pb2.PredictMissionRiskResponse(
            mission_id=request.mission_id,
            mission_type=request.mission_type,
            risk_score=risk_score,
            risk_level=self._get_risk_level(risk_score),
            risk_analysis="Mission risk is determined by multiple factors",
            predicted_at=int(time.time())
        )

    def PredictBatteryLevel(self, request, context):
        battery_level = self._predict_battery_level(request)
        return ml_bridge_pb2.PredictBatteryLevelResponse(
            mission_id=request.mission_id,
            predicted_battery_voltage=battery_level,
            battery_level=self._get_battery_level(battery_level),
            battery_analysis="Battery level prediction based on current voltage and payload",
            predicted_at=int(time.time())
        )

    def PredictEnergyConsumption(self, request, context):
        energy_consumption = self._predict_energy_consumption(request)
        return ml_bridge_pb2.PredictEnergyConsumptionResponse(
            mission_id=request.mission_id,
            predicted_energy_consumption_wh=energy_consumption,
            energy_analysis="Energy consumption prediction based on distance and payload",
            predicted_at=int(time.time())
        )

    def PredictWeatherImpact(self, request, context):
        delay_seconds, energy_increase = self.weather_impact_model.predict(request)
        return ml_bridge_pb2.PredictWeatherImpactResponse(
            mission_id=request.mission_id,
            weather_condition=request.weather_condition,
            delay_seconds=delay_seconds,
            energy_increase_percentage=energy_increase,
            weather_analysis="Weather impact prediction based on weather condition and route",
            predicted_at=int(time.time())
        )

    def PredictTrafficImpact(self, request, context):
        delay_seconds, energy_increase = self._predict_traffic_impact(request)
        return ml_bridge_pb2.PredictTrafficImpactResponse(
            mission_id=request.mission_id,
            traffic_condition=request.traffic_condition,
            delay_seconds=delay_seconds,
            energy_increase_percentage=energy_increase,
            traffic_analysis="Traffic impact prediction based on traffic condition and route",
            predicted_at=int(time.time())
        )

    def _get_risk_level(self, risk_score):
        if risk_score < 0.3:
            return "LOW"
        elif risk_score < 0.6:
            return "MEDIUM"
        else:
            return "HIGH"

    def _get_battery_level(self, voltage):
        if voltage > 24:
            return "HIGH"
        elif voltage > 20:
            return "MEDIUM"
        else:
            return "LOW"

    def _predict_battery_level(self, request):
        # Simple battery level prediction
        base_voltage = request.current_battery_voltage
        payload_factor = request.payload_kg * 0.01
        distance_factor = sum(
            ((w.longitude - prev.longitude)**2 + (w.latitude - prev.latitude)**2)**0.5
            for w, prev in zip(request.waypoints[1:], request.waypoints[:-1])
        ) * 0.05

        return base_voltage - payload_factor - distance_factor

    def _predict_energy_consumption(self, request):
        # Simple energy consumption prediction
        distance = sum(
            ((w.longitude - prev.longitude)**2 + (w.latitude - prev.latitude)**2)**0.5
            for w, prev in zip(request.waypoints[1:], request.waypoints[:-1])
        ) * 1000  # Convert to meters

        payload_factor = request.payload_kg * 0.1
        base_consumption = distance * 0.001  # 1 Wh per meter

        return base_consumption + payload_factor

    def _predict_traffic_impact(self, request):
        # Simple traffic impact prediction
        distance = sum(
            ((w.longitude - prev.longitude)**2 + (w.latitude - prev.latitude)**2)**0.5
            for w, prev in zip(request.waypoints[1:], request.waypoints[:-1])
        ) * 1000  # Convert to meters

        if request.traffic_condition == "HEAVY":
            delay_seconds = distance * 0.005  # 5 seconds per meter
            energy_increase = 0.3
        elif request.traffic_condition == "MODERATE":
            delay_seconds = distance * 0.002  # 2 seconds per meter
            energy_increase = 0.1
        else:
            delay_seconds = distance * 0.001  # 1 second per meter
            energy_increase = 0.05

        return delay_seconds, energy_increase

def serve():
    server = grpc.server(futures.ThreadPoolExecutor(max_workers=10))
    ml_bridge_pb2_grpc.add_MLBridgeServiceServicer_to_server(
        MLBridgeServicer(), server
    )
    server.add_insecure_port('[::]:50051')
    server.start()
    print("ML Bridge server started on port 50051")
    try:
        while True:
            time.sleep(86400)
    except KeyboardInterrupt:
        server.stop(0)

if __name__ == '__main__':
    serve()