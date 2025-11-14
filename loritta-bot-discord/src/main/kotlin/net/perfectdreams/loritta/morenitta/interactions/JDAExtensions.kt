package net.perfectdreams.loritta.morenitta.interactions

import dev.minn.jda.ktx.interactions.components.ButtonDefaults
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.components.buttons.Button
import net.dv8tion.jda.api.components.buttons.ButtonStyle
import net.dv8tion.jda.api.entities.Role
import net.dv8tion.jda.api.requests.Route
import net.dv8tion.jda.internal.requests.RestActionImpl
import net.perfectdreams.loritta.common.emotes.DiscordEmote
import net.perfectdreams.loritta.common.emotes.Emote
import net.perfectdreams.loritta.common.emotes.UnicodeEmote
import net.perfectdreams.loritta.morenitta.utils.extensions.await


/**
 * Create a button with keyword arguments.
 *
 * This will use the defaults from [ButtonDefaults] unless specified as parameters.
 *
 * @param [id] The component id to use.
 * @param [style] The button style.
 * @param [label] The button label
 * @param [emoji] The button emoji
 *
 * @return [Button] The resulting button instance.
 */
fun linkButton(
    url: String,
    label: String? = null,
    emoji: Emote? = null,
    disabled: Boolean = false,
) = Button.of(
    ButtonStyle.LINK,
    url,
    label,
    when (emoji) {
        is DiscordEmote -> Emoji.fromCustom(emoji.name, emoji.id, emoji.animated)
        is UnicodeEmote -> Emoji.fromUnicode(emoji.asMention)
        null -> null
    }
).withDisabled(disabled)

fun Role.retrieveMemberCount(): Long {
    val compiledRoute = Route.get("guilds/{guild_id}/roles/member-counts").compile(guild.id)

    val request = runBlocking {
        jda.makeRequest<Map<String, Long>>(compiledRoute)
    }

    return request.getValue(id)
}

/**
 * Extension function used to make direct requests to Discord's API that JDA doesn't support yet.
 * Useful for new, experimental, or custom endpoints.
 *
 * @param compiledRoute The compiled route with all parameters properly handled (path, query, etc).
 * @return The response body parsed as [T], or nothing if the response is empty.
 */
suspend inline fun <reified T: Any?> JDA.makeRequest(compiledRoute: Route.CompiledRoute): T  {
    val request = RestActionImpl(this, compiledRoute) { response, _ ->
        val bodyAsString = response.string

        if (bodyAsString.isBlank()) {
            if (T::class == Unit::class) Unit as T
            else error("Empty response body for ${compiledRoute.method} ${compiledRoute.compiledRoute}")
        } else {
            Json.decodeFromString<T>(response.string)
        }
    }

    return request.await()
}