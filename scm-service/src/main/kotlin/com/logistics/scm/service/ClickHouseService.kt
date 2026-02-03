package com.logistics.scm.service

import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.SQLException

class ClickHouseService {
    
    private val clickHouseUrl: String = System.getenv("CLICKHOUSE_URL") 
        ?: "jdbc:clickhouse://localhost:8123/default"
    
    private val clickHouseUser: String = System.getenv("CLICKHOUSE_USER") ?: "default"
    private val clickHousePassword: String = System.getenv("CLICKHOUSE_PASSWORD") ?: ""
    
    private fun getConnection(): Connection {
        return DriverManager.getConnection(clickHouseUrl, clickHouseUser, clickHousePassword)
    }
    
    fun createAuditLogTable() {
        val createTableSQL = """
            CREATE TABLE IF NOT EXISTS scm_audit_log (
                event_time DateTime,
                order_id String,
                risk_verdict String,
                input_data String,
                current_hash String,
                prev_hash Nullable(String),
                event_type String
            ) ENGINE = MergeTree()
            ORDER BY (event_time, order_id, current_hash)
            SETTINGS index_granularity = 8192
        """.trimIndent()
        
        getConnection().use { connection ->
            connection.createStatement().use { statement ->
                statement.execute(createTableSQL)
            }
        }
    }
    
    fun createEvidenceLogTable() {
        val createTableSQL = """
            CREATE TABLE IF NOT EXISTS scm_evidence_log (
                order_id String,
                evidence_package String,
                created_at DateTime
            ) ENGINE = MergeTree()
            ORDER BY (order_id, created_at)
            SETTINGS index_granularity = 8192
        """.trimIndent()
        
        getConnection().use { connection ->
            connection.createStatement().use { statement ->
                statement.execute(createTableSQL)
            }
        }
    }
    
    fun insertAuditEvent(event: AuditExportWorker.AuditEvent) {
        val insertSQL = """
            INSERT INTO scm_audit_log (
                event_time, order_id, risk_verdict, input_data, current_hash, prev_hash, event_type
            ) VALUES (?, ?, ?, ?, ?, ?, ?)
        """.trimIndent()
        
        getConnection().use { connection ->
            connection.prepareStatement(insertSQL).use { statement ->
                statement.setString(1, event.eventTime)
                statement.setString(2, event.orderId)
                statement.setString(3, event.riskVerdict)
                statement.setString(4, event.inputData)
                statement.setString(5, event.currentHash)
                statement.setString(6, event.prevHash)
                statement.setString(7, event.eventType)
                
                statement.executeUpdate()
            }
        }
    }
    
    fun insertEvidencePackage(orderId: String, evidencePackageJson: String) {
        val insertSQL = """
            INSERT INTO scm_evidence_log (
                order_id, evidence_package, created_at
            ) VALUES (?, ?, ?)
        """.trimIndent()
        
        getConnection().use { connection ->
            connection.prepareStatement(insertSQL).use { statement ->
                statement.setString(1, orderId)
                statement.setString(2, evidencePackageJson)
                statement.setString(3, java.time.LocalDateTime.now().toString())
                
                statement.executeUpdate()
            }
        }
    }
    
    fun getAuditEventsByOrderId(orderId: String): List<AuditExportWorker.AuditEvent> {
        val selectSQL = """
            SELECT event_time, order_id, risk_verdict, input_data, current_hash, prev_hash, event_type
            FROM scm_audit_log
            WHERE order_id = ?
            ORDER BY event_time
        """.trimIndent()
        
        val events = mutableListOf<AuditExportWorker.AuditEvent>()
        
        getConnection().use { connection ->
            connection.prepareStatement(selectSQL).use { statement ->
                statement.setString(1, orderId)
                
                statement.executeQuery().use { resultSet ->
                    while (resultSet.next()) {
                        val event = AuditExportWorker.AuditEvent(
                            eventTime = resultSet.getString("event_time"),
                            orderId = resultSet.getString("order_id"),
                            riskVerdict = resultSet.getString("risk_verdict"),
                            inputData = resultSet.getString("input_data"),
                            currentHash = resultSet.getString("current_hash"),
                            prevHash = resultSet.getString("prev_hash"),
                            eventType = resultSet.getString("event_type")
                        )
                        events.add(event)
                    }
                }
            }
        }
        
        return events
    }
    
    fun getEvidencePackageByOrderId(orderId: String): String? {
        val selectSQL = """
            SELECT evidence_package
            FROM scm_evidence_log
            WHERE order_id = ?
            ORDER BY created_at DESC
            LIMIT 1
        """.trimIndent()
        
        getConnection().use { connection ->
            connection.prepareStatement(selectSQL).use { statement ->
                statement.setString(1, orderId)
                
                statement.executeQuery().use { resultSet ->
                    if (resultSet.next()) {
                        return resultSet.getString("evidence_package")
                    }
                }
            }
        }
        
        return null
    }
    
    fun verifyWORMCompliance(): Boolean {
        try {
            // Test that UPDATE operations are not allowed
            val updateSQL = """
                ALTER TABLE scm_audit_log UPDATE risk_verdict = 'TEST' WHERE order_id = 'TEST'
            """.trimIndent()
            
            getConnection().use { connection ->
                connection.createStatement().use { statement ->
                    try {
                        statement.execute(updateSQL)
                        return false // If update succeeds, WORM is not enforced
                    } catch (e: SQLException) {
                        // Expected for WORM compliance
                    }
                }
            }
            
            // Test that DELETE operations are not allowed
            val deleteSQL = """
                ALTER TABLE scm_audit_log DELETE WHERE order_id = 'TEST'
            """.trimIndent()
            
            getConnection().use { connection ->
                connection.createStatement().use { statement ->
                    try {
                        statement.execute(deleteSQL)
                        return false // If delete succeeds, WORM is not enforced
                    } catch (e: SQLException) {
                        // Expected for WORM compliance
                    }
                }
            }
            
            return true
        } catch (e: Exception) {
            return false
        }
    }
    
    fun getTableSettings(): Map<String, String> {
        val settings = mutableMapOf<String, String>()
        
        val selectSQL = """
            SELECT name, value
            FROM system.settings
            WHERE name IN ('allow_experimental_lightweight_delete', 'allow_experimental_alter_update', 'allow_experimental_alter_delete')
        """.trimIndent()
        
        getConnection().use { connection ->
            connection.createStatement().use { statement ->
                statement.executeQuery(selectSQL).use { resultSet ->
                    while (resultSet.next()) {
                        settings[resultSet.getString("name")] = resultSet.getString("value")
                    }
                }
            }
        }
        
        return settings
    }
}