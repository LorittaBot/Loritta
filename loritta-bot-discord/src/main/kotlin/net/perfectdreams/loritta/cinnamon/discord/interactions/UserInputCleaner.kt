package net.perfectdreams.loritta.cinnamon.discord.interactions

import dev.kord.common.entity.Snowflake
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.GuildApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.discord.utils.DiscordInviteUtils
import net.perfectdreams.loritta.common.utils.LorittaPermission
import net.perfectdreams.loritta.common.utils.text.TextUtils.stripCodeBackticks
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext

/**
 * Clean up and escape user input, useful when displaying user input
 */
suspend fun cleanUpForOutput(
    context: InteractionContext,
    input: String,
    escapeMentions: Boolean = true,
    stripCodeBackticks: Boolean = true,
    stripInvites: Boolean = true
): String {
    val gContext = context as? GuildApplicationCommandContext
    return cleanUpForOutput(context.loritta, gContext?.guildId, gContext?.member?.roleIds, input, escapeMentions, stripCodeBackticks, stripInvites)
}

/**
 * Clean up and escape user input, useful when displaying user input
 */
suspend fun cleanUpForOutput(
    context: UnleashedContext,
    input: String,
    escapeMentions: Boolean = true,
    stripCodeBackticks: Boolean = true,
    stripInvites: Boolean = true
): String {
    return cleanUpForOutput(context.loritta, context.guildOrNull?.idLong?.let { Snowflake(it) }, context.memberOrNull?.roles?.map { Snowflake(it.idLong) }?.toSet() ?: emptySet(), input, escapeMentions, stripCodeBackticks, stripInvites)
}

/**
 * Clean up and escape user input, useful when displaying user input
 */
suspend fun cleanUpForOutput(
    loritta: LorittaBot,
    guildId: Snowflake?,
    memberRoleIds: Set<Snowflake>?,
    input: String,
    escapeMentions: Boolean = true,
    stripCodeBackticks: Boolean = true,
    stripInvites: Boolean = true
): String {
    val canBypassInviteBlocker = if (guildId != null && memberRoleIds != null)
        loritta.pudding.serverConfigs.hasLorittaPermission(guildId.value, memberRoleIds.map { it.value }, LorittaPermission.ALLOW_INVITES)
    else
        true // This is in a DM then, so let's allow the user to bypass the check

    return cleanUpForOutput(
        input,
        canBypassInviteBlocker,
        escapeMentions,
        stripCodeBackticks,
        stripInvites
    )
}

/**
 * Clean up and escape user input, useful when displaying user input
 */
suspend fun cleanUpForOutput(
    loritta: LorittaBot,
    guildId: Long?,
    memberRoleIds: Set<Long>?,
    input: String,
    escapeMentions: Boolean = true,
    stripCodeBackticks: Boolean = true,
    stripInvites: Boolean = true
): String {
    val canBypassInviteBlocker = if (guildId != null && memberRoleIds != null)
        loritta.pudding.serverConfigs.hasLorittaPermission(guildId.toULong(), memberRoleIds.map { it.toULong() }, LorittaPermission.ALLOW_INVITES)
    else
        true // This is in a DM then, so let's allow the user to bypass the check

    return cleanUpForOutput(
        input,
        canBypassInviteBlocker,
        escapeMentions,
        stripCodeBackticks,
        stripInvites
    )
}

/**
 * Clean up and escape user input, useful when displaying user input
 */
fun cleanUpForOutput(
    input: String,
    canBypassInviteBlocker: Boolean,
    escapeMentions: Boolean = true,
    stripCodeBackticks: Boolean = true,
    stripInvites: Boolean = true
): String {
    var newInput = input

    if (escapeMentions) {
        // This is actually not required because we do filter the input with "allowedMentions", buuuut let's clean it up anyway
        newInput = input.replace(Regex("\\\\+@"), "@").replace("@", "@\u200B")
    }

    if (stripCodeBackticks) {
        newInput = newInput.stripCodeBackticks()
    }

    val hasInvites = !canBypassInviteBlocker && stripInvites && DiscordInviteUtils.hasInvite(newInput)
    if (hasInvites) {
        newInput = DiscordInviteUtils.stripInvites(newInput)
    }

    // If the input is blank, we will return a shrug ¯\\_(ツ)_/¯, to avoid commands that use `{input}` being displayed as ``
    return newInput.trim().ifBlank { "¯\\_(ツ)_/¯" }
}