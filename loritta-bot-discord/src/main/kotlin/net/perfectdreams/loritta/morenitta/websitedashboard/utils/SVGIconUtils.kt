package net.perfectdreams.loritta.morenitta.websitedashboard.utils

import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.channel.Channel
import net.dv8tion.jda.api.entities.channel.concrete.NewsChannel
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel
import net.perfectdreams.loritta.morenitta.websitedashboard.svgicons.SVGIcon

object SVGIconUtils {
    fun getSVGIconForChannelFallbackIfNull(guild: Guild, channel: GuildChannel?): SVGIcon {
        if (channel == null)
            return SVGIcons.Asterisk

        return getSVGIconForChannel(guild, channel)
    }

    fun getSVGIconForChannel(guild: Guild, channel: GuildChannel): SVGIcon {
        val canEveryoneView = guild.publicRole.hasPermission(channel, Permission.VIEW_CHANNEL)

        return if (channel is NewsChannel) {
            if (canEveryoneView) {
                SVGIcons.AnnouncementChannel
            } else {
                SVGIcons.PrivateAnnouncementChannel
            }
        } else {
            if (guild.rulesChannel == channel) {
                // Rules channel do not have a locked icon variant
                SVGIcons.RulesChannel
            } else {
                if (canEveryoneView) {
                    SVGIcons.TextChannel
                } else {
                    SVGIcons.PrivateTextChannel
                }
            }
        }
    }
}