package logistics.autonomous

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class AutonomousOpsApplication

fun main(args: Array<String>) {
    runApplication<AutonomousOpsApplication>(*args)
}

