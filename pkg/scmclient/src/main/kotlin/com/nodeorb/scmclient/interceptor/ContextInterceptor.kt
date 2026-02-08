package com.nodeorb.scmclient.interceptor

import io.grpc.Metadata
import io.grpc.ServerCall
import io.grpc.ServerCallHandler
import io.grpc.ServerInterceptor
import io.grpc.Context
import org.slf4j.LoggerFactory
import java.util.*

/**
 * Інтерцептор для автоматичного зчитування та передачі метаданих
 * Збирає JWT токен, IP адресу, User-Agent та інший контекст
 */
class ContextInterceptor : ServerInterceptor {
    
    companion object {
        private val logger = LoggerFactory.getLogger(ContextInterceptor::class.java)
        
        // Ключі метаданих
        const val JWT_TOKEN_KEY = "x-jwt-token"
        const val USER_ID_KEY = "x-user-id"
        const val IP_ADDRESS_KEY = "x-ip-address"
        const val USER_AGENT_KEY = "x-user-agent"
        const val GEO_LAT_KEY = "x-geo-lat"
        const val GEO_LON_KEY = "x-geo-lon"
        const val DEVICE_ID_KEY = "x-device-id"
        const val REQUEST_ID_KEY = "x-request-id"
        
        // Контекстні ключі
        val JWT_TOKEN_CONTEXT_KEY = Context.key<String>(JWT_TOKEN_KEY)
        val USER_ID_CONTEXT_KEY = Context.key<String>(USER_ID_KEY)
        val IP_ADDRESS_CONTEXT_KEY = Context.key<String>(IP_ADDRESS_KEY)
        val USER_AGENT_CONTEXT_KEY = Context.key<String>(USER_AGENT_KEY)
        val GEO_LAT_CONTEXT_KEY = Context.key<Double>(GEO_LAT_KEY)
        val GEO_LON_CONTEXT_KEY = Context.key<Double>(GEO_LON_KEY)
        val DEVICE_ID_CONTEXT_KEY = Context.key<String>(DEVICE_ID_KEY)
        val REQUEST_ID_CONTEXT_KEY = Context.key<String>(REQUEST_ID_KEY)
    }

    /**
     * Створює метадані з контексту для gRPC виклику
     */
    fun createMetadata(context: Any): Metadata {
        val metadata = Metadata()
        
        // Додаємо контекст з поточного ThreadLocal
        val currentContext = getCurrentContext()
        
        currentContext.jwtToken?.let {
            metadata.put(Metadata.Key.of(JWT_TOKEN_KEY, Metadata.ASCII_STRING_MARSHALLER), it)
        }
        
        currentContext.userId?.let {
            metadata.put(Metadata.Key.of(USER_ID_KEY, Metadata.ASCII_STRING_MARSHALLER), it)
        }
        
        currentContext.ipAddress?.let {
            metadata.put(Metadata.Key.of(IP_ADDRESS_KEY, Metadata.ASCII_STRING_MARSHALLER), it)
        }
        
        currentContext.userAgent?.let {
            metadata.put(Metadata.Key.of(USER_AGENT_KEY, Metadata.ASCII_STRING_MARSHALLER), it)
        }
        
        currentContext.geoLat?.let {
            metadata.put(Metadata.Key.of(GEO_LAT_KEY, Metadata.DOUBLE_MARSHALLER), it)
        }
        
        currentContext.geoLon?.let {
            metadata.put(Metadata.Key.of(GEO_LON_KEY, Metadata.DOUBLE_MARSHALLER), it)
        }
        
        currentContext.deviceId?.let {
            metadata.put(Metadata.Key.of(DEVICE_ID_KEY, Metadata.ASCII_STRING_MARSHALLER), it)
        }
        
        currentContext.requestId?.let {
            metadata.put(Metadata.Key.of(REQUEST_ID_KEY, Metadata.ASCII_STRING_MARSHALLER), it)
        }
        
        return metadata
    }

    /**
     * Отримує контекст з поточного потоку
     */
    private fun getCurrentContext(): RequestContext {
        return RequestContextHolder.context ?: RequestContext()
    }

