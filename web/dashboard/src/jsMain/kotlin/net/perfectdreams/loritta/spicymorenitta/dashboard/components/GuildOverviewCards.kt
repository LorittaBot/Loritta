package net.perfectdreams.loritta.spicymorenitta.dashboard.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import net.perfectdreams.loritta.spicymorenitta.dashboard.screen.Screen
import net.perfectdreams.loritta.spicymorenitta.dashboard.utils.State
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.css.AlignItems
import org.jetbrains.compose.web.css.DisplayStyle
import org.jetbrains.compose.web.css.FlexDirection
import org.jetbrains.compose.web.css.alignItems
import org.jetbrains.compose.web.css.color
import org.jetbrains.compose.web.css.display
import org.jetbrains.compose.web.css.em
import org.jetbrains.compose.web.css.flexDirection
import org.jetbrains.compose.web.css.height
import org.jetbrains.compose.web.css.rgb
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.H2
import org.jetbrains.compose.web.dom.Input
import org.jetbrains.compose.web.dom.Text

@Composable
fun GuildOverviewCards(screen: Screen.UserOverview) {
    var filter by remember { mutableStateOf("") }

    H2 {
        Text("Servidores")

        Input(InputType.Text) {
            onInput {
                filter = it.value
            }
        }
    }

    // TODO: Add warning if the user doesn't have any guilds ("Are you logged in into the correct account?")
    // TODO: Add warning if filter doesn't match any server
    when (val state = screen.model.guilds) {
        is State.Success -> {
            val guilds = state.value
            val filteredGuilds = guilds.filter { it.name.contains(filter, true) }
                .sortedBy { it.name }
            if (guilds.isNotEmpty()) {
                if (filteredGuilds.isNotEmpty()) {
                    GuildOverviewCardsGrid(filteredGuilds)
                } else {
                    Text("Nenhum servidor é compatível com o filtro que você selecionou!")
                }
            } else {
                Text("Você não está em nenhum servidor! Tem certeza que você logou na conta certa?")
            }
        }
        is State.Loading -> {
            Div(
                attrs = {
                    style {
                        display(DisplayStyle.Flex)
                        flexDirection(FlexDirection.Column)
                        alignItems(AlignItems.Center)
                    }
                }
            ) {
                Div(
                    attrs = {
                        style {
                            width(5.em)
                            height(5.em)
                            color(rgb(0, 167, 255))
                        }
                    }
                ) {
                    LoadingIconTailSpin()
                }
                Text("Carregando alguma coisa... Espero que carregue logo né")
            }
        }
        is State.Failure -> Text("Deu ruim!")
    }
}