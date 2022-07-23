package net.perfectdreams.loritta.cinnamon.platform.commands.utils.packtracker

import net.perfectdreams.discordinteraktions.common.builder.message.MessageBuilder
import net.perfectdreams.discordinteraktions.common.builder.message.actionRow
import net.perfectdreams.discordinteraktions.common.builder.message.embed
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.common.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.common.utils.LorittaColors
import net.perfectdreams.loritta.cinnamon.platform.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.platform.commands.options.SlashCommandArguments
import net.perfectdreams.loritta.cinnamon.platform.commands.utils.declarations.PackageCommand
import net.perfectdreams.loritta.cinnamon.platform.components.loriEmoji
import net.perfectdreams.loritta.cinnamon.platform.components.selectMenu
import net.perfectdreams.loritta.cinnamon.platform.utils.correios.CorreiosClient
import net.perfectdreams.loritta.cinnamon.platform.utils.toKordColor
import net.perfectdreams.loritta.cinnamon.pudding.data.UserId

class PackageListExecutor(val client: CorreiosClient) : SlashCommandExecutor() {
    companion object : SlashCommandExecutorDeclaration() {
        fun createMessage(i18nContext: I18nContext, trackingIds: List<String>): suspend MessageBuilder.() -> (Unit) = {
            embed {
                title = i18nContext.get(PackageCommand.I18N_PREFIX.List.FollowedPackages)
                description = i18nContext.get(PackageCommand.I18N_PREFIX.List.YouAreFollowingXPackages(trackingIds.size))
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