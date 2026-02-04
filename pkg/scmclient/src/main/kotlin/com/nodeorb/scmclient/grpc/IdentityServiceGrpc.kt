package com.nodeorb.scmclient.grpc

import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import io.grpc.stub.MetadataUtils
import io.grpc.Metadata
import io.grpc.StatusRuntimeException
import com.nodeorb.identity.grpc.IdentityServiceGrpc
import com.nodeorb.identity.grpc.BiometricChallengeRequest
import com.nodeorb.identity.grpc.BiometricChallengeResponse
import com.nodeorb.identity.grpc.BiometricSignatureRequest
import com.nodeorb.identity.grpc.BiometricSignatureResponse
import com.nodeorb.identity.grpc.WebAuthnChallengeRequest
import com.nodeorb.identity.grpc.WebAuthnChallengeResponse
import com.nodeorb.identity.grpc.WebAuthnSignatureRequest
import com.nodeorb.identity.grpc.WebAuthnSignatureResponse
import com.nodeorb.identity.grpc.WebAuthnRegistrationRequest
import com.nodeorb.identity.grpc.WebAuthnRegistrationResponse
import com.nodeorb.identity.grpc.UserCredentialsRequest
import com.nodeorb.identity.grpc.UserCredentialsResponse
import com.nodeorb.identity.grpc.BiometricSessionRequest
import com.nodeorb.identity.grpc.BiometricSessionResponse
import com.nodeorb.identity.grpc.BiometricAuthType
import com.nodeorb.identity.grpc.AuthenticatorType
import com.nodeorb.identity.grpc.PublicKeyCredential
import com.nodeorb.scmclient.interceptor.ContextInterceptor
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit

/**
 * gRPC стаб для Identity Service
 * Містить реальні виклики gRPC методів для біометричної аутентифікації
 */
