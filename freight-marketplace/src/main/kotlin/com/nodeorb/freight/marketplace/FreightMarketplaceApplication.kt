package com.nodeorb.freight.marketplace

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableConfigurationProperties(FreightMarketplaceProperties::class)
class FreightMarketplaceApplication

fun main(args: Array<String>) {
    runApplication<FreightMarketplaceApplication>(*args)
}