# SCM Client SDK

Kotlin SDK для інтеграції з SCM-service. Надає простий інтерфейс для виклику SCM операцій з автоматичною обробкою помилок, авторизацією та resilience механізмами.

## Особливості

- **Автоматична авторизація**: Збирає JWT токен, IP адресу, User-Agent та інший контекст
- **Resilience4j Circuit Breaker**: Стійкість до відмов SCM сервісу
- **Error Mapping**: Перетворення gRPC PERMISSION_DENIED у зрозумілий для бізнесу виняток
- **Automatic Logging**: Логування кожного запиту для легкого дебагу
- **Context Interceptor**: Автоматичне зчитування метаданих з поточного контексту запиту
- **Biometric Authentication**: Підтримка FaceID, TouchID, WebAuthn для критичних операцій
- **Step-up Authentication**: Автоматичне ініціювання біометричної перевірки для високоризикових дій

## Встановлення

Додайте залежність у ваш `build.gradle.kts`:

```kotlin
dependencies {
    implementation("com.nodeorb:scmclient:1.0.0")
}
```

## Швидкий старт

### Створення клієнта

```kotlin
import com.nodeorb.scmclient.SCMClientFactory

// Для production середовища
val scmClient = SCMClientFactory.createProductionClient(
    host = "scm-service",
    port = 9090
)

// Для development середовища
val devClient = SCMClientFactory.createDevelopmentClient(
    host = "localhost",
    port = 9090
)
```

### Валідація вартості операції

```kotlin
import com.nodeorb.scmclient.exception.ScmSecurityBlockException

try {
    val result = scmClient.validateCost(
        userId = "user-123",
        orderId = "order-456",
        category = "BID_PLACEMENT",
        value = 1000.0,
        lat = 50.4501,
        lon = 30.5234
    )
    
    if (result.allowed) {
        // Операція дозволена
        println("Bid placement allowed")
    } else {
        // Операція заблокована
        println("Bid placement blocked: ${result.reason}")
    }
    
} catch (e: ScmSecurityBlockException) {
    // Обробка блокування SCM
    println("Security block: ${e.reason}")
    
    // Повертаємо деталі блокування для фронтенду
    val blockDetails = e.getBlockDetails()
    showAppealForm(blockDetails)
}
```

### Валідація вартості з біометричною перевіркою

```kotlin
import com.nodeorb.scmclient.exception.BiometricException
import com.nodeorb.scmclient.model.EnhancedValidationResult

try {
    val result = scmClient.validateCostWithBiometric(
        userId = "user-123",
        orderId = "order-456",
        category = "BID_PLACEMENT",
        value = 50000.0, // Висока сума
        lat = 50.4501,
        lon = 30.5234
    )
    
    when (result.biometricStatus) {
        BiometricStatus.NOT_REQUIRED -> {
            // Біометрична перевірка не потрібна
            if (result.allowed) {
                println("Operation allowed")
            }
        }
        BiometricStatus.REQUIRED -> {
            // Потрібна біометрична перевірка
            throw BiometricException(
                message = "High risk operation requires biometric verification",
                challenge = result.biometricChallenge!!,
                operation = "BID_PLACEMENT"
            )
        }
        BiometricStatus.VERIFIED -> {
            // Операція дозволена після біометричної перевірки
            println("Operation allowed after biometric verification")
        }
    }
    
} catch (e: BiometricException) {
    // Повертаємо challenge для фронтенду
    val challenge = e.getBiometricDetails().challenge
    showBiometricAuthForm(challenge)
}
```

### Підтвердження біометричної аутентифікації

```kotlin
val result = scmClient.confirmWithBiometrics(
    userId = "user-123",
    signedChallenge = "signed-challenge-from-device",
    bioSessionId = "session-123"
)

if (result.success) {
    println("Biometric authentication successful")
} else {
    println("Biometric authentication failed: ${result.errorMessage}")
}
```

### Подання апеляції з біометричним підписом

