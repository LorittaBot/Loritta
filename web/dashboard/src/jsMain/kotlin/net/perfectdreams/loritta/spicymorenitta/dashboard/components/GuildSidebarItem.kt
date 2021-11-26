package net.perfectdreams.loritta.spicymorenitta.dashboard.components

import androidx.compose.runtime.Composable
import net.perfectdreams.loritta.spicymorenitta.dashboard.styles.AppStylesheet
import org.jetbrains.compose.web.dom.Div

@Composable
fun GuildSidebarItem(content: @Composable () -> Unit) {
    Div(
        attrs = { classes(AppStylesheet.guildSidebarItem) }
    ) {
        content()
    }
}