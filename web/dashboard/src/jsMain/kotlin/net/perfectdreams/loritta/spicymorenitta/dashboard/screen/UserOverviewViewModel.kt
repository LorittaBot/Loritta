package net.perfectdreams.loritta.spicymorenitta.dashboard.screen

import SpicyMorenitta
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import http
import io.ktor.client.request.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import net.perfectdreams.loritta.cinnamon.pudding.data.discord.PartialDiscordGuild
import net.perfectdreams.loritta.spicymorenitta.dashboard.utils.State

class UserOverviewViewModel(private val m: SpicyMorenitta) {
    var guilds by mutableStateOf<State<List<PartialDiscordGuild>>>(State.Loading())

    fun loadData() {
        GlobalScope.launch {
            println("Faking delay...")

            val response = http.get<String>("http://127.0.0.1:8000/api/v1/users/@me/guilds") {}
            /* val guilds = listOf(
                SpicyMorenitta.GuildData(
                    0L,
                    "Floppa Shy",
                    "https://cdn.discordapp.com/emojis/806937807771795486.png?size=160",
                    listOf("PARTNERED", "VERIFIED")
                ),
                SpicyMorenitta.GuildData(
                    1L,
                    "Floppa Shy",
                    "https://cdn.discordapp.com/emojis/806937807771795486.png?size=160",
                    listOf("PARTNERED")
                ),
                SpicyMorenitta.GuildData(
                    2L,
                    "Floppa Shy",
                    "https://cdn.discordapp.com/emojis/806937807771795486.png?size=160",
                    listOf("VERIFIED")
                ),
                SpicyMorenitta.GuildData(
                    3L,
                    "Floppa Shy",
                    "https://cdn.discordapp.com/emojis/806937807771795486.png?size=160",
                    listOf()
                ),
                SpicyMorenitta.GuildData(
                    4L,
                    "Floppa Shy",
                    "https://cdn.discordapp.com/emojis/806937807771795486.png?size=160",
                    listOf()
                ),
                SpicyMorenitta.GuildData(
                    5L,
                    "Floppa Shy",
                    "https://cdn.discordapp.com/emojis/806937807771795486.png?size=160",
                    listOf()
                ),
                SpicyMorenitta.GuildData(
                    6L,
                    "Floppa Shy",
                    "https://cdn.discordapp.com/emojis/806937807771795486.png?size=160",
                    listOf()
                ),
                SpicyMorenitta.GuildData(
                    7L,
                    "Floppa Shy",
                    "https://cdn.discordapp.com/emojis/806937807771795486.png?size=160",
                    listOf()
                ),
                SpicyMorenitta.GuildData(
                    8L,
                    "Floppa Shy",
                    "https://cdn.discordapp.com/emojis/806937807771795486.png?size=160",
                    listOf()
                ),
                SpicyMorenitta.GuildData(
                    9L,
                    "Floppa Shy",
                    "https://cdn.discordapp.com/emojis/806937807771795486.png?size=160",
                    listOf()
                ),
                SpicyMorenitta.GuildData(
                    10L,
                    "Floppa Shy",
                    "https://cdn.discordapp.com/emojis/806937807771795486.png?size=160",
                    listOf()
                ),
                SpicyMorenitta.GuildData(
                    11L,
                    "Floppa Shy",
                    "https://cdn.discordapp.com/emojis/806937807771795486.png?size=160",
                    listOf()
                ),
                SpicyMorenitta.GuildData(
                    12L,
                    "Floppa Shy",
                    "https://cdn.discordapp.com/emojis/806937807771795486.png?size=160",
                    listOf()
                ),
                SpicyMorenitta.GuildData(
                    13L,
                    "Floppa Shy",
                    "https://cdn.discordapp.com/emojis/806937807771795486.png?size=160",
                    listOf()
                ),
                SpicyMorenitta.GuildData(
                    14L,
                    "Floppa Shy",
                    "https://cdn.discordapp.com/emojis/806937807771795486.png?size=160",
                    listOf()
                ),
                SpicyMorenitta.GuildData(
                    15L,
                    "Floppa Shy",
                    "https://cdn.discordapp.com/emojis/806937807771795486.png?size=160",
                    listOf()
                ),
                SpicyMorenitta.GuildData(
                    16L,
                    "Floppa Shy",
                    "https://cdn.discordapp.com/emojis/806937807771795486.png?size=160",
                    listOf()
                ),
                SpicyMorenitta.GuildData(
                    17L,
                    "Floppa Shy",
                    "https://cdn.discordapp.com/emojis/806937807771795486.png?size=160",
                    listOf()
                ),
                SpicyMorenitta.GuildData(
                    18L,
                    "Floppa Shy",
                    "https://cdn.discordapp.com/emojis/806937807771795486.png?size=160",
                    listOf()
                ),
                SpicyMorenitta.GuildData(
                    19L,
                    "Floppa Shy",
                    "https://cdn.discordapp.com/emojis/806937807771795486.png?size=160",
                    listOf()
                ),
                SpicyMorenitta.GuildData(
                    20L,
                    "Floppa Shy",
                    "https://cdn.discordapp.com/emojis/806937807771795486.png?size=160",
                    listOf()
                ),
                SpicyMorenitta.GuildData(
                    21L,
                    "Floppa Shy",
                    "https://cdn.discordapp.com/emojis/806937807771795486.png?size=160",
                    listOf()
                ),
                SpicyMorenitta.GuildData(
                    22L,
                    "Floppa Shy",
                    "https://cdn.discordapp.com/emojis/806937807771795486.png?size=160",
                    listOf()
                ),

                SpicyMorenitta.GuildData(
                    23L,
                    "Loritta",
                    "https://cdn.discordapp.com/icons/297732013006389252/a_fee7591870d26c60af64179c4ab520ed.gif?size=2048",
                    listOf()
                )
            ) */

            println("Switch/Twitch!")
            this@UserOverviewViewModel.guilds = State.Success(Json.decodeFromString(response))
        }
    }
}