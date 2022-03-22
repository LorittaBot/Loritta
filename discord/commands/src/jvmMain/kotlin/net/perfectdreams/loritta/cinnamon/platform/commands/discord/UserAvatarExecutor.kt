package net.perfectdreams.loritta.cinnamon.platform.commands.discord

import dev.kord.common.entity.Snowflake
import kotlinx.datetime.Clock
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import net.perfectdreams.loritta.cinnamon.common.achievements.AchievementType
import net.perfectdreams.loritta.cinnamon.platform.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.platform.commands.GuildApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.platform.commands.discord.declarations.UserCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.cinnamon.platform.commands.options.SlashCommandArguments
import kotlin.time.Duration.Companion.minutes

class UserAvatarExecutor(val lorittaId: Snowflake) : SlashCommandExecutor() {
    companion object : SlashCommandExecutorDeclaration(UserAvatarExecutor::class) {
        object Options : ApplicationCommandOptions() {
            val user = optionalUser("user", UserCommand.I18N_PREFIX.Avatar.Options.User)
                .register()
        }

        override val options = Options
    }

    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
        val user = args[Options.user] ?: context.user

        // TODO: Fix this workaround, it would be nice if Discord InteraKTions provided a "UserAndMember" object to us
        // (Or maybe expose it correctly?)
        val member = context.interaKTionsContext.data.resolved?.members?.get(user.id)

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
            context.giveAchievement(AchievementType.IS_THAT_AN_UNDERTALE_REFERENCE)
    }
}