```kotlin
val appeal = AppealRequestWithSignature(
    recordHash = "hash-123",
    text = "Ця ставка є обґрунтованою...",
    evidenceUrl = "https://example.com/evidence.pdf",
    biometricSignature = "signed-challenge",
    bioSessionId = "session-123"
)

val success = scmClient.submitAppealWithSignature(appeal)

if (success) {
    println("Appeal submitted successfully")
} else {
    println("Failed to submit appeal")
}
```

### WebAuthn аутентифікація

```kotlin
// Генерація WebAuthn challenge
val challenge = biometricHandler.generateWebAuthnChallenge("user-123")

// Перевірка WebAuthn підпису
val signedChallenge = SignedChallenge(
    challengeId = challenge.challengeId,
    signedChallenge = "signed-challenge",
    credentialId = "credential-123",
    userHandle = "user-123",
    authenticatorData = "authenticator-data",
    signature = "signature"
)

val result = biometricHandler.verifyWebAuthnSignature(signedChallenge, "user-123")

if (result.success) {
    println("WebAuthn authentication successful")
} else {
    println("WebAuthn authentication failed")
}
```

## Конфігурація

### Базова конфігурація

```kotlin
import com.nodeorb.scmclient.config.SCMClientConfig

val config = SCMClientConfig(
    host = "scm-service",
    port = 9090,
    maxRetries = 3,
    defaultAllowOnFailure = true, // FAIL_OPEN стратегія
    enableLogging = true
)

val client = SCMClient(config)
```

### Production конфігурація

```kotlin
val config = SCMClientConfig(
    host = "scm-service-prod",
    port = 9090,
    maxRetries = 5,
    defaultAllowOnFailure = false, // FAIL_CLOSED стратегія
    enableLogging = true,
    logLevel = "INFO",
    circuitBreakerConfig = CircuitBreakerConfig(
        failureRateThreshold = 50.0,
        minimumNumberOfCalls = 10,
        waitDurationInOpenState = Duration.ofSeconds(60),
        slidingWindowSize = 20,
        enabled = true
    )
)
```

## Biometric Authentication

### Biometric Challenge Flow

SDK автоматично ініціює біометричну перевірку для критичних дій:

1. **Risk Assessment**: Якщо riskScore >= 60.0 та операція заблокована
2. **Challenge Generation**: Генерується біометричний challenge
3. **Frontend Integration**: Challenge передається на фронтенд для FaceID/WebAuthn
4. **Signature Verification**: Підпис перевіряється на сервері
5. **Evidence Package**: Створюється Evidence Package для аудиту

### Biometric Types Support

- **FaceID**: Для iOS пристроїв
- **TouchID**: Для iOS пристроїв
- **Fingerprint**: Для Android пристроїв
- **WebAuthn**: Для веб-браузерів
- **Iris Scan**: Для спеціалізованих пристроїв

### WebAuthn Integration

```kotlin
// Реєстрація WebAuthn credential
val success = biometricHandler.saveWebAuthnCredential(
    userId = "user-123",
    credentialId = "credential-123",
    publicKeyPem = "-----BEGIN PUBLIC KEY-----...",
    authenticatorType = AuthenticatorType.PLATFORM,
    deviceName = "iPhone 14 Pro"
)

// Отримання credentials
val credentials = biometricHandler.getUserCredentials("user-123")
```

## Context Interceptor

SDK автоматично збирає контекст з поточного потоку:

```kotlin
import com.nodeorb.scmclient.interceptor.ContextInterceptor

// Встановлюємо контекст для поточного запиту
val context = ContextInterceptor.RequestContext(
    userId = "user-123",
    ipAddress = "192.168.1.1",
    userAgent = "Mozilla/5.0...",
    geoLat = 50.4501,
    geoLon = 30.5234,
    deviceId = "device-123",
    requestId = "req-456"
)

ContextInterceptor().setContext(context)

// Всі наступні виклики SCM будуть містити цей контекст
val result = scmClient.validateCost(...)
```

## Resilience (Circuit Breaker)

SDK використовує Resilience4j для забезпечення стійкості:

### Стратегії поведінки при недоступності SCM

- **FAIL_OPEN** (`defaultAllowOnFailure = true`): Дозволяє дію, але ставить мітку `scmOffline`
- **FAIL_CLOSED** (`defaultAllowOnFailure = false`): Блокує дію при недоступності SCM

