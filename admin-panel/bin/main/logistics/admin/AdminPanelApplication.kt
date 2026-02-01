package logistics.admin

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling
import logistics.admin.config.ServiceConfig

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(ServiceConfig::class)
class AdminPanelApplication

fun main(args: Array<String>) {
    runApplication<AdminPanelApplication>(*args)
}

