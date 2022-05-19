package net.perfectdreams.loritta.cinnamon.platform.commands.discord.avatar

import dev.kord.common.entity.Snowflake
import kotlinx.datetime.Clock
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import net.perfectdreams.discordinteraktions.common.entities.Member
import net.perfectdreams.discordinteraktions.common.entities.User
import net.perfectdreams.loritta.cinnamon.common.achievements.AchievementType
import net.perfectdreams.loritta.cinnamon.platform.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.platform.commands.GuildApplicationCommandContext
import kotlin.time.Duration.Companion.minutes

interface UserAvatarExecutor {
    suspend fun handleAvatarCommand(
        context: ApplicationCommandContext,
        lorittaId: Snowflake,
        user: User,
        member: Member?
    ) {
        val now = Clock.System.now()

        val data = UserDataUtils.ViewingGlobalUserAvatarData(
            user.name,
            user.discriminator.toInt(),
            user.avatarHash,
            member?.avatarHash
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
                id
            ),
            data
        )

        context.sendMessage {
            message()
        }

        if (user.id == context.user.id)
            context.giveAchievementAndNotify(AchievementType.IS_THAT_AN_UNDERTALE_REFERENCE)
    }
}