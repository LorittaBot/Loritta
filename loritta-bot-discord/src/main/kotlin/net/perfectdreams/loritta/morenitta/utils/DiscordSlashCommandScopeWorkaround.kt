package net.perfectdreams.loritta.morenitta.utils

import com.github.benmanes.caffeine.cache.Caffeine
import dev.minn.jda.ktx.messages.InlineMessage
import net.perfectdreams.harmony.logging.HarmonyLoggerFactory
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.exceptions.ErrorResponseException
import net.dv8tion.jda.api.requests.ErrorResponse
import net.dv8tion.jda.api.utils.messages.MessageCreateData
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.pudding.tables.SlashCommandsScopeAuthorizations
import net.perfectdreams.loritta.common.utils.text.TextUtils.stripNewLines
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.utils.extensions.await
import org.jetbrains.exposed.sql.insert
import java.time.Instant
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Workaround for when Loritta got suspended from Discord (due to their own mistake!) and guilds got the slash command scope revoked
 *
 * To work around this, we will ask users to manually reauthorize the scope, if we aren't able to do slash command related things on the guild
 */
class DiscordSlashCommandScopeWorkaround(private val loritta: LorittaBot) {
    companion object {
        private val logger by HarmonyLoggerFactory.logger {}
    }

    private val guildSlashCommandScopeEnabled = Caffeine.newBuilder()
        .expireAfterWrite(15, TimeUnit.MINUTES)
        .build<Long, Boolean>()
        .asMap()

    suspend fun checkIfSlashCommandScopeIsEnabled(guild: Guild, member: Member): Boolean {
        return this.guildSlashCommandScopeEnabled.getOrPut(guild.idLong) {
            val r = try {
                guild.retrieveCommands().await()
                true
            } catch (e: Exception) {
                logger.warn(e) { "Failed to retrieve guild ${guild.name} (${guild.idLong}) commands for slash command scope check" }
                if (e is ErrorResponseException) {
                    if (e.errorResponse == ErrorResponse.MISSING_ACCESS) {
                        // Scope not authorized, please reauthorize
                        false
                    } else null
                } else null
            }

            if (r == null) {
                logger.warn { "Unknown slash command scope state for ${guild.name} (${guild.idLong}), falling back to authorized..." }
                return@getOrPut true
            }

            loritta.transaction {
                SlashCommandsScopeAuthorizations.insert {
                    it[SlashCommandsScopeAuthorizations.guild] = guild.idLong
                    it[SlashCommandsScopeAuthorizations.authorized] = r
                    it[SlashCommandsScopeAuthorizations.triggeredBy] = member.idLong
                    it[SlashCommandsScopeAuthorizations.checkedAt] = Instant.now()
                }
            }

            r
        }.also {
            if (it) {
                logger.info { "Guild ${guild.name} (${guild.idLong}) has the slash commands scope authorized" }
            } else {
                logger.warn { "Guild ${guild.name} (${guild.idLong}) does NOT have the slash commands scope authorized" }
            }
        }
    }

    fun unauthMessage(guild: Guild, member: Member): InlineMessage<MessageCreateData>.() -> (Unit) = {
        allowedMentionTypes = EnumSet.of(
            Message.MentionType.CHANNEL,
            Message.MentionType.EMOJI,
            Message.MentionType.SLASH_COMMAND
        )

        styled(
            "**IMPORTANTE:** Em <t:1710803580:D> o Discord tinha suspendido, por erro deles, a Loritta. E, devido a um bug do Discord, quando ela teve a suspensão removida, alguns servidores ficaram sem slash commands (comandos por `/`).",
            Emotes.LoriSob
        )

        if (member.hasPermission(Permission.MANAGE_SERVER)) {
            styled(
                "O servidor que você está agora, o `${guild.name.stripCodeMarks().stripNewLines().escapeMentions().ifEmpty { " " }}`, foi afetado por este bug. Para arrumar este problema, reautorize a Loritta pelo link a seguir ${LorittaDiscordOAuth2AddBotURL(loritta)}"
            )
        } else {
            styled(
                "O servidor que você está agora, o `${guild.name.stripCodeMarks().stripNewLines().escapeMentions().ifEmpty { " " }}`, foi afetado por este bug. Para arrumar este problema, peça para um administrador do servidor para reautorizar a Loritta pelo link a seguir ${LorittaDiscordOAuth2AddBotURL(loritta)}"
            )
        }

        styled(
            "**NÃO PRECISA EXPULSAR A LORITTA DO SERVIDOR, só coloque para adicionar a Loritta novamente no servidor sem expulsar ela!**"
        )

        styled(
            "Após reautorizar, os slash commands da Loritta estarão disponíveis novamente no servidor. Se precisar de mais ajuda, visite o servidor de suporte da Loritta! <https://discord.gg/lori>"
        )

        styled(
            "Desculpe pela inconveniência, mas é que isso é um bug do próprio Discord que eles ainda não arrumaram, mesmo que este problema tenha sido causado por eles...",
            Emotes.LoriSob
        )
    }
}