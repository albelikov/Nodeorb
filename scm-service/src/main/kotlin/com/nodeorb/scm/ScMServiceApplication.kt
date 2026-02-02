package com.nodeorb.scm

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing

@SpringBootApplication
@EnableJpaAuditing
class ScmServiceApplication

fun main(args: Array<String>) {
    runApplication<ScmServiceApplication>(*args)
}