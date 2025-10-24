package net.perfectdreams.loritta.morenitta.websitedashboard.components

import kotlinx.html.FlowContent
import kotlinx.html.SELECT
import kotlinx.html.div
import kotlinx.html.option
import kotlinx.html.select
import kotlinx.html.span
import kotlinx.html.stream.createHTML
import kotlinx.html.style
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.channel.concrete.NewsChannel
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.SVGIconUtils
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.SVGIcons

fun FlowContent.fancySelectMenu(attrs: SELECT.() -> (Unit) = {}) {
    select {
        attributes["bliss-component"] = "fancy-select-menu"
        attributes["fancy-select-menu-chevron-svg"] = SVGIcons.CaretDown.html.toString()

        attrs()
    }
}