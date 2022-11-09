package net.perfectdreams.loritta.deviousfun.hooks

import net.perfectdreams.loritta.deviousfun.events.guild.GuildJoinEvent
import net.perfectdreams.loritta.deviousfun.events.guild.GuildLeaveEvent
import net.perfectdreams.loritta.deviousfun.events.guild.GuildReadyEvent
import net.perfectdreams.loritta.deviousfun.events.guild.member.GuildMemberJoinEvent
import net.perfectdreams.loritta.deviousfun.events.guild.member.GuildMemberRemoveEvent
import net.perfectdreams.loritta.deviousfun.events.guild.member.GuildMemberUpdateBoostTimeEvent
import net.perfectdreams.loritta.deviousfun.events.guild.member.GuildMemberUpdateNicknameEvent
import net.perfectdreams.loritta.deviousfun.events.guild.voice.GuildVoiceJoinEvent
import net.perfectdreams.loritta.deviousfun.events.guild.voice.GuildVoiceLeaveEvent
import net.perfectdreams.loritta.deviousfun.events.guild.voice.GuildVoiceMoveEvent
import net.perfectdreams.loritta.deviousfun.events.message.create.MessageReceivedEvent
import net.perfectdreams.loritta.deviousfun.events.message.delete.MessageBulkDeleteEvent
import net.perfectdreams.loritta.deviousfun.events.message.delete.MessageDeleteEvent
import net.perfectdreams.loritta.deviousfun.events.message.react.GenericMessageReactionEvent
import net.perfectdreams.loritta.deviousfun.events.message.react.MessageReactionAddEvent
import net.perfectdreams.loritta.deviousfun.events.message.update.MessageUpdateEvent

open class ListenerAdapter {
    open fun onGenericMessageReaction(event: GenericMessageReactionEvent) {}

    open fun onGuildMessageReactionAdd(event: MessageReactionAddEvent) {}
    open fun onGuildMemberJoin(event: GuildMemberJoinEvent) {}
    open fun onGuildMemberRemove(event: GuildMemberRemoveEvent) {}
    open fun onGuildMemberUpdateBoostTime(event: GuildMemberUpdateBoostTimeEvent) {}
    open fun onGuildMemberUpdateNickname(event: GuildMemberUpdateNicknameEvent) {}

    open fun onGuildReady(event: GuildReadyEvent) {}

    open fun onMessageReceived(event: MessageReceivedEvent) {}
    open fun onMessageUpdate(event: MessageUpdateEvent) {}
    open fun onMessageDelete(event: MessageDeleteEvent) {}
    open fun onMessageBulkDelete(event: MessageBulkDeleteEvent) {}

    open fun onGuildVoiceJoin(event: GuildVoiceJoinEvent) {}
    open fun onGuildVoiceMove(event: GuildVoiceMoveEvent) {}
    open fun onGuildVoiceLeave(event: GuildVoiceLeaveEvent) {}

    open fun onGuildJoin(event: GuildJoinEvent) {}
    open fun onGuildLeave(event: GuildLeaveEvent) {}
}