package net.perfectdreams.loritta.morenitta.utils

import io.ktor.http.*
import net.perfectdreams.loritta.morenitta.LorittaBot

/**
 * Builds Loritta's Discord OAuth2 Add Bot `authorize` URL
 *
 * This is an "extension" built on top of the original [net.perfectdreams.loritta.common.utils.LorittaDiscordOAuth2AddBotURL], which automatically provides
 * the `clientId` and `redirectUri` parameters by providing a [LorittaBot] instance
 *
 * @params loritta                        Loritta's instance
 * @params guildId                        the guild ID that will be prefilled in the add bot modal
 * @params redirectAfterAuthenticationUrl where the user will be redirected after their authentication has been validated
 * @params parameters additional OAuth2 URL parameters
 */
fun LorittaDiscordOAuth2AddBotURL(
    loritta: LorittaBot,
    guildId: Long? = null,
    redirectAfterAuthenticationUrl: String? = null,
    parameters: ParametersBuilder.() -> (Unit) = {}
) = net.perfectdreams.loritta.common.utils.LorittaDiscordOAuth2AddBotURL(
    loritta.config.loritta.discord.applicationId,
    "${loritta.config.loritta.dashboard.url.removeSuffix("/")}/discord/login",
    guildId,
    redirectAfterAuthenticationUrl,
    parameters
)

/**
 * Builds Loritta's Discord OAuth2 `authorize` URL
 *
 * This is an "extension" built on top of the original [net.perfectdreams.loritta.common.utils.LorittaDiscordOAuth2AuthorizeScopeURL], which automatically provides
 * the `clientId` and `redirectUri` parameters by providing a [LorittaBot] instance
 *
 * @params loritta                        Loritta's instance
 * @params guildId                        the guild ID that will be prefilled in the add bot modal
 * @params redirectAfterAuthenticationUrl where the user will be redirected after their authentication has been validated
 * @params parameters additional OAuth2 URL parameters
 */
fun LorittaDiscordOAuth2AuthorizeScopeURL(
    loritta: LorittaBot,
    redirectAfterAuthenticationUrl: String? = null,
    parameters: ParametersBuilder.() -> (Unit) = {}
) = net.perfectdreams.loritta.common.utils.LorittaDiscordOAuth2AuthorizeScopeURL(
    loritta.config.loritta.discord.applicationId,
    "${loritta.config.loritta.dashboard.url.removeSuffix("/")}/discord/login",
    redirectAfterAuthenticationUrl,
    parameters
)