    /**
     * Встановлює контекст для поточного потоку
     */
    fun setContext(context: RequestContext) {
        RequestContextHolder.context = context
    }

    /**
     * Очищає контекст поточного потоку
     */
    fun clearContext() {
        RequestContextHolder.context = null
    }

    /**
     * Інтерцептор для серверних викликів
     */
    override fun <ReqT, RespT> interceptCall(
        call: ServerCall<ReqT, RespT>,
        headers: Metadata,
        next: ServerCallHandler<ReqT, RespT>
    ): ServerCall.Listener<ReqT> {
        try {
            // Зчитуємо метадані з заголовків
            val requestContext = extractContextFromHeaders(headers)
            
            // Встановлюємо контекст у ThreadLocal
            setContext(requestContext)
            
            logger.debug("Extracted context from headers: $requestContext")
            
            // Продовжуємо обробку запиту
            val listener = next.startCall(call, headers)
            
            // Повертаємо обгортку, яка очищає контекст після завершення
            return ContextClearingListener(listener)
            
        } catch (e: Exception) {
            logger.error("Error intercepting call", e)
            throw e
        }
    }

    /**
     * Витягує контекст з gRPC заголовків
     */
    private fun extractContextFromHeaders(headers: Metadata): RequestContext {
        return RequestContext(
            jwtToken = headers.get(Metadata.Key.of(JWT_TOKEN_KEY, Metadata.ASCII_STRING_MARSHALLER)),
            userId = headers.get(Metadata.Key.of(USER_ID_KEY, Metadata.ASCII_STRING_MARSHALLER)),
            ipAddress = headers.get(Metadata.Key.of(IP_ADDRESS_KEY, Metadata.ASCII_STRING_MARSHALLER)),
            userAgent = headers.get(Metadata.Key.of(USER_AGENT_KEY, Metadata.ASCII_STRING_MARSHALLER)),
            geoLat = headers.get(Metadata.Key.of(GEO_LAT_KEY, Metadata.DOUBLE_MARSHALLER)),
            geoLon = headers.get(Metadata.Key.of(GEO_LON_KEY, Metadata.DOUBLE_MARSHALLER)),
            deviceId = headers.get(Metadata.Key.of(DEVICE_ID_KEY, Metadata.ASCII_STRING_MARSHALLER)),
            requestId = headers.get(Metadata.Key.of(REQUEST_ID_KEY, Metadata.ASCII_STRING_MARSHALLER))
        )
    }

    /**
     * Обгортка для очищення контексту після завершення виклику
     */
    private class ContextClearingListener<ReqT>(
        private val delegate: ServerCall.Listener<ReqT>
    ) : ServerCall.Listener<ReqT>() {
        
        override fun onMessage(message: ReqT) {
            try {
                delegate.onMessage(message)
            } finally {
                RequestContextHolder.context = null
            }
        }
        
        override fun onHalfClose() {
            try {
                delegate.onHalfClose()
            } finally {
                RequestContextHolder.context = null
            }
        }
        
        override fun onCancel() {
            try {
                delegate.onCancel()
            } finally {
                RequestContextHolder.context = null
            }
        }
        
        override fun onComplete() {
            try {
                delegate.onComplete()
            } finally {
                RequestContextHolder.context = null
            }
        }
        
        override fun onReady() {
            delegate.onReady()
        }
    }

    /**
     * Контекст запиту
     */
    data class RequestContext(
        val jwtToken: String? = null,
        val userId: String? = null,
        val ipAddress: String? = null,
        val userAgent: String? = null,
        val geoLat: Double? = null,
        val geoLon: Double? = null,
        val deviceId: String? = null,
        val requestId: String? = null
    )

    /**
     * ThreadLocal для зберігання контексту
     */
    private object RequestContextHolder {
        private val contextHolder = ThreadLocal<RequestContext>()
        
        var context: RequestContext?
            get() = contextHolder.get()
            set(value) = contextHolder.set(value)
        
        fun clear() {
            contextHolder.remove()
        }
    }
}