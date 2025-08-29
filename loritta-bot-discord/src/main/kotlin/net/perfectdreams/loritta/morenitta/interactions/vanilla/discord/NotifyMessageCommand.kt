package net.perfectdreams.loritta.morenitta.interactions.vanilla.discord

import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import net.dv8tion.jda.api.interactions.IntegrationType
import net.dv8tion.jda.api.interactions.InteractionContextType
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.pudding.tables.NotifyMessagesRequests
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.utils.TodoFixThisData
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.commands.LorittaSlashCommandExecutor
import net.perfectdreams.loritta.morenitta.interactions.commands.SlashCommandArguments
import net.perfectdreams.loritta.morenitta.interactions.commands.SlashCommandDeclarationWrapper
import net.perfectdreams.loritta.morenitta.interactions.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.morenitta.interactions.commands.slashCommand
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import java.time.Instant
import java.util.*

class NotifyMessageCommand(val m: LorittaBot) : SlashCommandDeclarationWrapper {
    companion object {
        val I18N_PREFIX = I18nKeysData.Commands.Command.Notifymessage
    }

    override fun command() = slashCommand(I18N_PREFIX.Label, I18N_PREFIX.Description, CommandCategory.DISCORD, UUID.fromString("0d240952-891b-48b8-a25e-dc9519ca41e4")) {
        this.interactionContexts = listOf(InteractionContextType.GUILD)
        this.integrationTypes = listOf(IntegrationType.GUILD_INSTALL)

        executor = NotifyMessageExecutor()
    }

    inner class NotifyMessageExecutor : LorittaSlashCommandExecutor() {
        inner class Options : ApplicationCommandOptions() {
            val channel = optionalChannel("channel", I18N_PREFIX.Options.Channel.Text)
            val user = optionalUser("user", I18N_PREFIX.Options.User.Text)
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            val channel = args[options.channel]
            val user = args[options.user]

            val trackedChannel = channel ?: context.channel

            if (trackedChannel !is GuildMessageChannel) {
                context.reply(true) {
                    styled(
                        context.i18nContext.get(I18N_PREFIX.YouNeedToSelectATextChannel),
                        Emotes.Error
                    )
                }
                return
            }

            if (!context.member.hasPermission(trackedChannel, Permission.VIEW_CHANNEL, Permission.MESSAGE_HISTORY)) {
                context.reply(true) {
                    styled(
                        context.i18nContext.get(I18N_PREFIX.YouDontHavePermissionToReadThatChannel),
                        Emotes.Error
                    )
                }
                return
            }

            context.deferChannelMessage(true)

            val created = m.transaction {
                val alreadyHasRequest = NotifyMessagesRequests.selectAll()
                    .where {
                        NotifyMessagesRequests.userId eq context.user.idLong and (NotifyMessagesRequests.channelId eq context.channel.idLong) and (NotifyMessagesRequests.notifyUserId eq user?.user?.idLong) and (NotifyMessagesRequests.processedAt.isNull())
                    }
                    .count() != 0L

                if (alreadyHasRequest)
                    return@transaction false

                NotifyMessagesRequests.insert {
                    it[NotifyMessagesRequests.userId] = context.user.idLong
                    it[NotifyMessagesRequests.channelId] = trackedChannel.idLong
                    it[NotifyMessagesRequests.notifyUserId] = user?.user?.idLong
                    it[NotifyMessagesRequests.requestedAt] = Instant.now()
                    it[NotifyMessagesRequests.processedAt] = null
                }

                return@transaction true
            }

            if (created) {
                context.reply(true) {
                    styled(
                        if (user != null)
                            context.i18nContext.get(I18N_PREFIX.NotificationCreatedUser(user.user.asMention, trackedChannel.asMention))
                        else
                            context.i18nContext.get(I18N_PREFIX.NotificationCreatedAnyone(trackedChannel.asMention)),
                        Emotes.LoriHappy
                    )
                }
            } else {
                context.reply(true) {
                    styled(
                        context.i18nContext.get(I18N_PREFIX.YouAlreadyHaveANotificationCreated),
                        Emotes.Error
                    )
                }
            }
        }
    }
}