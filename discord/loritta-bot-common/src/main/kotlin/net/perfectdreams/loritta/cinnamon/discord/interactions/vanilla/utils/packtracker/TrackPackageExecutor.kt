package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.utils.packtracker

import dev.kord.common.entity.ButtonStyle
import dev.kord.common.entity.Snowflake
import net.perfectdreams.discordinteraktions.common.builder.message.MessageBuilder
import net.perfectdreams.discordinteraktions.common.builder.message.actionRow
import net.perfectdreams.discordinteraktions.common.builder.message.embed
import net.perfectdreams.discordinteraktions.common.utils.field
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.common.utils.LorittaColors
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.CinnamonSlashCommandExecutor
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.options.LocalizedApplicationCommandOptions
import net.perfectdreams.discordinteraktions.common.commands.options.SlashCommandArguments
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.utils.declarations.PackageCommand
import net.perfectdreams.loritta.cinnamon.discord.interactions.components.interactiveButton
import net.perfectdreams.loritta.cinnamon.discord.utils.ComponentDataUtils
import net.perfectdreams.loritta.cinnamon.discord.utils.CorreiosUtils
import net.perfectdreams.loritta.cinnamon.discord.utils.DiscordResourceLimits
import net.perfectdreams.loritta.cinnamon.discord.utils.correios.CorreiosClient
import net.perfectdreams.loritta.cinnamon.discord.utils.correios.entities.CorreiosFoundObjeto
import net.perfectdreams.loritta.cinnamon.discord.utils.correios.entities.CorreiosUnknownObjeto
import net.perfectdreams.loritta.cinnamon.discord.utils.correios.entities.eventTypeWithStatus
import net.perfectdreams.loritta.cinnamon.discord.utils.correios.exceptions.InvalidTrackingIdException
import net.perfectdreams.loritta.cinnamon.discord.utils.toKordColor
import net.perfectdreams.loritta.cinnamon.pudding.data.UserId

class TrackPackageExecutor(loritta: LorittaBot, val client: CorreiosClient) : CinnamonSlashCommandExecutor(loritta) {
    companion object {
        fun createMessage(
            i18nContext: I18nContext,
            userId: Snowflake,
            trackingId: String,
            obj: CorreiosFoundObjeto,
            isReceivingNotificationsAboutPackage: Boolean
        ): suspend MessageBuilder.() -> (Unit) = {
            val packageEvents = obj.events.take(DiscordResourceLimits.Embed.FieldsPerEmbed)
            val wouldHaveOverflown = obj.events.size > DiscordResourceLimits.Embed.FieldsPerEmbed

            embed {
                title = "${Emotes.Correios} `${trackingId}` (${obj.nome})"
                if (wouldHaveOverflown)
                    description = i18nContext.get(PackageCommand.I18N_PREFIX.Track.TooManyEventsFiltered(DiscordResourceLimits.Embed.FieldsPerEmbed))

                for (event in packageEvents) {
                    val eventTypeWithStatus = event.eventTypeWithStatus

                    field(
                        "${CorreiosUtils.getEmoji(eventTypeWithStatus)} ${event.descricao}",
                        CorreiosUtils.formatEvent(event),
                        false
                    )
                }

                actionRow {
                    if (isReceivingNotificationsAboutPackage) {
                        interactiveButton(
                            ButtonStyle.Primary,
                            i18nContext.get(PackageCommand.I18N_PREFIX.Track.UnfollowPackageUpdates),
                            UnfollowPackageButtonClickExecutor,
                            ComponentDataUtils.encode(
                                UnfollowPackageData(
                                    userId,
                                    trackingId
                                )
                            )
                        )
                    } else {
                        interactiveButton(
                            ButtonStyle.Primary,
                            i18nContext.get(PackageCommand.I18N_PREFIX.Track.FollowPackageUpdates),
                            FollowPackageButtonClickExecutor,
                            ComponentDataUtils.encode(
                                FollowPackageData(
                                    userId,
                                    trackingId
                                )
                            )
                        )
                    }
                }

                color = LorittaColors.CorreiosYellow.toKordColor()
            }
        }
    }

    inner class Options : LocalizedApplicationCommandOptions(loritta) {
        val trackingId = string("tracking_id", PackageCommand.I18N_PREFIX.Track.Options.TrackingId.Text)
    }

    override val options = Options()

    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
        context.deferChannelMessageEphemerally()

        val packageId = args[options.trackingId].uppercase()

        val correiosResponse = try {
            client.getPackageInfo(packageId)
        } catch (e: InvalidTrackingIdException) {
            context.failEphemerally(
                context.i18nContext.get(PackageCommand.I18N_PREFIX.InvalidCorreiosTrackingId("LO400218922RI")), // While there is a "AA123456785BR" package ID example in Correios website, that's actually a (very buggy) package!
                Emotes.LoriSob
            )
        }

        val obj = correiosResponse.objeto.firstOrNull()

        if (obj is CorreiosUnknownObjeto)
            context.failEphemerally(
                context.i18nContext.get(PackageCommand.I18N_PREFIX.ObjectNotFoundCorreios),
                Emotes.LoriSob
            )

        if (obj is CorreiosFoundObjeto) {
            val message = createMessage(
                context.i18nContext,
                context.user.id,
                packageId,
                obj,
                packageId in context.loritta.pudding.packagesTracking.getTrackedCorreiosPackagesByUser(UserId(context.user.id.value))
            )

            context.sendEphemeralMessage {
                message()
            }
        }
    }
}