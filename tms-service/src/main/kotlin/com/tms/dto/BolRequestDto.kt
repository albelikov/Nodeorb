package com.tms.dto

data class BolRequestDto(
    val shipperInfo: ShipperInfoDto,
    val consigneeInfo: ConsigneeInfoDto,
    val cargoDetails: List<CargoItemDto>,
    val instructions: String?
)

data class ShipperInfoDto(
    val name: String,
    val address: String,
    val city: String,
    val state: String,
    val zipCode: String,
    val country: String,
    val contact: String,
    val phone: String,
    val email: String
)

data class ConsigneeInfoDto(
    val name: String,
    val address: String,
    val city: String,
    val state: String,
    val zipCode: String,
    val country: String,
    val contact: String,
    val phone: String,
    val email: String
)

data class CargoItemDto(
    val description: String,
    val weight: Double,
    val volume: Double,
    val packageCount: Int,
    val type: String
)