import com.typesafe.config.ConfigFactory
import kotlinx.serialization.hocon.Hocon
import kotlinx.serialization.hocon.decodeFromConfig
import net.perfectdreams.loritta.cinnamon.platform.webserver.LorittaCinnamonWebServer
import net.perfectdreams.loritta.cinnamon.platform.webserver.utils.config.RootConfig
import org.junit.jupiter.api.Test

class CheckConfigTest {
    private fun loadFromJar(inputPath: String): String {
        val inputStream = LorittaCinnamonWebServer::class.java.getResourceAsStream(inputPath)
        return inputStream.bufferedReader(Charsets.UTF_8).readText()
    }

    @Test
    fun `check config`() {
        val stream = loadFromJar("/loritta.conf")
        Hocon.decodeFromConfig<RootConfig>(ConfigFactory.parseReader(stream.reader()))
    }
}