package net.perfectdreams.loritta.morenitta.commands.vanilla.administration

import kotlinx.coroutines.runBlocking
import net.perfectdreams.loritta.morenitta.commands.AbstractCommand
import net.perfectdreams.loritta.morenitta.commands.CommandContext
import net.perfectdreams.loritta.morenitta.utils.MessageUtils
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.perfectdreams.loritta.morenitta.utils.onReactionAddByAuthor
import net.perfectdreams.loritta.morenitta.utils.stripCodeMarks
import dev.kord.common.entity.Permission
import net.perfectdreams.loritta.deviousfun.entities.Member
import net.perfectdreams.loritta.deviousfun.entities.Message
import net.perfectdreams.loritta.deviousfun.entities.User
import net.perfectdreams.loritta.morenitta.messages.LorittaReply
import net.perfectdreams.loritta.common.utils.Emotes
import net.perfectdreams.loritta.morenitta.utils.PunishmentAction
import net.perfectdreams.loritta.morenitta.LorittaBot

class KickCommand(loritta: LorittaBot) : AbstractCommand(
    loritta,
    "kick",
    listOf("expulsar", "kickar"),
    net.perfectdreams.loritta.common.commands.CommandCategory.MODERATION
) {
    override fun getDescriptionKey() = LocaleKeyData("commands.command.kick.description")
    override fun getExamplesKey() = AdminUtils.PUNISHMENT_EXAMPLES_KEY
    override fun getUsage() = AdminUtils.PUNISHMENT_USAGES

    override fun getDiscordPermissions(): List<Permission> {
        return listOf(Permission.KickMembers)
    }

    override fun canUseInPrivateChannel(): Boolean {
        return false
    }

    override fun getBotPermissions(): List<Permission> {
        return listOf(Permission.KickMembers)
    }

    override suspend fun run(context: CommandContext, locale: BaseLocale) {
        if (context.args.isNotEmpty()) {
            val (users, rawReason) = AdminUtils.checkAndRetrieveAllValidUsersFromMessages(context) ?: return

            val members = mutableListOf<Member>()
            for (user in users) {
                val member = context.guild.retrieveMemberOrNull(user)

                if (member == null) {
                    context.reply(
                        LorittaReply(
                            context.locale["commands.userNotOnTheGuild", "${user.asMention} (`${user.name.stripCodeMarks()}#${user.discriminator} (${user.idLong})`)"],
                            Emotes.LORI_HM
                        )
                    )
                    return
                }

                if (!AdminUtils.checkForPermissions(context, member))
                    return

                members.add(member)
            }

            val settings = AdminUtils.retrieveModerationInfo(loritta, context.config)
            val (reason, skipConfirmation, silent, delDays) = AdminUtils.getOptions(context, rawReason) ?: return

            val kickCallback: suspend (Message?, Boolean) -> (Unit) = { message, isSilent ->
                for (member in members)
                    kick(context, settings, locale, member, member.user, reason, isSilent)

                runCatching { message?.delete() }

                AdminUtils.sendSuccessfullyPunishedMessage(context, reason, true)
            }

            if (skipConfirmation) {
                kickCallback.invoke(null, silent)
                return
            }

            val hasSilent = settings.sendPunishmentViaDm || settings.sendPunishmentToPunishLog
            val message = AdminUtils.sendConfirmationMessage(context, users, hasSilent, "kick")

            message.onReactionAddByAuthor(context) {
                if (it.reactionEmote.isEmote("✅") || it.reactionEmote.isEmote("\uD83D\uDE4A")) {
                    kickCallback.invoke(message, it.reactionEmote.isEmote("\uD83D\uDE4A"))
                }
                return@onReactionAddByAuthor
            }

            runCatching { message.addReaction("✅") }
            if (hasSilent) {
                runCatching { message.addReaction("\uD83D\uDE4A") }
            }
        } else {
            this.explain(context)
        }
    }

    companion object {
        private val LOCALE_PREFIX = "commands.command"

        suspend fun kick(
            context: CommandContext,
            settings: AdminUtils.ModerationConfigSettings,
            locale: BaseLocale,
            member: Member,
            user: User,
            reason: String,
            isSilent: Boolean
        ) {
            if (!isSilent) {
                if (settings.sendPunishmentViaDm && context.guild.isMember(user)) {
                    try {
                        val embed = AdminUtils.createPunishmentMessageSentViaDirectMessage(
                            context.guild,
                            locale,
                            context.userHandle,
                            locale["commands.command.kick.punishAction"],
                            reason
                        )

                        runCatching {
                            user.openPrivateChannel().sendMessage(embed)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                val punishLogMessage = runBlocking {
                    AdminUtils.getPunishmentForMessage(
                        context.loritta,
                        settings,
                        context.guild,
                        PunishmentAction.KICK
                    )
                }

                if (settings.sendPunishmentToPunishLog && settings.punishLogChannelId != null && punishLogMessage != null) {
                    val textChannel = context.guild.getTextChannelById(settings.punishLogChannelId)

                    if (textChannel != null && textChannel.canTalk()) {
                        val message = MessageUtils.generateMessage(
                            punishLogMessage,
                            listOf(user, context.guild),
                            context.guild,
                            mutableMapOf(
                                "duration" to locale["commands.command.mute.forever"]
                            ) + AdminUtils.getStaffCustomTokens(context.userHandle)
                                    + AdminUtils.getPunishmentCustomTokens(locale, reason, "${LOCALE_PREFIX}.kick")
                        )

                        message?.let {
                            runCatching { textChannel.sendMessage(it) }
                        }
                    }
                }
            }

            context.guild.kick(member, AdminUtils.generateAuditLogMessage(locale, context.userHandle, reason))
            runCatching { }
        }
    }
}