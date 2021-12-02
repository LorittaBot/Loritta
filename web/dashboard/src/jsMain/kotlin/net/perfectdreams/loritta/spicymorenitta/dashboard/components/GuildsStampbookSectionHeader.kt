package net.perfectdreams.loritta.spicymorenitta.dashboard.components

import androidx.compose.runtime.Composable
import net.perfectdreams.loritta.cinnamon.pudding.data.discord.PartialDiscordGuild
import net.perfectdreams.loritta.spicymorenitta.dashboard.utils.State
import org.jetbrains.compose.web.dom.Div
import org.w3c.dom.asList
import org.w3c.dom.parsing.DOMParser
import org.w3c.dom.svg.SVGElement
import org.w3c.dom.svg.SVGImageElement

@Composable
fun GuildsStampbookSectionHeader(guildsState: State<List<PartialDiscordGuild>>) {
    Div(attrs = {
        classes("user-guilds-wrapper")

        // The check must be in here, state is not updated within a ref
        if (guildsState is State.Success) {
            ref { htmlDivElement ->
                // Only show the SVG if the guilds are loaded!

                val parser = DOMParser()
                val document = parser.parseFromString(svgGuildsOverviewStampbook, "image/svg+xml")

                val svgTag = document.getElementsByTagName("svg")
                    .asList()
                    .first() as SVGElement

                var notUsedYetGuilds = mutableListOf<PartialDiscordGuild>()

                svgTag.setAttributeNS(null, "preserveAspectRatio", "xMidYMid slice")
                svgTag.setAttribute("style", "width:100%;height:100%;")

                val guildIcons = svgTag.querySelectorAll(".guild-icon-priority-1").asList() +
                        svgTag.querySelectorAll(".guild-icon-priority-2").asList() +
                        svgTag.querySelectorAll(".guild-icon-priority-3").asList() +
                        svgTag.querySelectorAll(".guild-icon-priority-4").asList() +
                        svgTag.querySelectorAll(".guild-icon-priority-5").asList() +
                        svgTag.querySelectorAll(".guild-icon-priority-6").asList()

                guildIcons.filterIsInstance<SVGImageElement>()
                    .forEach {
                        if (notUsedYetGuilds.isEmpty()) {
                            notUsedYetGuilds = guildsState.value
                                .sortedWith(
                                    // TODO: Fix this compare by to compare permissions too!
                                    compareBy<PartialDiscordGuild> { it.owner }
                                        .thenBy { it.features.size }
                                        .thenBy { it.name }
                                )
                                .toMutableList()
                        }

                        println("SVG width: ${it.width.baseVal.value}")
                        console.log(it.getBoundingClientRect())

                        val guildData = notUsedYetGuilds.first()

                        // TODO: Add default icon if not present
                        // This is always webp, not animated, to avoid lag
                        val extension = "webp"

                        val discordIconUrl =
                            "https://cdn.discordapp.com/icons/${guildData.id}/${guildData.icon}.$extension?size=256"
                        it.setAttribute("xlink:href", discordIconUrl)

                        notUsedYetGuilds.remove(guildData)
                    }

                htmlDivElement.innerHTML = svgTag.outerHTML

                onDispose {}
            }
        }
    })
}