package net.perfectdreams.loritta.cinnamon.platform.commands.moderation

import dev.kord.common.Color
import dev.kord.common.entity.ButtonStyle
import dev.kord.common.entity.DiscordPartialEmoji
import dev.kord.rest.service.RestClient
import net.perfectdreams.discordinteraktions.common.builder.message.actionRow
import net.perfectdreams.discordinteraktions.common.builder.message.embed
import net.perfectdreams.discordinteraktions.common.utils.thumbnailUrl
import net.perfectdreams.discordinteraktions.platforms.kord.entities.KordUser
import net.perfectdreams.loritta.cinnamon.common.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.platform.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandArguments
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.GuildApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.platform.commands.declarations.CommandExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.platform.commands.moderation.ban.UnbanButtonClickExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.moderation.ban.UnbanData
import net.perfectdreams.loritta.cinnamon.platform.commands.moderation.declarations.BanCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.options.CommandOptions
import net.perfectdreams.loritta.cinnamon.platform.commands.styled
import net.perfectdreams.loritta.cinnamon.platform.components.interactiveButton
import net.perfectdreams.loritta.cinnamon.platform.utils.ComponentDataUtils

class BanInfoExecutor(
    val rest: RestClient
) : CommandExecutor() {
    companion object : CommandExecutorDeclaration(BanInfoExecutor::class) {
        object Options : CommandOptions() {
            val user = user("user", BanCommand.I18N_PREFIX.BanInfo.Options.User)
                .register()
        }

        override val options = Options
    }

    override suspend fun execute(context: ApplicationCommandContext, args: CommandArguments) {
        if (context !is GuildApplicationCommandContext)
            context.fail {
                content = context.i18nContext.get(I18nKeysData.Commands.CommandOnlyAvailableInGuilds)
            }

        val user = args[Options.user]

        context.deferChannelMessage()

        val guildBans = rest.guild.getGuildBans(context.guildId)
        val banInformation = guildBans.find { it.user.id == user.id }
            ?: context.fail {
                styled(
                    context.i18nContext.get(BanCommand.I18N_PREFIX.BanInfo.BanDoesNotExist),
                    Emotes.Error
                )
            }

        val bannedUser = KordUser(banInformation.user)

        context.sendMessage {
            embed {
                title = "${Emotes.LoriCoffee} " + context.i18nContext.get(BanCommand.I18N_PREFIX.BanInfo.Title)
                thumbnailUrl = bannedUser.avatar.url

                color = Color(114, 137, 218) // TODO: Move this to an object

                field {
                    name = "${Emotes.LoriTemmie} " + context.i18nContext.get(BanCommand.I18N_PREFIX.BanInfo.User)
                    value = "${bannedUser.name}#${bannedUser.discriminator} (`${bannedUser.id}`)"
                }

                field {
                    name = "${Emotes.LoriBanHammer} " + context.i18nContext.get(BanCommand.I18N_PREFIX.BanInfo.Reason)
                    value = banInformation.reason ?: context.i18nContext.get(
                        I18nKeysData.Punishment.NoReasonSpecified
                    )
                }

                footer {
                    text = context.i18nContext.get(BanCommand.I18N_PREFIX.BanInfo.UnbanUser(Emotes.HammerPick))
                }
            }

            actionRow {
                interactiveButton(
                    ButtonStyle.Secondary,
                    UnbanButtonClickExecutor,
                    ComponentDataUtils.encode(
                        UnbanData(
                            context.user.id,
                            bannedUser.id
                        )
                    )
                ) {
                    emoji = DiscordPartialEmoji(name = Emotes.HammerPick.name)
                }
            }
        }
    }
}