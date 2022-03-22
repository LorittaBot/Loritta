package net.perfectdreams.loritta.cinnamon.platform.commands.discord

import kotlinx.datetime.toKotlinInstant
import net.perfectdreams.discordinteraktions.common.builder.message.actionRow
import net.perfectdreams.discordinteraktions.common.builder.message.embed
import net.perfectdreams.discordinteraktions.common.utils.author
import net.perfectdreams.discordinteraktions.common.utils.footer
import net.perfectdreams.loritta.cinnamon.common.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.common.utils.GACampaigns
import net.perfectdreams.loritta.cinnamon.common.utils.LorittaColors
import net.perfectdreams.loritta.cinnamon.platform.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.platform.commands.discord.declarations.LorittaCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.options.SlashCommandArguments
import net.perfectdreams.loritta.cinnamon.platform.components.loriEmoji
import net.perfectdreams.loritta.cinnamon.platform.utils.toKordColor
import java.time.Instant

class LorittaInfoExecutor : SlashCommandExecutor() {
    companion object : SlashCommandExecutorDeclaration(LorittaInfoExecutor::class)

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
                        loriSunglasses = Emotes.LoriSunglasses,
                        loriYay = Emotes.LoriYay,
                        loriKiss = Emotes.LoriKiss,
                        loriHeart = Emotes.LoriHeart
                    )
                ).joinToString("\n")

                image = "https://cdn.discordapp.com/attachments/297732013006389252/955575384852799548/lori_voando_no_espaco.png"

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
                    loriEmoji = Emotes.LoriSunglasses
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
                    loriEmoji = Emotes.LoriReading
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
                    loriEmoji = Emotes.LoriWow
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
                    loriEmoji = Emotes.LoriHm
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
                    loriEmoji = Emotes.LoriRich
                }
            }

            actionRow {
                linkButton("https://twitter.com/LorittaBot") {
                    label = "Twitter"
                    loriEmoji = Emotes.Twitter
                }

                linkButton("https://instagram.com/lorittabot") {
                    label = "Instagram"
                    loriEmoji = Emotes.Instagram
                }

                linkButton("https://youtube.com/c/Loritta") {
                    label = "YouTube"
                    loriEmoji = Emotes.YouTube
                }

                linkButton("https://github.com/LorittaBot") {
                    label = "GitHub"
                    loriEmoji = Emotes.GitHub
                }
            }
        }
    }
}