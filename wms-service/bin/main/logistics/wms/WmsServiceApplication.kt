package logistics.wms

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class WmsServiceApplication

fun main(args: Array<String>) {
    runApplication<WmsServiceApplication>(*args)
}

