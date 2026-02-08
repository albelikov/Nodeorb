# Simple stub file for ml_bridge gRPC
from concurrent import futures
import time
import grpc

class MLBridgeServiceServicer:
    def PredictMissionRisk(self, request, context):
        raise NotImplementedError()

    def PredictBatteryLevel(self, request, context):
        raise NotImplementedError()

    def PredictEnergyConsumption(self, request, context):
        raise NotImplementedError()

    def PredictWeatherImpact(self, request, context):
        raise NotImplementedError()

    def PredictTrafficImpact(self, request, context):
        raise NotImplementedError()

def add_MLBridgeServiceServicer_to_server(servicer, server):
    server.add_generic_rpc_handlers([
        servicer
    ])