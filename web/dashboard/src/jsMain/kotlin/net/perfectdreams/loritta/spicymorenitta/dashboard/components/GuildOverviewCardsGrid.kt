package net.perfectdreams.loritta.spicymorenitta.dashboard.components

import androidx.compose.runtime.Composable
import net.perfectdreams.loritta.cinnamon.pudding.data.discord.PartialDiscordGuild
import net.perfectdreams.loritta.spicymorenitta.dashboard.styles.GuildCardsStylesheet
import org.jetbrains.compose.web.dom.Div

@Composable
fun GuildOverviewCardsGrid(guilds: List<PartialDiscordGuild>) {
    Div(attrs = { classes(GuildCardsStylesheet.guildOverviewCardsGrid) }) {
        for (guild in guilds)
            GuildOverviewCard(guild)
    }
}