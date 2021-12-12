package net.perfectdreams.loritta.cinnamon.platform.commands.moderation.ban

import dev.kord.common.entity.ButtonStyle
import dev.kord.common.entity.DiscordPartialEmoji
import dev.kord.rest.service.RestClient
import net.perfectdreams.discordinteraktions.api.entities.User
import net.perfectdreams.discordinteraktions.common.builder.message.actionRow
import net.perfectdreams.loritta.cinnamon.common.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.platform.commands.ComponentExecutorIds
import net.perfectdreams.loritta.cinnamon.platform.commands.styled
import net.perfectdreams.loritta.cinnamon.platform.components.ComponentContext
import net.perfectdreams.loritta.cinnamon.platform.components.GuildComponentContext
import net.perfectdreams.loritta.cinnamon.platform.components.buttons.ButtonClickExecutor
import net.perfectdreams.loritta.cinnamon.platform.components.buttons.ButtonClickExecutorDeclaration

class UnbanButtonClickExecutor(val rest: RestClient) : ButtonClickExecutor {
    companion object : ButtonClickExecutorDeclaration(
        UnbanButtonClickExecutor::class,
        ComponentExecutorIds.UNBAN_BUTTON_EXECUTOR
    )

    override suspend fun onClick(user: User, context: ComponentContext, data: String) {
        val (authorId, bannedUserId) = context.decodeViaComponentDataUtilsAndRequireUserToMatch<UnbanData>(data)

        context.deferUpdateMessage()
        context.updateMessage {
            components = mutableListOf()

            actionRow {
                interactionButton(ButtonStyle.Secondary, "####") {
                    emoji = DiscordPartialEmoji(name = Emotes.HammerPick.name)
                    disabled = true
                }
            }
        }

        // TODO: Check if the user is allowed to unban this user
        // TODO: Check if me is allowed to unban this user

        rest.guild.deleteGuildBan((context as GuildComponentContext).guildId, bannedUserId)

        context.sendMessage {
            styled(
                context.i18nContext.get(I18nKeysData.Punishment.SuccessfullyUnbanned),
                Emotes.Tada
            )
        }
    }
}