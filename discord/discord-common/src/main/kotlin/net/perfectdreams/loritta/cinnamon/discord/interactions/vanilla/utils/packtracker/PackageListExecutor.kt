package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.utils.packtracker

import net.perfectdreams.discordinteraktions.common.builder.message.MessageBuilder
import net.perfectdreams.discordinteraktions.common.builder.message.actionRow
import net.perfectdreams.discordinteraktions.common.builder.message.embed
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.utils.LorittaColors
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.CinnamonSlashCommandExecutor
import net.perfectdreams.loritta.cinnamon.discord.LorittaCinnamon
import net.perfectdreams.discordinteraktions.common.commands.options.SlashCommandArguments
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.utils.declarations.PackageCommand
import net.perfectdreams.loritta.cinnamon.discord.interactions.components.loriEmoji
import net.perfectdreams.loritta.cinnamon.discord.interactions.components.selectMenu
import net.perfectdreams.loritta.cinnamon.discord.utils.correios.CorreiosClient
import net.perfectdreams.loritta.cinnamon.discord.utils.toKordColor
import net.perfectdreams.loritta.cinnamon.pudding.data.UserId

class PackageListExecutor(loritta: LorittaCinnamon, val client: CorreiosClient) : CinnamonSlashCommandExecutor(loritta) {
    companion object {
        fun createMessage(i18nContext: I18nContext, trackingIds: List<String>): suspend MessageBuilder.() -> (Unit) = {
            embed {
                title = i18nContext.get(PackageCommand.I18N_PREFIX.List.FollowedPackages)
                description =
                    i18nContext.get(PackageCommand.I18N_PREFIX.List.YouAreFollowingXPackages(trackingIds.size))
                color = LorittaColors.CorreiosYellow.toKordColor()
            }

            actionRow {
                selectMenu(SelectPackageSelectMenuExecutor) {
                    for (packageId in trackingIds) {
                        option(packageId, packageId) {
                            loriEmoji = Emotes.Correios
                        }
                    }
                }
            }
        }
    }

    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
        context.deferChannelMessageEphemerally()

        val trackingIdsTrackedByUser = context.loritta.services.packagesTracking.getTrackedCorreiosPackagesByUser(UserId(context.user.id.value))

        if (trackingIdsTrackedByUser.isEmpty())
            context.failEphemerally(
                context.i18nContext.get(PackageCommand.I18N_PREFIX.List.YouAreNotFollowingAnyPackage),
                Emotes.LoriSob
            )

        val message = createMessage(context.i18nContext, trackingIdsTrackedByUser)

        context.sendEphemeralMessage {
            message()
        }
    }
}