package logistics.tms

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class TmsServiceApplication

fun main(args: Array<String>) {
    runApplication<TmsServiceApplication>(*args)
}