### Приклад обробки недоступності

```kotlin
val result = scmClient.validateCost(...)

if (result.scmOffline) {
    // SCM недоступний, використовуємо fallback стратегію
    if (result.allowed) {
        // Дія дозволена (FAIL_OPEN)
        logger.warn("SCM offline, using default allow policy")
    } else {
        // Дія заборонена (FAIL_CLOSED)
        logger.warn("SCM offline, using default deny policy")
    }
}
```

### Метрики Circuit Breaker

```kotlin
val metrics = scmClient.getCircuitBreakerMetrics()

println("""
    Circuit Breaker Status:
    - State: ${metrics.state}
    - Failure Rate: ${metrics.failureRate}%
    - Total Calls: ${metrics.numberOfCalls}
""".trimIndent())
```

## Error Handling

### ScmSecurityBlockException

Перетворює gRPC `PERMISSION_DENIED` у зрозумілий для бізнесу виняток:

```kotlin
try {
    val result = scmClient.validateCost(...)
} catch (e: ScmSecurityBlockException) {
    // Обробка блокування
    val blockDetails = e.getBlockDetails()
    
    // Показ форму апеляції на фронтенді
    showAppealForm(
        reason = blockDetails.reason,
        operation = blockDetails.operation,
        message = blockDetails.message
    )
}
```

### BiometricException

Ініціює біометричну перевірку для критичних дій:

```kotlin
try {
    val result = scmClient.validateCostWithBiometric(...)
} catch (e: BiometricException) {
    // Повертаємо challenge для фронтенду
    val challenge = e.getBiometricDetails().challenge
    showBiometricAuthForm(challenge)
}
```

### Error Mapping

- `PERMISSION_DENIED` → `ScmSecurityBlockException`
- `UNAVAILABLE`, `DEADLINE_EXCEEDED` → `ValidationResult` з `scmOffline = true`
- `Biometric verification required` → `BiometricException`
- Інші помилки → Проброс через

## Логування

SDK автоматично логує кожен запит:

```
INFO  SCM Client: validateCost - userId: user-123, orderId: order-456, category: BID_PLACEMENT, value: 1000.0, lat: 50.4501, lon: 30.5234
INFO  SCM Client: validateCost - userId: user-123, orderId: order-456, allowed: false, reason: Risk score too high, riskScore: 85.0, policyId: policy-123, scmOffline: false
INFO  Biometric Challenge: Generated challenge for user: user-123, session: session-123, type: FACE_ID
INFO  Biometric Verification: Successful verification for user: user-123, session: session-123
```

### Рівні логування

- `DEBUG`: Детальне логування для development
- `INFO`: Загальна інформація про роботу
- `WARN`: Попередження про проблеми
- `ERROR`: Критичні помилки

## Evidence Package

Для юридичної доказовості SDK створює Evidence Package:

```kotlin
val evidencePackage = EvidencePackage(
    operationId = "operation-123",
    userId = "user-123",
    operationType = "BIOMETRIC_AUTHENTICATION",
    timestamp = Instant.now(),
    ipAddress = "192.168.1.1",
    userAgent = "Mozilla/5.0...",
    geoLocation = GeoLocation(
        latitude = 50.4501,
        longitude = 30.5234
    ),
    biometricEvidence = BiometricallySignedEvidence(
        biometricallySigned = true,
        authType = BiometricAuthType.FACE_ID,
        bioSessionId = "session-123",
        signaturePayload = "signed-challenge",
        signedAt = Instant.now()
    )
)
```

## Приклад використання у Spring Boot

