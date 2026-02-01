package com.nodeorb.scm

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing

@SpringBootApplication(scanBasePackages = ["com.nodeorb"])
@EntityScan("com.nodeorb.common.model")
@EnableJpaAuditing
class ScmServiceApplication

fun main(args: Array<String>) {
    runApplication<ScmServiceApplication>(*args)
}
