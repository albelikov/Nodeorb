package com.autonomous.data

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import redis.clients.jedis.JedisPool
import redis.clients.jedis.JedisPoolConfig

@Service
class DatabaseAccess(
    @Value("\${spring.datasource.url:jdbc:postgresql://localhost:5432/nodeorb}")
    private val dbUrl: String,
    @Value("\${spring.datasource.username:postgres}")
    private val dbUser: String,
    @Value("\${spring.datasource.password:password}")
    private val dbPassword: String,
    @Value("\${spring.data.redis.host:localhost}")
    private val redisHost: String,
    @Value("\${spring.data.redis.port:6379}")
    private val redisPort: Int
) {

    private val jedisPool: JedisPool

    init {
        val poolConfig = JedisPoolConfig()
        poolConfig.maxTotal = 100
        poolConfig.maxIdle = 50
        poolConfig.minIdle = 10
        jedisPool = JedisPool(poolConfig, redisHost, redisPort)
    }

    fun getCachedData(key: String): String? {
        jedisPool.resource.use { jedis ->
            return jedis.get(key)
        }
    }

    fun cacheData(key: String, value: String, ttl: Int = 3600) {
        jedisPool.resource.use { jedis ->
            jedis.setex(key, ttl.toLong(), value)
        }
    }

    fun removeCachedData(key: String) {
        jedisPool.resource.use { jedis ->
            jedis.del(key)
        }
    }

    fun isCached(key: String): Boolean {
        jedisPool.resource.use { jedis ->
            return jedis.exists(key)
        }
    }

    fun getDbUrl(): String {
        return dbUrl
    }

    fun getDbUser(): String {
        return dbUser
    }

    fun getDbPassword(): String {
        return dbPassword
    }
}