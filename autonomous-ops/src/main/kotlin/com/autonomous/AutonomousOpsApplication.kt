package com.autonomous

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan

@SpringBootApplication
@ComponentScan(basePackages = ["com.autonomous"])
class AutonomousOpsApplication

fun main(args: Array<String>) {
    runApplication<AutonomousOpsApplication>(*args)
}