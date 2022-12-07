package net.perfectdreams.loritta.morenitta.interactions.vanilla.discord

import kotlinx.datetime.toKotlinInstant
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.utils.GACampaigns
import net.perfectdreams.loritta.common.utils.LorittaColors
import net.perfectdreams.loritta.common.utils.TodoFixThisData
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.interactions.commands.*
import net.perfectdreams.loritta.morenitta.interactions.linkButton
import java.time.Instant

class LorittaCommand : SlashCommandDeclarationWrapper {
    companion object {
        private val I18N_PREFIX = I18nKeysData.Commands.Command.Loritta
    }

    override fun command() = slashCommand(I18nKeysData.Commands.Command.Loritta.Label, TodoFixThisData, CommandCategory.DISCORD) {
        subcommand(I18nKeysData.Commands.Command.Loritta.Info.Label, I18nKeysData.Commands.Command.Loritta.Info.Description) {
            executor = LorittaInfoExecutor()
        }
    }

    inner class LorittaInfoExecutor : LorittaSlashCommandExecutor() {
        override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
            context.deferChannelMessage()

            val since = Instant.now()
                .minusSeconds(86400)
                .toKotlinInstant()

            val guildCount = context.loritta.lorittaShards.getGuildCount()
            val executedApplicationCommands = context.loritta.pudding.executedInteractionsLog.getExecutedApplicationCommands(since)
            val uniqueUsersExecutedApplicationCommands = context.loritta.pudding.executedInteractionsLog.getUniqueUsersExecutedApplicationCommands(since)

            context.reply {
                embed {
                    color = LorittaColors.LorittaAqua.rgb

                    author(context.i18nContext.get(I18N_PREFIX.Info.Embed.Title))

                    description = context.i18nContext.get(
                        I18N_PREFIX.Info.Embed.Description(
                            guildCount = guildCount,
                            commandCount = context.loritta.getCommandCount(),
                            executedApplicationCommands = executedApplicationCommands,
                            uniqueUsersExecutedApplicationCommands = uniqueUsersExecutedApplicationCommands,
                            userMention = context.user.asMention,
                            loriSunglasses = Emotes.LoriSunglasses,
                            loriYay = Emotes.LoriYay,
                            loriKiss = Emotes.LoriKiss,
                            loriHeart = Emotes.LoriHeart
                        )
                    ).joinToString("\n")

                    image = "${context.loritta.config.loritta.website.url}v3/assets/img/sonhos/lori-space.gif"

                    footer(context.i18nContext.get(LorittaCommand.I18N_PREFIX.Info.Embed.Footer("MrPowerGamerBR#4185", "https://mrpowergamerbr.com")), "https://mrpowergamerbr.com/assets/img/avatar.png")
                }

                actionRow(
                    linkButton(
                        GACampaigns.createUrlWithCampaign(
                            "https://loritta.website",
                            "discord",
                            "loritta-info",
                            "loritta-info-links",
                            "home-page"
                        ).toString(),
                        context.i18nContext.get(LorittaCommand.I18N_PREFIX.Info.Website),
                        Emotes.LoriSunglasses
                    ),
                    linkButton(
                        GACampaigns.createUrlWithCampaign(
                            "https://loritta.website/dashboard",
                            "discord",
                            "loritta-info",
                            "loritta-info-links",
                            "dashboard"
                        ).toString(),
                        context.i18nContext.get(LorittaCommand.I18N_PREFIX.Info.Dashboard),
                        Emotes.LoriReading
                    ),
                    linkButton(
                        GACampaigns.createUrlWithCampaign(
                            "https://loritta.website/commands",
                            "discord",
                            "loritta-info",
                            "loritta-info-links",
                            "commands"
                        ).toString(),
                        context.i18nContext.get(LorittaCommand.I18N_PREFIX.Info.Commands),
                        Emotes.LoriWow
                    ),
                    linkButton(
                        GACampaigns.createUrlWithCampaign(
                            "https://loritta.website/support",
                            "discord",
                            "loritta-info",
                            "loritta-info-links",
                            "support"
                        ).toString(),
                        context.i18nContext.get(LorittaCommand.I18N_PREFIX.Info.Support),
                        Emotes.LoriHm
                    ),
                    linkButton(
                        GACampaigns.createUrlWithCampaign(
                            "https://loritta.website/donate",
                            "discord",
                            "loritta-info",
                            "loritta-info-links",
                            "premium"
                        ).toString(),
                        context.i18nContext.get(LorittaCommand.I18N_PREFIX.Info.Premium),
                        Emotes.LoriCard
                    )
                )

                actionRow(
                    linkButton(
                        "https://twitter.com/LorittaBot",
                        "Twitter",
                        Emotes.Twitter
                    ),
                    linkButton(
                        "https://twitter.com/LorittaBot",
                        "Twitter",
                        Emotes.Twitter
                    ),
                    linkButton(
                        "https://instagram.com/lorittabot",
                        "Instagram",
                        Emotes.Instagram
                    ),
                    linkButton(
                        "https://youtube.com/c/Loritta",
                        "YouTube",
                        Emotes.YouTube
                    ),
                    linkButton(
                        "https://www.tiktok.com/@lorittamorenittabot",
                        "TikTok",
                        Emotes.TikTok
                    )
                )

                actionRow(
                    linkButton(
                        "https://github.com/LorittaBot",
                        "GitHub",
                        Emotes.GitHub
                    )
                )
            }
        }
    }
}