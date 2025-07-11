package net.perfectdreams.loritta.morenitta

import kotlinx.coroutines.delay
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Icon
import net.perfectdreams.harmony.logging.HarmonyLoggerFactory
import net.perfectdreams.loritta.cinnamon.pudding.tables.DiscordLorittaApplicationEmojis
import net.perfectdreams.loritta.common.emojis.LorittaEmojiReference
import net.perfectdreams.loritta.common.emojis.LorittaEmojis
import net.perfectdreams.loritta.common.emotes.DiscordEmote
import net.perfectdreams.loritta.common.emotes.Emote
import net.perfectdreams.loritta.common.emotes.UnicodeEmote
import net.perfectdreams.loritta.morenitta.utils.extensions.await
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.notInList
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.statements.jdbc.JdbcConnectionImpl
import org.jetbrains.exposed.sql.upsert
import java.security.MessageDigest

/**
 * Converts a [LorittaEmojiReference] to a Loritta [Emote]
 */
class LorittaEmojiManager(private val loritta: LorittaBot) {
    companion object {
        private val logger by HarmonyLoggerFactory.logger {}
        private val lockId = "loritta-cinnamon-application-emojis-updater".hashCode()
    }

    private val registeredApplicationEmojis = mutableMapOf<LorittaEmojiReference, DiscordEmote>()
    private var updatedEmojiList = false

    private fun sha256Hash(byteArray: ByteArray): ByteArray {
        val digest = MessageDigest.getInstance("SHA-256")
        return digest.digest(byteArray)
    }