class IdentityServiceGrpc(
    private val channel: ManagedChannel,
    private val contextInterceptor: ContextInterceptor
) {

    private val blockingStub: IdentityServiceGrpc.IdentityServiceBlockingStub
    private val asyncStub: IdentityServiceGrpc.IdentityServiceStub

    companion object {
        private val logger = LoggerFactory.getLogger(IdentityServiceGrpc::class.java)
    }

    init {
        blockingStub = IdentityServiceGrpc.newBlockingStub(channel)
        asyncStub = IdentityServiceGrpc.newStub(channel)
    }

    /**
     * Виклик методу generateBiometricChallenge
     */
    fun generateBiometricChallenge(
        userId: String,
        authType: BiometricAuthType,
        context: Map<String, String>,
        metadata: Metadata
    ): BiometricChallengeResponse {
        val request = BiometricChallengeRequest.newBuilder()
            .setUserId(userId)
            .setAuthType(authType)
            .putAllContext(context)
            .build()

        return blockingStub.withInterceptors(
            MetadataUtils.newAttachHeadersInterceptor(metadata)
        ).generateBiometricChallenge(request)
    }

    /**
     * Виклик методу verifyBiometricSignature
     */
    fun verifyBiometricSignature(
        bioSessionId: String,
        signedChallenge: String,
        userId: String,
        metadata: Metadata
    ): BiometricSignatureResponse {
        val request = BiometricSignatureRequest.newBuilder()
            .setBioSessionId(bioSessionId)
            .setSignedChallenge(signedChallenge)
            .setUserId(userId)
            .build()

        return blockingStub.withInterceptors(
            MetadataUtils.newAttachHeadersInterceptor(metadata)
        ).verifyBiometricSignature(request)
    }

    /**
     * Виклик методу generateWebAuthnChallenge
     */
    fun generateWebAuthnChallenge(
        userId: String,
        origin: String,
        rpId: String,
        metadata: Metadata
    ): WebAuthnChallengeResponse {
        val request = WebAuthnChallengeRequest.newBuilder()
            .setUserId(userId)
            .setOrigin(origin)
            .setRpId(rpId)
            .build()

        return blockingStub.withInterceptors(
            MetadataUtils.newAttachHeadersInterceptor(metadata)
        ).generateWebAuthnChallenge(request)
    }

    /**
     * Виклик методу verifyWebAuthnSignature
     */
    fun verifyWebAuthnSignature(
        challengeId: String,
        signedChallenge: String,
        credentialId: String,
        userHandle: String,
        authenticatorData: String,
        signature: String,
        userId: String,
        metadata: Metadata
    ): WebAuthnSignatureResponse {
        val request = WebAuthnSignatureRequest.newBuilder()
            .setChallengeId(challengeId)
            .setSignedChallenge(signedChallenge)
            .setCredentialId(credentialId)
            .setUserHandle(userHandle)
            .setAuthenticatorData(authenticatorData)
            .setSignature(signature)
            .setUserId(userId)
            .build()

        return blockingStub.withInterceptors(
            MetadataUtils.newAttachHeadersInterceptor(metadata)
        ).verifyWebAuthnSignature(request)
    }

    /**
     * Виклик методу registerWebAuthnCredential
     */
    fun registerWebAuthnCredential(
        userId: String,
        credentialId: String,
        publicKeyPem: String,
        authenticatorType: AuthenticatorType,
        deviceName: String?,
        metadata: Map<String, String>,
        metadataHeaders: Metadata
    ): WebAuthnRegistrationResponse {
        val request = WebAuthnRegistrationRequest.newBuilder()
            .setUserId(userId)
            .setCredentialId(credentialId)
            .setPublicKeyPem(publicKeyPem)
            .setAuthenticatorType(authenticatorType)
            .setDeviceName(deviceName ?: "")
            .putAllMetadata(metadata)
            .build()

        return blockingStub.withInterceptors(
            MetadataUtils.newAttachHeadersInterceptor(metadataHeaders)
        ).registerWebAuthnCredential(request)
    }

    /**
     * Виклик методу getUserCredentials
     */
    fun getUserCredentials(
        userId: String,
        metadata: Metadata
    ): UserCredentialsResponse {
        val request = UserCredentialsRequest.newBuilder()
            .setUserId(userId)
            .build()

        return blockingStub.withInterceptors(
            MetadataUtils.newAttachHeadersInterceptor(metadata)
        ).getUserCredentials(request)
    }

    /**
     * Виклик методу validateBiometricSession
     */
    fun validateBiometricSession(
        bioSessionId: String,
        userId: String,
        metadata: Metadata
    ): BiometricSessionResponse {
        val request = BiometricSessionRequest.newBuilder()
            .setBioSessionId(bioSessionId)
            .setUserId(userId)
            .build()

        return blockingStub.withInterceptors(
            MetadataUtils.newAttachHeadersInterceptor(metadata)
        ).validateBiometricSession(request)
    }

    /**
     * Асинхронний виклик generateBiometricChallenge
     */
    fun generateBiometricChallengeAsync(
        userId: String,
        authType: BiometricAuthType,
        context: Map<String, String>,
        metadata: Metadata,
        callback: (BiometricChallengeResponse?, StatusRuntimeException?) -> Unit
    ) {
        val request = BiometricChallengeRequest.newBuilder()
            .setUserId(userId)
            .setAuthType(authType)
            .putAllContext(context)
            .build()

        asyncStub.withInterceptors(
            MetadataUtils.newAttachHeadersInterceptor(metadata)
        ).generateBiometricChallenge(request) { response, error ->
            callback(response, error)
        }
    }

    /**
     * Асинхронний виклик verifyBiometricSignature
     */
    fun verifyBiometricSignatureAsync(
        bioSessionId: String,
        signedChallenge: String,
        userId: String,
        metadata: Metadata,
        callback: (BiometricSignatureResponse?, StatusRuntimeException?) -> Unit
    ) {
        val request = BiometricSignatureRequest.newBuilder()
            .setBioSessionId(bioSessionId)
            .setSignedChallenge(signedChallenge)
            .setUserId(userId)
            .build()

        asyncStub.withInterceptors(
            MetadataUtils.newAttachHeadersInterceptor(metadata)
        ).verifyBiometricSignature(request) { response, error ->
            callback(response, error)
        }
    }

    /**
     * Асинхронний виклик generateWebAuthnChallenge
     */
    fun generateWebAuthnChallengeAsync(
        userId: String,
        origin: String,
        rpId: String,
        metadata: Metadata,
        callback: (WebAuthnChallengeResponse?, StatusRuntimeException?) -> Unit
    ) {
        val request = WebAuthnChallengeRequest.newBuilder()
            .setUserId(userId)
            .setOrigin(origin)
            .setRpId(rpId)
            .build()

        asyncStub.withInterceptors(
            MetadataUtils.newAttachHeadersInterceptor(metadata)
        ).generateWebAuthnChallenge(request) { response, error ->
            callback(response, error)
        }
    }

    /**
     * Асинхронний виклик verifyWebAuthnSignature
     */
    fun verifyWebAuthnSignatureAsync(
        challengeId: String,
        signedChallenge: String,
        credentialId: String,
        userHandle: String,
        authenticatorData: String,
        signature: String,
        userId: String,
        metadata: Metadata,
        callback: (WebAuthnSignatureResponse?, StatusRuntimeException?) -> Unit
    ) {
        val request = WebAuthnSignatureRequest.newBuilder()
            .setChallengeId(challengeId)
            .setSignedChallenge(signedChallenge)
            .setCredentialId(credentialId)
            .setUserHandle(userHandle)
            .setAuthenticatorData(authenticatorData)
            .setSignature(signature)
            .setUserId(userId)
            .build()

        asyncStub.withInterceptors(
            MetadataUtils.newAttachHeadersInterceptor(metadata)
        ).verifyWebAuthnSignature(request) { response, error ->
            callback(response, error)
        }
    }

    /**
     * Асинхронний виклик registerWebAuthnCredential
     */
    fun registerWebAuthnCredentialAsync(
        userId: String,
        credentialId: String,
        publicKeyPem: String,
        authenticatorType: AuthenticatorType,
        deviceName: String?,
        metadata: Map<String, String>,
        metadataHeaders: Metadata,
        callback: (WebAuthnRegistrationResponse?, StatusRuntimeException?) -> Unit
    ) {
        val request = WebAuthnRegistrationRequest.newBuilder()
            .setUserId(userId)
            .setCredentialId(credentialId)
            .setPublicKeyPem(publicKeyPem)
            .setAuthenticatorType(authenticatorType)
            .setDeviceName(deviceName ?: "")
            .putAllMetadata(metadata)
            .build()

        asyncStub.withInterceptors(
            MetadataUtils.newAttachHeadersInterceptor(metadataHeaders)
        ).registerWebAuthnCredential(request) { response, error ->
            callback(response, error)
        }
    }

    /**
     * Асинхронний виклик getUserCredentials
     */
    fun getUserCredentialsAsync(
        userId: String,
        metadata: Metadata,
        callback: (UserCredentialsResponse?, StatusRuntimeException?) -> Unit
    ) {
        val request = UserCredentialsRequest.newBuilder()
            .setUserId(userId)
            .build()

        asyncStub.withInterceptors(
            MetadataUtils.newAttachHeadersInterceptor(metadata)
        ).getUserCredentials(request) { response, error ->
            callback(response, error)
        }
    }

    /**
     * Асинхронний виклик validateBiometricSession
     */
    fun validateBiometricSessionAsync(
        bioSessionId: String,
        userId: String,
        metadata: Metadata,
        callback: (BiometricSessionResponse?, StatusRuntimeException?) -> Unit
    ) {
        val request = BiometricSessionRequest.newBuilder()
            .setBioSessionId(bioSessionId)
            .setUserId(userId)
            .build()

        asyncStub.withInterceptors(
            MetadataUtils.newAttachHeadersInterceptor(metadata)
        ).validateBiometricSession(request) { response, error ->
            callback(response, error)
        }
    }

    /**
     * Закриття каналу
     */
    fun shutdown() {
        try {
            channel.shutdown()
                .awaitTermination(5, TimeUnit.SECONDS)
        } catch (e: InterruptedException) {
            logger.error("Error shutting down gRPC channel", e)
            Thread.currentThread().interrupt()
            throw RuntimeException("Error shutting down gRPC channel", e)
        }
    }
}