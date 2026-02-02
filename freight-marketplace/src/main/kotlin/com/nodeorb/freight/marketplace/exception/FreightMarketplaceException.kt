package com.nodeorb.freight.marketplace.exception

open class FreightMarketplaceException(message: String) : RuntimeException(message)

class OrderNotFoundException(message: String = "Order not found") : FreightMarketplaceException(message)

class BidNotFoundException(message: String = "Bid not found") : FreightMarketplaceException(message)

class ValidationException(message: String = "Validation failed") : FreightMarketplaceException(message)

class AuthorizationException(message: String = "Authorization required") : FreightMarketplaceException(message)