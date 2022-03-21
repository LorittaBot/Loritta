import java.io.Serializable

data class ImageOptimizationSettings(
    val path: String
) : Serializable // Needs to be serializable because if not Gradle complains that it can't serialize