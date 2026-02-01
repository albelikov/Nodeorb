package logistics.admin.controller

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
class UserManagementController {
    
    @GetMapping
    fun getAllUsers(): List<Map<String, Any>> {
        // TODO: Интеграция с scm-iam
        return emptyList()
    }
    
    @GetMapping("/{id}")
    fun getUser(@PathVariable id: String): Map<String, Any> {
        // TODO: Интеграция с scm-iam
        return emptyMap()
    }
    
    @PostMapping
    fun createUser(@RequestBody userData: Map<String, Any>): Map<String, Any> {
        // TODO: Интеграция с scm-iam
        return emptyMap()
    }
    
    @PutMapping("/{id}")
    fun updateUser(
        @PathVariable id: String,
        @RequestBody userData: Map<String, Any>
    ): Map<String, Any> {
        // TODO: Интеграция с scm-iam
        return emptyMap()
    }
    
    @DeleteMapping("/{id}")
    fun deleteUser(@PathVariable id: String): Map<String, String> {
        // TODO: Интеграция с scm-iam
        return mapOf("status" to "deleted")
    }
}