    suspend fun syncEmojis(jda: JDA) {
        while (true) {
            logger.info { "Attempting to sync emojis..." }

            val processed = loritta.transaction {
                logger.info { "Requesting PostgreSQL advisory lock ${lockId}..." }
                val lockStatement = (this.connection as JdbcConnectionImpl).connection.prepareStatement("SELECT pg_try_advisory_xact_lock(?);")
                lockStatement.setInt(1, lockId)
                var lockAcquired = false
                lockStatement.executeQuery().use { rs ->
                    if (rs.next()) {
                        lockAcquired = rs.getBoolean(1)
                    }
                }
                if (!lockAcquired)
                    return@transaction false

                logger.info { "Successfully requested advisory lock ${lockId}!" }

                // To avoid querying Discord on each startup, we'll pull the emojis from the database directly and ONLY query Discord when needed
                val remoteApplicationEmojis = DiscordLorittaApplicationEmojis
                    .selectAll()
                    .toList()

                var allEmojisAreUpToDate = true
                if (remoteApplicationEmojis.size != LorittaEmojis.applicationEmojis.size) {
                    logger.warn { "Remote emoji count (${remoteApplicationEmojis.size}) does not match local emoji count (${LorittaEmojis.applicationEmojis.size})! Emojis should be resynced!" }
                    allEmojisAreUpToDate = false
                }

                if (allEmojisAreUpToDate) {
                    for (remoteEmoji in remoteApplicationEmojis) {
                        val localEmoji =
                            LorittaEmojis.applicationEmojis.firstOrNull { it.name == remoteEmoji[DiscordLorittaApplicationEmojis.emojiName] }
                        if (localEmoji == null) {
                            logger.warn { "Remote emoji ${remoteEmoji[DiscordLorittaApplicationEmojis.emojiName]} does not exist on our local emojis! Emojis should be resynced!" }
                            allEmojisAreUpToDate = false
                            break
                        }

                        var imageInputStream =
                            LorittaEmojiManager::class.java.getResourceAsStream("${localEmoji.imagePath}${remoteEmoji[DiscordLorittaApplicationEmojis.emojiName]}.png")
                        if (imageInputStream == null) {
                            imageInputStream =
                                LorittaEmojiManager::class.java.getResourceAsStream("${localEmoji.imagePath}${remoteEmoji[DiscordLorittaApplicationEmojis.emojiName]}.gif")

                            if (imageInputStream == null) {
                                logger.warn { "Database emoji ${remoteEmoji[DiscordLorittaApplicationEmojis.emojiName]} is not present on the ${localEmoji.imagePath} folder! Emojis should be resynced!" }
                                allEmojisAreUpToDate = false
                                break
                            }
                        }

                        val imageData = imageInputStream.readAllBytes()
                        val hash = sha256Hash(imageData)

                        if (!remoteEmoji[DiscordLorittaApplicationEmojis.imageHash].contentEquals(hash)) {
                            logger.warn { "Database emoji ${remoteEmoji[DiscordLorittaApplicationEmojis.emojiName]} does not match our local ${localEmoji.imagePath} copy! Emojis should be resynced!" }
                            allEmojisAreUpToDate = false
                            break
                        }

                        registeredApplicationEmojis[localEmoji] = DiscordEmote(
                            remoteEmoji[DiscordLorittaApplicationEmojis.emojiId],
                            remoteEmoji[DiscordLorittaApplicationEmojis.emojiName],
                            remoteEmoji[DiscordLorittaApplicationEmojis.animated],
                        )
                    }
                }

                if (allEmojisAreUpToDate) {
                    logger.info { "All emojis are up to date and we don't need to query Discord to sync the emojis! :) Application Emojis count: ${registeredApplicationEmojis.size}" }
                    return@transaction true
                } else {
                    logger.info { "Emojis are not up to date and require a resync!" }
                    registeredApplicationEmojis.clear()

                    val discordApplicationEmojis = jda.retrieveApplicationEmojis().await()

                    val rawLocalApplicationEmojiNames = LorittaEmojis.applicationEmojis.map { it.name }

                    val deletedCount = DiscordLorittaApplicationEmojis.deleteWhere {
                        DiscordLorittaApplicationEmojis.emojiName notInList rawLocalApplicationEmojiNames
                    }

                    logger.info { "Deleted $deletedCount emojis that were stored in our database, but that aren't used anymore" }

                    for (discordEmoji in discordApplicationEmojis) {
                        if (discordEmoji.name !in rawLocalApplicationEmojiNames) {
                            logger.info { "Deleting emoji $discordEmoji because it is not present on our application command list" }
                            discordEmoji.delete().await()

                            DiscordLorittaApplicationEmojis.deleteWhere {
                                DiscordLorittaApplicationEmojis.emojiId eq discordEmoji.idLong
                            }
                        }
                    }

                    for (localApplicationEmoji in LorittaEmojis.applicationEmojis) {
                        val imageInputStream =
                            LorittaEmojiManager::class.java.getResourceAsStream("${localApplicationEmoji.imagePath}.png")
                                ?: LorittaEmojiManager::class.java.getResourceAsStream("${localApplicationEmoji.imagePath}.gif")

                        if (imageInputStream == null)
                            error("Missing emoji ${localApplicationEmoji.name} image file! The file should've been at ${localApplicationEmoji.imagePath}")

                        val imageData = imageInputStream.readAllBytes()
                        val hash = sha256Hash(imageData)

                        val remoteApplicationEmoji = remoteApplicationEmojis.firstOrNull {
                            it[DiscordLorittaApplicationEmojis.emojiName] == localApplicationEmoji.name
                        }
                        val discordEmoji =
                            discordApplicationEmojis.firstOrNull { it.name == localApplicationEmoji.name }

                        // Upsert the new emoji!
                        if (remoteApplicationEmoji == null || discordEmoji == null || !remoteApplicationEmoji[DiscordLorittaApplicationEmojis.imageHash].contentEquals(
                                hash
                            )
                        ) {
                            if (discordEmoji != null) {
                                logger.info { "Deleting emoji $discordEmoji because it does not match our new image hash" }
                                discordEmoji.delete().await() // Delete the old emoji if it is not null
                            }

                            logger.info { "Uploading emoji ${localApplicationEmoji.name}..." }
                            val newEmoji =
                                jda.createApplicationEmoji(localApplicationEmoji.name, Icon.from(imageData)).await()
                            logger.info { "Successfully uploaded emoji ${localApplicationEmoji.name}!" }

                            DiscordLorittaApplicationEmojis.upsert(DiscordLorittaApplicationEmojis.emojiName) {
                                it[DiscordLorittaApplicationEmojis.emojiName] = localApplicationEmoji.name
                                it[DiscordLorittaApplicationEmojis.emojiId] = newEmoji.idLong
                                it[DiscordLorittaApplicationEmojis.animated] = newEmoji.isAnimated
                                it[DiscordLorittaApplicationEmojis.imageHash] = hash
                            }

                            registeredApplicationEmojis[localApplicationEmoji] = DiscordEmote(
                                newEmoji.idLong,
                                newEmoji.name,
                                newEmoji.isAnimated
                            )
                        } else {
                            logger.info { "Emoji $discordEmoji is up to date and does not require any updates" }

                            registeredApplicationEmojis[localApplicationEmoji] = DiscordEmote(
                                discordEmoji.idLong,
                                discordEmoji.name,
                                discordEmoji.isAnimated
                            )
                        }
                    }
                }

                logger.info { "Successfully synced all emojis! :) Application Emojis count: ${registeredApplicationEmojis.size}" }

                return@transaction true
            }

            if (processed) {
                this.updatedEmojiList = true
                break
            }

            logger.info { "Could not acquire lock! Retrying in 100ms..." }
            delay(100)
        }
    }

    fun get(ref: LorittaEmojiReference): Emote {
        return when (ref) {
            is LorittaEmojiReference.ApplicationEmoji -> {
                return registeredApplicationEmojis[ref] ?: error("Missing ${ref.name} from the uploaded application emoji list! Is the emoji list updated? ${this.updatedEmojiList}")
            }
            is LorittaEmojiReference.GuildEmoji -> DiscordEmote(
                ref.id,
                ref.name,
                ref.animated
            )
            is LorittaEmojiReference.UnicodeEmoji -> UnicodeEmote(ref.name)
        }
    }
}