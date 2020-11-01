package com.mrpowergamerbr.loritta.utils.extensions

import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.commands.vanilla.administration.AdminUtils
import com.mrpowergamerbr.loritta.utils.isValidSnowflake
import com.mrpowergamerbr.loritta.utils.onReactionAddByAuthor
import com.mrpowergamerbr.loritta.utils.stripCodeMarks
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.entities.User
import net.perfectdreams.loritta.api.messages.LorittaReply
import net.perfectdreams.loritta.utils.Emotes

fun CommandContext.getTextChannel(input: String?, executedIfNull: Boolean = false): TextChannel? {
    if (input == null)
        return event.textChannel

    val channels = guild.getTextChannelsByName(input, false)
    if (channels.isNotEmpty()) {
        return channels[0]
    }

    val id = input
            .replace("<", "")
            .replace("#", "")
            .replace(">", "")

    if (!id.isValidSnowflake())
        return null

    val channel = guild.getTextChannelById(id)

    return if (channel == null && event.textChannel != null && executedIfNull)
        event.textChannel
    else
        channel
}

suspend fun CommandContext.getValidMembersForPunishment(users: List<User>): List<Member> = users.map {
    val member = guild.retrieveMemberOrNull(it)

    if (member == null) {
        reply(
                LorittaReply(
                        locale["commands.userNotOnTheGuild", "${it.asMention} (`${it.name.stripCodeMarks()}#${it.discriminator} (${it.idLong})`)"],
                        Emotes.LORI_HM
                )
        )
        return@map null
    }

    if (!AdminUtils.checkForPermissions(this, member))
        return@map null

    return@map member
}.filterNotNull()

suspend fun CommandContext.handlePunishmentConfirmation(message: Message, callback: suspend (Message?, Boolean) -> (Unit)) {
    message.onReactionAddByAuthor(this) {
        if (it.reactionEmote.isEmote("✅") || it.reactionEmote.isEmote("\uD83D\uDE4A")) {
            callback.invoke(message, it.reactionEmote.isEmote("\uD83D\uDE4A"))
        }
        return@onReactionAddByAuthor
    }

    val settings = AdminUtils.retrieveModerationInfo(config)
    val hasSilent = settings.sendPunishmentViaDm || settings.sendPunishmentToPunishLog

    message.addReaction("✅").queue()
    if (hasSilent) {
        message.addReaction("\uD83D\uDE4A").queue()
    }
}
