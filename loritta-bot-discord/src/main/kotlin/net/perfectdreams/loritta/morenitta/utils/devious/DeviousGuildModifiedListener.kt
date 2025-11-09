package net.perfectdreams.loritta.morenitta.utils.devious

import net.dv8tion.jda.api.events.channel.GenericChannelEvent
import net.dv8tion.jda.api.events.emoji.GenericEmojiEvent
import net.dv8tion.jda.api.events.guild.GenericGuildEvent
import net.dv8tion.jda.api.events.guild.GuildAuditLogEntryCreateEvent
import net.dv8tion.jda.api.events.guild.GuildReadyEvent
import net.dv8tion.jda.api.events.guild.member.GuildMemberUpdateEvent
import net.dv8tion.jda.api.events.role.GenericRoleEvent
import net.dv8tion.jda.api.events.sticker.GenericGuildStickerEvent
import net.dv8tion.jda.api.events.thread.GenericThreadEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.perfectdreams.harmony.logging.HarmonyLoggerFactory
import net.perfectdreams.loritta.morenitta.LorittaBot

// Tracks whenever a guild is modified
// This doesn't need to be 100% precise, we just need it to be "good enough" to avoid the more hefty checks (creating a GuildCreateEvent + storing on the db)
class DeviousGuildModifiedListener(val m: LorittaBot) : ListenerAdapter() {
    companion object {
        private val logger by HarmonyLoggerFactory.logger {}
    }

    override fun onGenericGuild(event: GenericGuildEvent) {
        if (event is GuildReadyEvent)
            return

        if (event is GuildAuditLogEntryCreateEvent)
            return

        if (event is GuildMemberUpdateEvent && event.member.idLong != event.guild.selfMember.idLong)
            return

        logger.info { "Guild ${event.guild.idLong} marked as modified! Cause: $event" }
        m.unmodifiedGuilds.remove(event.guild.idLong)
    }

    override fun onGenericEmoji(event: GenericEmojiEvent) {
        logger.info { "Guild ${event.guild.idLong} marked as modified! Cause: $event" }
        m.unmodifiedGuilds.remove(event.guild.idLong)
    }

    override fun onGenericRole(event: GenericRoleEvent) {
        logger.info { "Guild ${event.guild.idLong} marked as modified! Cause: $event" }
        m.unmodifiedGuilds.remove(event.guild.idLong)
    }

    override fun onGenericChannel(event: GenericChannelEvent) {
        if (!event.isFromGuild)
            return

        logger.info { "Guild ${event.guild.idLong} marked as modified! Cause: $event" }
        m.unmodifiedGuilds.remove(event.guild.idLong)
    }

    override fun onGenericThread(event: GenericThreadEvent) {
        logger.info { "Guild ${event.guild.idLong} marked as modified! Cause: $event" }
        m.unmodifiedGuilds.remove(event.guild.idLong)
    }

    override fun onGenericGuildSticker(event: GenericGuildStickerEvent) {
        logger.info { "Guild ${event.guild.idLong} marked as modified! Cause: $event" }
        m.unmodifiedGuilds.remove(event.guild.idLong)
    }
}