package net.perfectdreams.loritta.platform.twitter

import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import io.ktor.client.*
import io.ktor.client.engine.apache.*
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import net.perfectdreams.loritta.api.LorittaBot
import net.perfectdreams.loritta.api.commands.SilentCommandException
import net.perfectdreams.loritta.api.platform.PlatformFeature
import net.perfectdreams.loritta.platform.twitter.commands.TwitterCommandContext
import net.perfectdreams.loritta.platform.twitter.commands.TwitterCommandMap
import net.perfectdreams.loritta.platform.twitter.entities.TwitterMessage
import net.perfectdreams.loritta.platform.twitter.plugin.JVMPluginManager
import net.perfectdreams.loritta.platform.twitter.utils.JVMLorittaAssets
import net.perfectdreams.loritta.platform.twitter.utils.config.TempConfig
import twitter4j.*
import twitter4j.conf.Configuration
import twitter4j.conf.ConfigurationBuilder
import kotlin.concurrent.thread
import kotlin.random.Random

/**
 * Loritta implementation for Twitter (Twitter4J)
 */
class LorittaTwitter(val config: TempConfig) : LorittaBot() {
    companion object {
        private val logger = KotlinLogging.logger {}
        lateinit var TWITTER: Twitter
    }

    override val supportedFeatures = PlatformFeature.values().toList()
    override val commandMap = TwitterCommandMap()
    override val pluginManager = JVMPluginManager(this)
    override val assets = JVMLorittaAssets(this)
    override val http = HttpClient(Apache) {
        this.expectSuccess = false

        engine {
            this.socketTimeout = 25_000
            this.connectTimeout = 25_000
            this.connectionRequestTimeout = 25_000

            customizeClient {
                // Maximum number of socket connections.
                this.setMaxConnTotal(100)

                // Maximum number of requests for a specific endpoint route.
                this.setMaxConnPerRoute(100)
            }
        }
    }
    override val random = Random(System.currentTimeMillis())
    val twitter by lazy {
        val tf = TwitterFactory(buildTwitterConfig())
        tf.instance
    }

    fun buildTwitterConfig(): Configuration {
        val cb = ConfigurationBuilder()
        cb.setDebugEnabled(true)
                .setOAuthConsumerKey(config.consumerKey)
                .setOAuthConsumerSecret(config.consumerSecret)
                .setOAuthAccessToken(config.accessToken)
                .setOAuthAccessTokenSecret(config.accessTokenSecret)

        return cb.build()
    }

    fun start() {
        pluginManager.loadPlugins()

        TWITTER = twitter

        val loritta = this

        val baseLocale = BaseLocale("default")

        thread {
            val twitterStream = TwitterStreamFactory(buildTwitterConfig()).instance

            twitterStream.addListener(object : StatusListener {
                override fun onTrackLimitationNotice(p0: Int) {
                    logger.info(p0.toString())
                }

                override fun onStallWarning(p0: StallWarning) {
                    logger.info(p0.toString())
                }

                override fun onException(p0: Exception) {
                    p0.printStackTrace()
                }

                override fun onDeletionNotice(p0: StatusDeletionNotice) {}

                override fun onStatus(p0: Status) {
                    val text = p0.text

                    if (p0.isRetweet)
                        return

                    println(text)

                    if (text.contains("@LorittaEdit")) {
                        // Example: @MrPowerGamerBR @LorittaBot petpet
                        // Drop all initial text starting with "@"
                        val endResult = text.split(" ").dropWhile { it.startsWith("@") }
                        val commandAndArguments = endResult.joinToString(" ")

                        try {
                            val split = commandAndArguments.split(" ")
                            val commandLabel = split.first()

                            val canelladvd = commandMap
                                    .commands.first { it.labels.first() == commandLabel }

                            runBlocking {
                                canelladvd.executor.invoke(
                                        TwitterCommandContext(
                                                loritta,
                                                canelladvd,
                                                split.drop(1),
                                                TwitterMessage(p0),
                                                baseLocale
                                        )
                                )
                            }
                        } catch (e: SilentCommandException) {
                            e.printStackTrace()
                        }
                    }
                }

                override fun onScrubGeo(p0: Long, p1: Long) {}
            })

            val tweetFilterQuery = FilterQuery() // See
            tweetFilterQuery.track("LorittaEdit") // OR on keywords

            logger.info { "Starting Twitter Tweet Tracker... Using query $tweetFilterQuery" }

            twitterStream.filter(tweetFilterQuery)
        }
    }
}