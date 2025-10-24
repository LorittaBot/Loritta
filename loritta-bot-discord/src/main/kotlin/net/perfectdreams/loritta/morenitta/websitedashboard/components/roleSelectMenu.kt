package net.perfectdreams.loritta.morenitta.websitedashboard.components

import kotlinx.html.FlowContent
import kotlinx.html.SELECT
import kotlinx.html.body
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
import java.awt.Color

fun FlowContent.roleSelectMenu(
    guild: Guild,
    selectedRoleId: Long?,
    additionalOptions: SELECT.() -> (Unit) = {},
    attrs: SELECT.() -> (Unit) = {}
) {
    select {
        attributes["bliss-component"] = "fancy-select-menu"
        attributes["fancy-select-menu-chevron-svg"] = SVGIcons.CaretDown.html.toString()

        attrs()

        additionalOptions()

        for (role in guild.roles) {
            if (role.isPublicRole)
                continue

            val roleColor = role.color ?: Color(153, 170, 181)

            option {
                this.attributes["fancy-select-menu-label"] = createHTML(false)
                    .body {
                        div {
                            style = "display: flex; gap: 4px; align-items: center;"

                            svgIcon(SVGIcons.RoleShield) {
                                attr("style", "width: 1.25em; height: 1.25em; color: rgb(${roleColor.red}, ${roleColor.green}, ${roleColor.blue});")
                            }

                            text(role.name)
                        }
                    }

                this.label = role.name
                this.value = role.id
                this.selected = role.idLong == selectedRoleId
                this.disabled = false
            }
        }
    }
}