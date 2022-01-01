package net.perfectdreams.loritta.spicymorenitta.dashboard.components

import androidx.compose.runtime.Composable
import net.perfectdreams.loritta.cinnamon.pudding.data.discord.PartialDiscordGuild
import org.jetbrains.compose.web.dom.Div

@Composable
fun GuildOverviewCardsGrid(guilds: List<PartialDiscordGuild>) {
    Div(attrs = { classes("guild-overview-cards-grid") }) {
        for (guild in guilds)
            GuildOverviewCard(guild)
    }
}