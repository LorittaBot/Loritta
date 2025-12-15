package net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.added

import io.ktor.server.application.*
import io.ktor.server.response.respondRedirect
import io.ktor.server.util.getOrFail
import kotlinx.coroutines.delay
import kotlinx.html.b
import kotlinx.html.body
import kotlinx.html.li
import kotlinx.html.p
import kotlinx.html.ul
import net.dv8tion.jda.api.Permission
import net.perfectdreams.harmony.logging.HarmonyLoggerFactory
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.BannedUsers
import net.perfectdreams.loritta.cinnamon.pudding.tables.BlacklistedGuilds
import net.perfectdreams.loritta.common.utils.Emotes
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaUserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.RequiresUserAuthDashboardLocalizedRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.respondHtml
import net.perfectdreams.loritta.serializable.ColorTheme
import net.perfectdreams.loritta.shimeji.LorittaShimejiSettings
import org.jetbrains.exposed.sql.selectAll

class AddedLorittaGuildDashboardRoute(website: LorittaDashboardWebServer) : RequiresUserAuthDashboardLocalizedRoute(website, "/guilds/{guildId}/added") {
    companion object {
        private val logger by HarmonyLoggerFactory.logger {}
    }

    override suspend fun onAuthenticatedRequest(
        call: ApplicationCall,
        i18nContext: I18nContext,
        session: LorittaUserSession,
        userPremiumPlan: UserPremiumPlans,
        theme: ColorTheme,
        shimejiSettings: LorittaShimejiSettings
    ) {
        // Notice that we DO NOT use the RequiresGuildAuthDiscordLocalizedRoute for this route!
        // This is intentional, because Loritta MAY NOT be in the server yet (due to gateway latency, lag, or maybe she was kicked?!)
        val guildId = call.parameters.getOrFail("guildId").toLong()

        var tries = 0
        val maxGuildTries = website.loritta.config.loritta.website.maxGuildTries

        while (true) {
            val guild = website.loritta.lorittaShards.getGuildById(guildId)

            if (guild != null) {
                logger.info { "Guild ${guild} was successfully found after $tries tries! Yay!!" }

                val serverConfig = website.loritta.getOrCreateServerConfig(guild.idLong)

                // Now we are going to save the server's new locale ID, based on the user's locale
                // This fixes issues because Discord doesn't provide the voice channel server anymore
                // (which, well, was already a huge workaround anyway)
                website.loritta.newSuspendedTransaction {
                    // Patches and workarounds!!!
                    val legacyLocaleId = when (website.loritta.languageManager.getIdByI18nContext(i18nContext)) {
                        "pt" -> "default"
                        "en-us" -> "en"
                        else -> "default"
                    }
                    val locale = website.loritta.localeManager.getLocaleById(legacyLocaleId)

                    serverConfig.localeId = legacyLocaleId

                    val userId = session.userId

                    val user = website.loritta.lorittaShards.retrieveUserById(userId)

                    if (user != null) {
                        val member = guild.getMember(user)

                        if (member != null) {
                            // E, se o membro não for um bot e possui permissão de gerenciar o servidor ou permissão de administrador...
                            if (!user.isBot && (member.hasPermission(Permission.MANAGE_SERVER) || member.hasPermission(Permission.ADMINISTRATOR))) {
                                // Verificar coisas antes de adicionar a Lori
                                val blacklisted = website.loritta.transaction {
                                    BlacklistedGuilds.selectAll().where {
                                        BlacklistedGuilds.id eq guild.idLong
                                    }.firstOrNull()
                                }

                                if (blacklisted != null) {
                                    val blacklistedReason = blacklisted[BlacklistedGuilds.reason]

                                    // Envie via DM uma mensagem falando sobre o motivo do ban
                                    val message = locale.getList("website.router.blacklistedServer", blacklistedReason)

                                    website.loritta.getOrRetrievePrivateChannelForUser(user)
                                        .sendMessage(message.joinToString("\n")).queue({
                                            guild.leave().queue()
                                        }, {
                                            guild.leave().queue()
                                        })
                                    return@newSuspendedTransaction
                                }

                                val guildOwner = guild.owner

                                // Sometimes the guild owner can be null, that's why we need to check if it is null or not!
                                if (guildOwner != null) {
                                    val profile = website.loritta.getLorittaProfile(guildOwner.user.id)
                                    val bannedState = profile?.getBannedState(website.loritta)
                                    if (bannedState != null) { // Dono blacklisted
                                        // Envie via DM uma mensagem falando sobre a Loritta!
                                        val message = locale.getList("website.router.ownerLorittaBanned", guild.owner?.user?.asMention, bannedState[BannedUsers.reason]).joinToString("\n")

                                        website.loritta.getOrRetrievePrivateChannelForUser(user)
                                            .sendMessage(message)
                                            .queue({
                                                guild.leave().queue()
                                            }, {
                                                guild.leave().queue()
                                            })
                                        return@newSuspendedTransaction
                                    }

                                    // Envie via DM uma mensagem falando sobre a Loritta!
                                    val message = locale.getList(
                                        "website.router.addedOnServer",
                                        user.asMention,
                                        guild.name,
                                        website.loritta.config.loritta.website.url + "commands",
                                        website.loritta.config.loritta.dashboard.url.removeSuffix("/") + "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/guilds/${guild.id}/overview",
                                        website.loritta.config.loritta.website.url + "guidelines",
                                        website.loritta.config.loritta.website.url + "donate",
                                        website.loritta.config.loritta.website.url + "support",
                                        Emotes.LORI_PAT,
                                        Emotes.LORI_NICE,
                                        Emotes.LORI_HEART,
                                        Emotes.LORI_COFFEE,
                                        Emotes.LORI_SMILE,
                                        Emotes.LORI_PRAY,
                                        Emotes.LORI_BAN_HAMMER,
                                        Emotes.LORI_RICH,
                                        Emotes.LORI_HEART1.toString() + Emotes.LORI_HEART2.toString()
                                    ).joinToString("\n")

                                    website.loritta
                                        .getOrRetrievePrivateChannelForUser(user)
                                        .sendMessage(message)
                                        .queue()
                                }
                            }
                        }
                    }
                }

                call.respondRedirect("/guilds/${guild.id}/overview")
                return
            }

            if (tries == maxGuildTries) {
                // oof
                logger.warn { "Received guild $guildId via OAuth2 scope, we tried ${maxGuildTries} times, but I'm not in that guild yet! Telling the user about the issue..." }

                call.respondHtml {
                    body {
                        p {
                            text("Parece que você tentou me adicionar no seu servidor, mas mesmo assim eu não estou nele!")
                        }

                        ul {
                            li {
                                text("Tente me readicionar, as vezes isto acontece devido a um delay entre o tempo até o Discord atualizar os servidores que eu estou.")
                            }

                            li {
                                text("Verifique o registro de auditoria do seu servidor, alguns bots expulsam/banem ao adicionar novos bots. Caso isto tenha acontecido, expulse o bot que me puniu e me readicione!")

                                ul {
                                    li {
                                        b {
                                            text("Em vez de confiar em um bot para \"proteger\" o seu servidor: ")
                                        }

                                        text("Veja quem possui permissão de administrador ou de gerenciar servidores no seu servidor, eles são os únicos que conseguem adicionar bots no seu servidor. Existem boatos que existem \"bugs que permitem adicionar bots sem permissão\", mas isto é mentira.")
                                    }
                                }

                            }
                        }

                        p {
                            text("Desculpe pela inconveniência ;w;")
                        }
                    }
                }
                return
            }

            tries++
            logger.warn { "Received guild $guildId via OAuth2 scope, but I'm not in that guild yet! Waiting for 1s... Tries: ${tries}" }
            delay(1_000)
        }
    }
}