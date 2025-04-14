package net.perfectdreams.loritta.website.backend.utils

import io.ktor.http.*

// Examples:
// https://discordapp.com/oauth2/authorize?redirect_uri=https://loritta.website%2Fdashboard&scope=identify%20guilds%20email&response_type=code&client_id=297153970613387264
// https://discordapp.com/oauth2/authorize?client_id=297153970613387264&scope=bot+identify+guilds+email+applications.commands&permissions=2080374975&response_type=code&redirect_uri=https://loritta.website/dashboard
fun DiscordOAuth2AuthorizationURL(
    parameters: ParametersBuilder.() -> (Unit)
) = URLBuilder(
    protocol = URLProtocol.HTTPS,
    host = "discord.com",
    pathSegments = listOf("oauth2", "authorize"),
    parameters = ParametersBuilder().apply(parameters).build()
).build()