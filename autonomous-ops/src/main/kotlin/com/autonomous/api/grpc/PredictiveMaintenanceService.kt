package com.autonomous.api.grpc

import io.grpc.stub.StreamObserver
import net.devh.boot.grpc.server.service.GrpcService
import com.autonomous.maintenance.PredictiveMaintenance

@GrpcService
class PredictiveMaintenanceService(
    private val predictiveMaintenance: PredictiveMaintenance
) : PredictiveMaintenanceServiceGrpc.PredictiveMaintenanceServiceImplBase() {

    override fun getPredictiveMaintenance(request: PredictiveMaintenanceRequest, responseObserver: StreamObserver<PredictiveMaintenanceResponse>) {
        val sensorData = request.sensorDataList.map {
            com.autonomous.maintenance.SensorReading(it.timestamp, it.temperature, it.vibration, it.pressure)
        }

        val prediction = predictiveMaintenance.predictFailure(request.vehicleId, sensorData)

        val response = PredictiveMaintenanceResponse.newBuilder()
            .setVehicleId(prediction.vehicleId)
            .setFailureProbability(prediction.failureProbability)
            .setNextMaintenance(prediction.nextMaintenance)
            .setRecommendedAction(prediction.recommendedAction)
            .build()

        responseObserver.onNext(response)
        responseObserver.onCompleted()
    }
}