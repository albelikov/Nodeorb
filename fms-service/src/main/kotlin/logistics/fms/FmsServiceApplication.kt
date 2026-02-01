package logistics.fms

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class FmsServiceApplication

fun main(args: Array<String>) {
    runApplication<FmsServiceApplication>(*args)
}