```kotlin
@Service
class BiometricBidService {
    
    private val scmClient = SCMClientFactory.createProductionClient(
        host = "scm-service",
        port = 9090
    )
    
    fun placeHighRiskBid(bidRequest: HighRiskBidRequest): BidResult {
        return try {
            // Встановлюємо контекст з HTTP запиту
            setRequestContext(bidRequest)
            
            // Виконуємо валідацію з біометричною перевіркою
            val result = scmClient.validateCostWithBiometric(
                userId = bidRequest.userId,
                orderId = bidRequest.orderId,
                category = "BID_PLACEMENT",
                value = bidRequest.amount,
                lat = bidRequest.latitude,
                lon = bidRequest.longitude
            )
            
            when (result.biometricStatus) {
                BiometricStatus.REQUIRED -> {
                    // Повертаємо BiometricException для фронтенду
                    throw BiometricException(
                        message = "High risk bid requires biometric verification",
                        challenge = result.biometricChallenge!!,
                        operation = "BID_PLACEMENT"
                    )
                }
                BiometricStatus.VERIFIED -> {
                    // Створюємо ставку
                    createBid(bidRequest)
                }
                else -> {
                    throw ScmSecurityBlockException(
                        message = "Bid placement blocked",
                        reason = result.reason,
                        operation = "BID_PLACEMENT"
                    )
                }
            }
            
        } catch (e: BiometricException) {
            // Логуємо потребу біометричної перевірки
            logger.warn("Biometric verification required for bid: ${e.message}")
            throw e
        }
    }
    
    fun confirmBiometricBid(
        userId: String,
        signedChallenge: String,
        bioSessionId: String
    ): AuthResult {
        return scmClient.confirmWithBiometrics(
            userId = userId,
            signedChallenge = signedChallenge,
            bioSessionId = bioSessionId
        )
    }
    
    private fun setRequestContext(bidRequest: HighRiskBidRequest) {
        val context = ContextInterceptor.RequestContext(
            userId = bidRequest.userId,
            ipAddress = bidRequest.ipAddress,
            userAgent = bidRequest.userAgent,
            geoLat = bidRequest.latitude,
            geoLon = bidRequest.longitude,
            deviceId = bidRequest.deviceId,
            requestId = bidRequest.requestId
        )
        
        ContextInterceptor().setContext(context)
    }
}
```

## Тестування

```kotlin
@Test
fun `validateCostWithBiometric should return required status for high risk`() {
    val client = SCMClientFactory.createTestClient("localhost", 9090)
    
    val result = client.validateCostWithBiometric(
        userId = "test-user",
        orderId = "test-order",
        category = "BID_PLACEMENT",
        value = 100000.0, // Висока сума
        lat = 0.0,
        lon = 0.0
    )
    
    assertEquals(BiometricStatus.REQUIRED, result.biometricStatus)
    assertNotNull(result.biometricChallenge)
}

@Test
fun `confirmWithBiometrics should return success for valid signature`() {
    val client = SCMClientFactory.createTestClient("localhost", 9090)
    
    val result = client.confirmWithBiometrics(
        userId = "test-user",
        signedChallenge = "valid-signed-challenge",
        bioSessionId = "test-session"
    )
    
    assertTrue(result.success)
    assertEquals(BiometricAuthType.FACE_ID, result.authType)
}
```

## Моніторинг

### Health Check

```kotlin
fun checkSCMHealth(): Health {
    return try {
        val metrics = scmClient.getCircuitBreakerMetrics()
        
        when (metrics.state) {
            "CLOSED" -> Health.up("SCM Service is healthy")
                .withDetail("state", metrics.state)
                .withDetail("failureRate", "${metrics.failureRate}%")
                .build()
            "OPEN" -> Health.down("SCM Service is unavailable")
                .withDetail("state", metrics.state)
                .withDetail("notPermittedCalls", metrics.numberOfNotPermittedCalls)
                .build()
            else -> Health.unknown("SCM Service state is unknown")
                .withDetail("state", metrics.state)
                .build()
        }
    } catch (e: Exception) {
        Health.down("SCM Service health check failed")
            .withException(e)
            .build()
    }
}
```

### Biometric Metrics

```kotlin
fun getBiometricMetrics(): Map<String, Any> {
    return mapOf(
        "active_sessions" to biometricHandler.getActiveSessionCount(),
        "challenge_expiry_seconds" to 300,
        "webauthn_credentials" to biometricHandler.getTotalCredentials(),
        "verification_success_rate" to biometricHandler.getVerificationSuccessRate()
    )
}
```

## Підтримка

Для питань та пропозицій звертайтеся до команди розробки SCM.

## Ліцензія

Цей SDK поширюється під ліцензією MIT.