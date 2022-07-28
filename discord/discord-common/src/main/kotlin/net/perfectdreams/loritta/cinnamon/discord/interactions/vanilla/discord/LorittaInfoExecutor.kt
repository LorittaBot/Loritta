package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.discord

import kotlinx.datetime.toKotlinInstant
import net.perfectdreams.discordinteraktions.common.builder.message.actionRow
import net.perfectdreams.discordinteraktions.common.builder.message.embed
import net.perfectdreams.discordinteraktions.common.utils.author
import net.perfectdreams.discordinteraktions.common.utils.footer
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.utils.GACampaigns
import net.perfectdreams.loritta.cinnamon.utils.LorittaColors
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.discord.declarations.LorittaCommand
import net.perfectdreams.discordinteraktions.common.commands.options.SlashCommandArguments
import net.perfectdreams.loritta.cinnamon.discord.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.CinnamonSlashCommandExecutor
import net.perfectdreams.loritta.cinnamon.discord.interactions.components.loriEmoji
import net.perfectdreams.loritta.cinnamon.discord.utils.toKordColor
import java.time.Instant

class LorittaInfoExecutor(loritta: LorittaCinnamon) : CinnamonSlashCommandExecutor(loritta) {
    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
        context.deferChannelMessage()

        val since = Instant.now()
            .minusSeconds(86400)
            .toKotlinInstant()

        val guildCount = context.loritta.services.stats.getGuildCount()
        val executedApplicationCommands = context.loritta.services.executedInteractionsLog.getExecutedApplicationCommands(since)
        val uniqueUsersExecutedApplicationCommands = context.loritta.services.executedInteractionsLog.getUniqueUsersExecutedApplicationCommands(since)

        context.sendMessage {
            embed {
                color = LorittaColors.LorittaAqua.toKordColor()

                author(context.i18nContext.get(LorittaCommand.I18N_PREFIX.Info.Embed.Title))

                description = context.i18nContext.get(
                    LorittaCommand.I18N_PREFIX.Info.Embed.Description(
                        guildCount = guildCount,
                        commandCount = context.loritta.getCommandCount(),
                        executedApplicationCommands = executedApplicationCommands,
                        uniqueUsersExecutedApplicationCommands = uniqueUsersExecutedApplicationCommands,
                        userMention = "<@${context.user.id.value}>",
                        loriSunglasses = net.perfectdreams.loritta.cinnamon.emotes.Emotes.LoriSunglasses,
                        loriYay = net.perfectdreams.loritta.cinnamon.emotes.Emotes.LoriYay,
                        loriKiss = net.perfectdreams.loritta.cinnamon.emotes.Emotes.LoriKiss,
                        loriHeart = net.perfectdreams.loritta.cinnamon.emotes.Emotes.LoriHeart
                    )
                ).joinToString("\n")

                image = "${context.loritta.config.website}v3/assets/img/sonhos/lori-space.gif"

                footer(context.i18nContext.get(LorittaCommand.I18N_PREFIX.Info.Embed.Footer("MrPowerGamerBR#4185", "https://mrpowergamerbr.com")), "https://mrpowergamerbr.com/assets/img/avatar.png")
            }

            actionRow {
                linkButton(
                    GACampaigns.createUrlWithCampaign(
                        "https://loritta.website",
                        "discord",
                        "loritta-info",
                        "loritta-info-links",
                        "home-page"
                    ).toString()
                ) {
                    label = context.i18nContext.get(LorittaCommand.I18N_PREFIX.Info.Website)
                    loriEmoji = net.perfectdreams.loritta.cinnamon.emotes.Emotes.LoriSunglasses
                }

                linkButton(
                    GACampaigns.createUrlWithCampaign(
                        "https://loritta.website/dashboard",
                        "discord",
                        "loritta-info",
                        "loritta-info-links",
                        "dashboard"
                    ).toString()
                ) {
                    label = context.i18nContext.get(LorittaCommand.I18N_PREFIX.Info.Dashboard)
                    loriEmoji = net.perfectdreams.loritta.cinnamon.emotes.Emotes.LoriReading
                }

                linkButton(
                    GACampaigns.createUrlWithCampaign(
                        "https://loritta.website/commands",
                        "discord",
                        "loritta-info",
                        "loritta-info-links",
                        "commands"
                    ).toString()
                ) {
                    label = context.i18nContext.get(LorittaCommand.I18N_PREFIX.Info.Commands)
                    loriEmoji = net.perfectdreams.loritta.cinnamon.emotes.Emotes.LoriWow
                }

                linkButton(
                    GACampaigns.createUrlWithCampaign(
                        "https://loritta.website/support",
                        "discord",
                        "loritta-info",
                        "loritta-info-links",
                        "support"
                    ).toString()
                ) {
                    label = context.i18nContext.get(LorittaCommand.I18N_PREFIX.Info.Support)
                    loriEmoji = net.perfectdreams.loritta.cinnamon.emotes.Emotes.LoriHm
                }

                linkButton(
                    GACampaigns.createUrlWithCampaign(
                        "https://loritta.website/donate",
                        "discord",
                        "loritta-info",
                        "loritta-info-links",
                        "premium"
                    ).toString()
                ) {
                    label = context.i18nContext.get(LorittaCommand.I18N_PREFIX.Info.Premium)
                    loriEmoji = net.perfectdreams.loritta.cinnamon.emotes.Emotes.LoriCard
                }
            }

            actionRow {
                linkButton("https://twitter.com/LorittaBot") {
                    label = "Twitter"
                    loriEmoji = net.perfectdreams.loritta.cinnamon.emotes.Emotes.Twitter
                }

                linkButton("https://instagram.com/lorittabot") {
                    label = "Instagram"
                    loriEmoji = net.perfectdreams.loritta.cinnamon.emotes.Emotes.Instagram
                }

                linkButton("https://youtube.com/c/Loritta") {
                    label = "YouTube"
                    loriEmoji = net.perfectdreams.loritta.cinnamon.emotes.Emotes.YouTube
                }

                linkButton("https://www.tiktok.com/@lorittamorenittabot") {
                    label = "TikTok"
                    loriEmoji = net.perfectdreams.loritta.cinnamon.emotes.Emotes.TikTok
                }

                linkButton("https://github.com/LorittaBot") {
                    label = "GitHub"
                    loriEmoji = net.perfectdreams.loritta.cinnamon.emotes.Emotes.GitHub
                }
            }
        }
    }
}