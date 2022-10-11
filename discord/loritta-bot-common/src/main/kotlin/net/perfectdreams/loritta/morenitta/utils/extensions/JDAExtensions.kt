package net.perfectdreams.loritta.morenitta.utils.extensions

import dev.kord.common.entity.ChannelType
import dev.kord.common.entity.Permission
import dev.kord.common.entity.Permission.*
import net.perfectdreams.loritta.deviousfun.entities.*
import net.perfectdreams.loritta.deviousfun.*

suspend fun Channel.sendMessageAsync(text: String) = this.sendMessage(text)
suspend fun Channel.sendMessageAsync(message: DeviousMessage) = this.sendMessage(message)
suspend fun Channel.sendMessageAsync(embed: DeviousEmbed) = this.sendMessage(embed)

suspend fun Message.edit(message: String, embed: DeviousEmbed, clearReactions: Boolean = true): Message {
    return this.edit(MessageBuilder().setEmbed(embed).append(if (message.isEmpty()) " " else message).build(), clearReactions)
}

suspend fun Message.edit(content: DeviousMessage, clearReactions: Boolean = true): Message {
    if (this.isFromType(ChannelType.DM) || !this.guild.retrieveSelfMember().hasPermission(this.textChannel, Permission.ManageMessages)) {
        // Nós não podemos limpar as reações das mensagens caso a gente esteja em uma DM ou se a Lori não tem permissão para gerenciar mensagens
        // Nestes casos, iremos apenas deletar a mensagem e reenviar
        runCatching { this.delete() }
        return this.channel.sendMessage(content)
    }

    // Se não, vamos apagar as reações e editar a mensagem atual!
    if (clearReactions)
        this.clearReactions()
    return this.editMessage(content)
}

/**
 * Edits the message, but only if the content was changed
 *
 * This reduces the number of API requests needed
 */
suspend fun Message.editMessageIfContentWasChanged(message: String): Message {
    if (this.contentRaw == message)
        return this

    return this.editMessage(message)
}

/**
 * Adds the [emotes] to the [message] if needed, this avoids a lot of unnecessary API requests
 */
suspend fun Message.doReactions(vararg emotes: String): Message {
    var message = this

    var clearAll = false

    // Vamos pegar todas as reações que não deveriam estar aqui

    val emoteOnlyIds = emotes.map { str -> str.split(":").getOrNull(1) }.filterNotNull()

    val invalidReactions = this.reactions.filterNot {
        if (it.reactionEmote.isEmote)
            emoteOnlyIds.contains(it.reactionEmote.id)
        else
            emotes.contains(it.reactionEmote.name)
    }

    if (invalidReactions.isNotEmpty())
        clearAll = true

    // Se o número de reações for diferente das reações na mensagem, então algo está errado ;w;
    if (this.reactions.size != emotes.size)
        clearAll = true

    if (clearAll) { // Pelo visto tem alguns emojis que não deveriam estar aqui, vamos limpar!
        this.clearReactions() // Vamos limpar todas as reações
        message = this.refresh() // E pegar o novo obj da mensagem

        emotes.forEach {
            // E agora vamos readicionar os emotes!
            message.addReaction(it)
        }
    }
    return message
}

/**
 * Checks if a role is a valid giveable role (not managed, not a public role, etc).
 *
 * @return       if the role can be given to the specified member
 */
suspend fun Role.canBeGiven() = !this.isPublicRole &&
        !this.isManaged &&
        guild.retrieveSelfMember().canInteract(this)

/**
 * Filters a role list with [canBeGiven].
 *
 * @param member the member that the role will be given to
 * @return       all roles that can be given to the member
 */
suspend fun Collection<Role>.filterOnlyGiveableRoles() = this.filter { it.canBeGiven() }

/**
 * Filters a role list with [canBeGiven].
 *
 * @param member the member that the role will be given to
 * @return       all roles that can be given to the member
 */
suspend fun Sequence<Role>.filterOnlyGiveableRoles() = this.toList().filter { it.canBeGiven() }