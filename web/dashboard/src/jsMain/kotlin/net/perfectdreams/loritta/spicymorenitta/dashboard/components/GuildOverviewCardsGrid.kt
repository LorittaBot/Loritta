package net.perfectdreams.loritta.spicymorenitta.dashboard.components

import androidx.compose.runtime.Composable
import net.perfectdreams.loritta.spicymorenitta.dashboard.styles.GuildCardsStylesheet
import net.perfectdreams.loritta.webapi.data.PartialDiscordGuild
import org.jetbrains.compose.web.dom.Div

@Composable
fun GuildOverviewCardsGrid(guilds: List<PartialDiscordGuild>) {
    Div(attrs = { classes(GuildCardsStylesheet.guildOverviewCardsGrid) }) {
        for (guild in guilds)
            GuildOverviewCard(guild)
    }
}