package logistics.admin.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "services")
data class ServiceConfig(
    var oms: ServiceUrl = ServiceUrl(),
    var wms: ServiceUrl = ServiceUrl(),
    var tms: ServiceUrl = ServiceUrl(),
    var fms: ServiceUrl = ServiceUrl(),
    var yms: ServiceUrl = ServiceUrl(),
    var transportPlatform: ServiceUrl = ServiceUrl(),
    var gisSubsystem: ServiceUrl = ServiceUrl(),
    var customs: ServiceUrl = ServiceUrl(),
    var freightMarketplace: ServiceUrl = ServiceUrl(),
    var autonomousOps: ServiceUrl = ServiceUrl(),
    var reverseLogistics: ServiceUrl = ServiceUrl(),
    var scmIam: ServiceUrl = ServiceUrl(),
    var scmDataProtection: ServiceUrl = ServiceUrl(),
    var scmAudit: ServiceUrl = ServiceUrl(),
    var cyberResilience: ServiceUrl = ServiceUrl()
)

data class ServiceUrl(
    var url: String = "http://localhost:8080",
    var enabled: Boolean = true
)

