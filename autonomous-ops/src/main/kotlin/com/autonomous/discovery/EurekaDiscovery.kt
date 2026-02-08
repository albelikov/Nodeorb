package com.autonomous.discovery

import org.springframework.cloud.client.discovery.DiscoveryClient
import org.springframework.stereotype.Service

@Service
class EurekaDiscovery(
    private val discoveryClient: DiscoveryClient
) {

    fun getServiceInstances(serviceName: String): List<ServiceInstance> {
        return discoveryClient.getInstances(serviceName).map { instance ->
            ServiceInstance(
                serviceId = instance.serviceId,
                host = instance.host,
                port = instance.port,
                uri = instance.uri.toString()
            )
        }
    }

    fun getServiceUrl(serviceName: String): String? {
        return getServiceInstances(serviceName).firstOrNull()?.uri
    }

    fun listRegisteredServices(): List<String> {
        return discoveryClient.services
    }

    fun isServiceAvailable(serviceName: String): Boolean {
        return getServiceInstances(serviceName).isNotEmpty()
    }
}

data class ServiceInstance(
    val serviceId: String,
    val host: String,
    val port: Int,
    val uri: String
)