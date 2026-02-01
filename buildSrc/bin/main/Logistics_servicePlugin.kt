/**
 * Precompiled [logistics.service.gradle.kts][Logistics_service_gradle] script plugin.
 *
 * @see Logistics_service_gradle
 */
public
class Logistics_servicePlugin : org.gradle.api.Plugin<org.gradle.api.Project> {
    override fun apply(target: org.gradle.api.Project) {
        try {
            Class
                .forName("Logistics_service_gradle")
                .getDeclaredConstructor(org.gradle.api.Project::class.java, org.gradle.api.Project::class.java)
                .newInstance(target, target)
        } catch (e: java.lang.reflect.InvocationTargetException) {
            throw e.targetException
        }
    }
}
