package com.nodeorb.scmclient.grpc

import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import io.grpc.stub.MetadataUtils
import io.grpc.Metadata
import io.grpc.StatusRuntimeException
import com.nodeorb.scmclient.model.ValidationContext
import com.nodeorb.scmclient.model.AppealRequest
import com.nodeorb.scmclient.model.AccessCheckRequest
import com.nodeorb.scmclient.interceptor.ContextInterceptor

/**
 * gRPC стаб для SCM сервісу
 * Містить реальні виклики gRPC методів
 */
class SCMServiceGrpc(
    private val channel: ManagedChannel,
    private val contextInterceptor: ContextInterceptor
) {

    private val blockingStub: SCMServiceGrpcBlockingStub
    private val asyncStub: SCMServiceGrpcAsyncStub

    init {
        blockingStub = SCMServiceGrpc.newBlockingStub(channel)
        asyncStub = SCMServiceGrpc.newStub(channel)
    }

    /**
     * Виклик методу validateCost
     */
    fun validateCost(context: ValidationContext, metadata: Metadata): ValidateCostResponse {
        val request = ValidateCostRequest.newBuilder()
            .setUserId(context.userId)
            .setOrderId(context.orderId)
            .setCategory(context.category)
            .setValue(context.value)
            .setLatitude(context.latitude)
            .setLongitude(context.longitude)
            .putAllAdditionalContext(context.additionalContext)
            .build()

        return blockingStub.withInterceptors(
            MetadataUtils.newAttachHeadersInterceptor(metadata)
        ).validateCost(request)
    }

    /**
     * Виклик методу submitAppeal
     */
    fun submitAppeal(request: AppealRequest, metadata: Metadata): SubmitAppealResponse {
        val grpcRequest = SubmitAppealRequest.newBuilder()
            .setRecordHash(request.recordHash)
            .setText(request.text)
            .setEvidenceUrl(request.evidenceUrl)
            .putAllAdditionalContext(request.additionalContext)
            .build()

        return blockingStub.withInterceptors(
            MetadataUtils.newAttachHeadersInterceptor(metadata)
        ).submitAppeal(grpcRequest)
    }

    /**
     * Виклик методу checkAccess
     */
    fun checkAccess(request: AccessCheckRequest, metadata: Metadata): CheckAccessResponse {
        val grpcRequest = CheckAccessRequest.newBuilder()
            .setUserId(request.userId)
            .setAction(request.action)
            .putAllContext(request.context)
            .build()

        return blockingStub.withInterceptors(
            MetadataUtils.newAttachHeadersInterceptor(metadata)
        ).checkAccess(grpcRequest)
    }

    /**
     * Асинхронний виклик validateCost
     */
    fun validateCostAsync(
        context: ValidationContext,
        metadata: Metadata,
        callback: (ValidateCostResponse?, StatusRuntimeException?) -> Unit
    ) {
        val request = ValidateCostRequest.newBuilder()
            .setUserId(context.userId)
            .setOrderId(context.orderId)
            .setCategory(context.category)
            .setValue(context.value)
            .setLatitude(context.latitude)
            .setLongitude(context.longitude)
            .putAllAdditionalContext(context.additionalContext)
            .build()

        asyncStub.withInterceptors(
            MetadataUtils.newAttachHeadersInterceptor(metadata)
        ).validateCost(request) { response, error ->
            callback(response, error)
        }
    }

    /**
     * Асинхронний виклик submitAppeal
     */
    fun submitAppealAsync(
        request: AppealRequest,
        metadata: Metadata,
        callback: (SubmitAppealResponse?, StatusRuntimeException?) -> Unit
    ) {
        val grpcRequest = SubmitAppealRequest.newBuilder()
            .setRecordHash(request.recordHash)
            .setText(request.text)
            .setEvidenceUrl(request.evidenceUrl)
            .putAllAdditionalContext(request.additionalContext)
            .build()

        asyncStub.withInterceptors(
            MetadataUtils.newAttachHeadersInterceptor(metadata)
        ).submitAppeal(grpcRequest) { response, error ->
            callback(response, error)
        }
    }

    /**
     * Асинхронний виклик checkAccess
     */
    fun checkAccessAsync(
        request: AccessCheckRequest,
        metadata: Metadata,
        callback: (CheckAccessResponse?, StatusRuntimeException?) -> Unit
    ) {
        val grpcRequest = CheckAccessRequest.newBuilder()
            .setUserId(request.userId)
            .setAction(request.action)
            .putAllContext(request.context)
            .build()

        asyncStub.withInterceptors(
            MetadataUtils.newAttachHeadersInterceptor(metadata)
        ).checkAccess(grpcRequest) { response, error ->
            callback(response, error)
        }
    }

    /**
     * Закриття каналу
     */
    fun shutdown() {
        try {
            channel.shutdown()
                .awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS)
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
            throw RuntimeException("Error shutting down gRPC channel", e)
        }
    }
}