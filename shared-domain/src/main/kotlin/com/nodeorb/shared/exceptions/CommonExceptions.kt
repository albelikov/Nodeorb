package com.nodeorb.shared.exceptions

open class NodeorbException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)

class EntityNotFoundException(entityType: String, entityId: Any) :
    NodeorbException("Entity $entityType with id $entityId not found")

class InvalidEntityStateException(entityType: String, currentStatus: String, desiredStatus: String) :
    NodeorbException("Cannot transition $entityType from $currentStatus to $desiredStatus")

class ValidationException(field: String, message: String) :
    NodeorbException("Validation failed for field $field: $message")

class BusinessRuleViolationException(rule: String, message: String) :
    NodeorbException("Business rule violation: $rule - $message")