package net.perfectdreams.loritta.cinnamon.dashboard.backend

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import net.perfectdreams.loritta.cinnamon.dashboard.backend.utils.config.RootConfig
import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import net.perfectdreams.loritta.common.locale.LorittaLanguageManager
import net.perfectdreams.loritta.common.utils.config.ConfigUtils
import net.perfectdreams.loritta.common.utils.extensions.getPathFromResources
import net.perfectdreams.loritta.serializable.internal.requests.LorittaInternalRPCRequest
import net.perfectdreams.loritta.serializable.internal.responses.LorittaInternalRPCResponse
import java.util.*
import kotlin.io.path.readText
import kotlin.time.Duration.Companion.seconds

object LorittaDashboardBackendLauncher {
    private val logger = KotlinLogging.logger {}

    @JvmStatic
    fun main(args: Array<String>) {
        // https://github.com/JetBrains/Exposed/issues/1356
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))

        val rootConfig = ConfigUtils.loadAndParseConfigOrCopyFromJarAndExit<RootConfig>(
            LorittaDashboardBackend::class,
            System.getProperty("spicymorenitta.config", "spicy-morenitta.conf")
        )
        logger.info { "Loaded SpicyMorenitta's configuration file" }

        val http = HttpClient {}

        val response = runBlocking { getLorittaReplicasInfo(rootConfig, http) }
        logger.info { "Loritta replica information: Environment Type: ${response.environmentType}; Max Shards: ${response.maxShards}; ${response.instances.size} replicas" }

        val languageManager = LorittaLanguageManager(LorittaDashboardBackend::class)

        val services = Pudding.createPostgreSQLPudding(
            rootConfig.pudding.address,
            rootConfig.pudding.database,
            rootConfig.pudding.username,
            rootConfig.pudding.password
        )
        services.setupShutdownHook()

        logger.info { "Started Pudding client!" }

        // Loads the appropriate bundle depending if we are overriding the JS file or not
        val spicyMorenittaJsBundle = if (rootConfig.spicyMorenittaJsPath != null) {
            SpicyMorenittaDevelopmentBundle(rootConfig.spicyMorenittaJsPath)
        } else {
            SpicyMorenittaProductionBundle(
                SpicyMorenittaBundle.createSpicyMorenittaJsBundleContent(
                    LorittaDashboardBackend::class.getPathFromResources("/spicy_frontend/js/spicy-frontend.js")!!.readText()
                )
            )
        }

        val m = LorittaDashboardBackend(
            rootConfig,
            languageManager,
            services,
            response,
            http,
            spicyMorenittaJsBundle
        )
        m.start()
    }

    suspend fun getLorittaReplicasInfo(rootConfig: RootConfig, http: HttpClient): LorittaInternalRPCResponse.GetLorittaInfoResponse.Success {
        while (true) {
            try {
                logger.info { "Attempting to get Loritta's replicas info from Loritta's main replica..." }
                // Attempt to get Loritta's replicas info from the main replica

                return Json.decodeFromString<LorittaInternalRPCResponse>(
                    http.post("${rootConfig.lorittaMainRpcUrl.removeSuffix("/")}/rpc") {
                        setBody(Json.encodeToString<LorittaInternalRPCRequest>(LorittaInternalRPCRequest.GetLorittaInfoRequest()))
                    }.bodyAsText()
                ) as LorittaInternalRPCResponse.GetLorittaInfoResponse.Success
            } catch (e: Exception) {
                logger.warn(e) { "Failed to get Loritta's replicas info from Loritta's main replica! Trying again in 2s..." }
                delay(2.seconds)
            }
        }
    }
}