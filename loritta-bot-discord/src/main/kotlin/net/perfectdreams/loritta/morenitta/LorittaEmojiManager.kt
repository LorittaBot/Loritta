package net.perfectdreams.loritta.morenitta

import com.zaxxer.hikari.pool.HikariProxyConnection
import mu.KotlinLogging
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Icon
import net.dv8tion.jda.api.entities.emoji.ApplicationEmoji
import net.perfectdreams.loritta.cinnamon.pudding.tables.DiscordLorittaApplicationEmojis
import net.perfectdreams.loritta.common.emojis.LorittaEmojiReference
import net.perfectdreams.loritta.common.emojis.LorittaEmojis
import net.perfectdreams.loritta.common.emotes.DiscordEmote
import net.perfectdreams.loritta.common.emotes.Emote
import net.perfectdreams.loritta.common.emotes.UnicodeEmote
import net.perfectdreams.loritta.morenitta.utils.extensions.await
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.statements.api.ExposedConnection
import org.jetbrains.exposed.sql.upsert
import java.security.MessageDigest
import java.sql.Connection

/**
 * Converts a [LorittaEmojiReference] to a Loritta [Emote]
 */
class LorittaEmojiManager(private val loritta: LorittaBot) {
    companion object {
        private val logger = KotlinLogging.logger {}
        private val lockId = "loritta-cinnamon-application-emojis-updater".hashCode()
    }

    private val registeredApplicationEmojis = mutableMapOf<LorittaEmojiReference, ApplicationEmoji>()

    private fun sha256Hash(byteArray: ByteArray): ByteArray {
        val digest = MessageDigest.getInstance("SHA-256")
        return digest.digest(byteArray)
    }

    suspend fun syncEmojis(jda: JDA) {
        logger.info { "Attempting to sync emojis..." }
        loritta.transaction {
            logger.info { "Locking PostgreSQL advisory lock $lockId" }
            val rawConnection = (this.connection as ExposedConnection<HikariProxyConnection>).connection.unwrap(Connection::class.java)
            // First, we will hold a lock to avoid other instances trying to update the app commands at the same time
            val xactLockStatement = rawConnection.prepareStatement("SELECT pg_advisory_xact_lock(?);")
            xactLockStatement.setInt(1, lockId)
            xactLockStatement.execute()
            logger.info { "Successfully acquired PostgreSQL advisory lock $lockId!" }

            val discordApplicationEmojis = jda.retrieveApplicationEmojis().await()

            val remoteApplicationEmojis = DiscordLorittaApplicationEmojis
                .selectAll()
                .toList()

            val rawLocalApplicationEmojiNames = LorittaEmojis.applicationEmojis.map { it.name }

            for (discordEmoji in discordApplicationEmojis) {
                if (discordEmoji.name !in rawLocalApplicationEmojiNames) {
                    logger.info { "Deleting emoji $discordEmoji because it is not present on our application command list" }
                    discordEmoji.delete().await()

                    DiscordLorittaApplicationEmojis.deleteWhere {
                        DiscordLorittaApplicationEmojis.id eq discordEmoji.idLong
                    }
                }
            }

            for (localApplicationEmoji in LorittaEmojis.applicationEmojis) {
                val imageInputStream = LorittaEmojiManager::class.java.getResourceAsStream("/application_emojis/${localApplicationEmoji.name}.png") ?: LorittaEmojiManager::class.java.getResourceAsStream("/application_emojis/${localApplicationEmoji.name}.gif")
                if (imageInputStream == null)
                    error("Missing emoji ${localApplicationEmoji.name} image file!")

                val imageData = imageInputStream.readAllBytes()
                val hash = sha256Hash(imageData)

                val remoteApplicationEmoji = remoteApplicationEmojis.firstOrNull {
                    it[DiscordLorittaApplicationEmojis.emojiName] == localApplicationEmoji.name
                }
                val discordEmoji = discordApplicationEmojis.firstOrNull { it.name == localApplicationEmoji.name }

                // Upsert the new emoji!
                if (remoteApplicationEmoji == null || !remoteApplicationEmoji[DiscordLorittaApplicationEmojis.imageHash].contentEquals(hash)) {
                    if (discordEmoji != null) {
                        logger.info { "Deleting emoji $discordEmoji because it does not match our new image hash" }
                        discordEmoji.delete().await() // Delete the old emoji if it is not null
                    }

                    logger.info { "Uploading emoji ${localApplicationEmoji.name}..." }
                    val newEmoji = jda.createApplicationEmoji(localApplicationEmoji.name, Icon.from(imageData)).await()
                    logger.info { "Successfully uploaded emoji ${localApplicationEmoji.name}!" }

                    DiscordLorittaApplicationEmojis.upsert(DiscordLorittaApplicationEmojis.id) {
                        it[DiscordLorittaApplicationEmojis.id] = newEmoji.idLong
                        it[DiscordLorittaApplicationEmojis.emojiName] = localApplicationEmoji.name
                        it[DiscordLorittaApplicationEmojis.imageHash] = hash
                    }

                    registeredApplicationEmojis[localApplicationEmoji] = newEmoji
                } else {
                    logger.info { "Emoji $discordEmoji is up to date and does not require any updates :)" }
                    registeredApplicationEmojis[localApplicationEmoji] = discordEmoji!! // This SHOULD EXIST
                }
            }
        }

        logger.info { "Successfully synced all emojis! Application Emojis count: ${registeredApplicationEmojis.size}" }
    }

    fun get(ref: LorittaEmojiReference): Emote {
        return when (ref) {
            is LorittaEmojiReference.ApplicationEmoji -> {
                val appEmoji = registeredApplicationEmojis[ref] ?: error("Missing ${ref.name} from the uploaded application emoji list!")
                 DiscordEmote(
                    appEmoji.idLong,
                    appEmoji.name,
                    appEmoji.isAnimated
                )
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