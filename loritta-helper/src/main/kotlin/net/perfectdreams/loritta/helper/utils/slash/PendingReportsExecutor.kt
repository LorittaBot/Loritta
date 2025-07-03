package net.perfectdreams.loritta.helper.utils.slash

import mu.KotlinLogging
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageType
import net.dv8tion.jda.api.entities.emoji.CustomEmoji
import net.perfectdreams.loritta.morenitta.interactions.commands.ApplicationCommandContext
import net.perfectdreams.loritta.morenitta.interactions.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.morenitta.interactions.commands.SlashCommandArguments
import net.perfectdreams.loritta.helper.LorittaHelper
import net.perfectdreams.loritta.helper.interactions.commands.vanilla.HelperExecutor
import net.perfectdreams.loritta.helper.utils.Emotes
import net.perfectdreams.loritta.helper.utils.extensions.await
import net.perfectdreams.loritta.helper.utils.generateserverreport.GenerateAppealsReport
import net.perfectdreams.loritta.helper.utils.generateserverreport.GenerateServerReport
import net.perfectdreams.sequins.text.StringUtils
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.OffsetTime
import java.time.ZoneId
import java.time.ZonedDateTime

class PendingReportsExecutor(helper: LorittaHelper) : HelperExecutor(helper, PermissionLevel.ADMIN) {
    private val logger = KotlinLogging.logger {}

    inner class Options : ApplicationCommandOptions() {
        val channel = optionalString("channel", "Em qual canal deverá ser filtrado os reports pendentes") {
            choice("Canal de Denúncias", "${helper.config.guilds.community.channels.serverReports}")
            choice("Canal de Apelos", "${helper.config.guilds.community.channels.appeals}")
        }
    }

    override val options = Options()

    override suspend fun executeHelper(context: ApplicationCommandContext, args: SlashCommandArguments) {
        val jda = context.user.jda
        context.deferChannelMessage(false)

        val channelId = args[options.channel] ?: "${helper.config.guilds.community.channels.serverReports}"
        val channel = jda.getTextChannelById(channelId) ?: return

        val today90DaysAgo = OffsetDateTime.now(ZoneId.of("America/Sao_Paulo"))
            .minusDays(90)

        try {
            val history = channel.history

            val messages = mutableListOf<Message>()

            while (true) {
                val newMessages = history.retrievePast(100).await()
                if (newMessages.isEmpty())
                    break

                val onlyMessagesInTheFilter = newMessages.filter {
                    it.timeCreated.isAfter(today90DaysAgo)
                }

                logger.info { "There are ${onlyMessagesInTheFilter.size} messages that were sent after $today90DaysAgo!" }

                if (onlyMessagesInTheFilter.isEmpty())
                    break

                messages += onlyMessagesInTheFilter
            }

            logger.info { "There are ${messages.size} messages to be sent!" }

            // Do a filter of the ones that aren't approved yet
            val notApprovedMessages = messages
                .filter { it.author.idLong == jda.selfUser.idLong }
                .filter { it.type == MessageType.DEFAULT }
                .filter { !it.isWebhookMessage }
                .filter { it.embeds.isNotEmpty() }
                .filter {
                    it.reactions.all { reaction ->
                        reaction.count == 1
                    }
                }

            if (notApprovedMessages.isEmpty()) {
                context.reply(false) {
                    content = "${Emotes.LORI_HEART} **|** Oba! Aparentemente não temos nenhuma denúncia/apelo pendente!"
                }
                return
            }

            val notApprovedMessagesByLevel = mutableMapOf<MessageTypeLevel, MutableList<Message>>()

            notApprovedMessages.forEach {
                val isPendingAnalysis = it.reactions.any { it.emoji is CustomEmoji }
                val messageType = if (isPendingAnalysis) MessageTypeLevel.PENDING else MessageTypeLevel.UNAPPROVED

                val list = notApprovedMessagesByLevel.getOrPut(messageType) {
                    mutableListOf()
                }

                list.add(it)

                notApprovedMessagesByLevel[messageType] = list
            }

            val lines = mutableListOf(
                "**Lista dos reports pendentes dos últimos 90 dias:**\n"
            )

            notApprovedMessagesByLevel.entries.sortedByDescending { it.key.level }.forEach { (t, u) ->
                lines.add("${t.emote} **${t.text}:**" + "\n")

                u.forEach {
                    lines.add(it.jumpUrl + "\n")
                }

                lines.add("\n\n")
            }

            val chunkedLines = StringUtils.chunkedLines(lines, 1900, forceSplit = true)

            // And now send them!
            for (line in chunkedLines) {
                context.reply(false) {
                    content = line
                }
            }
        } catch (e: Exception) {
            logger.warn(e) { "Something went wrong while trying to retrieve the reports!" }
        }
    }

    enum class MessageTypeLevel(val text: String, val emote: String, val level: Int) {
        UNAPPROVED(
            "Sem resposta",
            "<:catpolice:585608392110899200>",
            1000
        ),
        PENDING(
            "Em situação de análise",
            "<:lori_coffee:727631176432484473>",
            900
        )
    }
}