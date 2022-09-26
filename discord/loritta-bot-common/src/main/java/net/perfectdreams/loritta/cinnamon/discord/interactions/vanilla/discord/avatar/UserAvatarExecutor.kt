package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.discord.avatar

import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.Member
import dev.kord.core.entity.User
import kotlinx.datetime.Clock
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.GuildApplicationCommandContext
import kotlin.time.Duration.Companion.minutes

interface UserAvatarExecutor {
    suspend fun handleAvatarCommand(
        context: ApplicationCommandContext,
        lorittaId: Snowflake,
        user: User,
        member: Member?,
        isEphemeral: Boolean,
    ) {
        val now = Clock.System.now()

        val data = UserDataUtils.ViewingGlobalUserAvatarData(
            user.username,
            user.discriminator.toInt(),
            user.data.avatar,
            member?.memberData?.avatar?.value
        )

        val id = context.loritta.services.interactionsData.insertInteractionData(
            Json.encodeToJsonElement(
                data
            ).jsonObject,
            now,
            now + 15.minutes // Expires after 15m
        )

        val message = UserDataUtils.createAvatarPreviewMessage(
            context.loritta,
            context.i18nContext,
            lorittaId,
            UserDataUtils.SwitchAvatarInteractionIdData(
                context.user.id,
                user.id,
                (context as? GuildApplicationCommandContext)?.guildId,
                MessageTargetType.EDIT_MESSAGE,
                id
            ),
            data
        )

        if (isEphemeral)
            context.sendEphemeralMessage {
                message()
            }
        else
            context.sendMessage {
                message()
            }

        if (user.id == context.user.id)
            context.giveAchievementAndNotify(net.perfectdreams.loritta.cinnamon.achievements.AchievementType.IS_THAT_AN_UNDERTALE_REFERENCE)
    }
}