package net.perfectdreams.loritta.cinnamon.platform.commands.roleplay

import kotlinx.coroutines.delay
import net.perfectdreams.discordinteraktions.common.entities.User
import net.perfectdreams.loritta.cinnamon.platform.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.options.CommandOption
import net.perfectdreams.loritta.cinnamon.platform.commands.options.SlashCommandArguments
import net.perfectdreams.loritta.cinnamon.platform.commands.roleplay.retribute.RetributeRoleplayData
import net.perfectdreams.loritta.cinnamon.platform.utils.AchievementUtils
import net.perfectdreams.loritta.cinnamon.platform.utils.UserId
import net.perfectdreams.randomroleplaypictures.client.RandomRoleplayPicturesClient

abstract class RoleplayPictureExecutor(
    private val client: RandomRoleplayPicturesClient,
    private val attributes: RoleplayActionAttributes
) : SlashCommandExecutor() {
    abstract val userOption: CommandOption<User>

    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
        context.deferChannelMessage()

        val receiver = args[userOption]

        val (achievementTargets, message) = RoleplayUtils.handleRoleplayMessage(
            context.loritta,
            context.i18nContext,
            RetributeRoleplayData(
                context.user.id,
                context.user.id,
                receiver.id,
                1
            ),
            client,
            attributes
        )

        context.sendMessage(message)

        for ((achievementReceiver, achievement) in achievementTargets) {
            if (context.user.id == achievementReceiver)
                context.giveAchievementAndNotify(achievement)
            else
                AchievementUtils.giveAchievementToUser(context.loritta, UserId(achievementReceiver), achievement)
        }

        // Easter Egg: Small chance for Loritta to retribute the action (1%)
        val shouldLorittaRetribute = receiver.id.value.toLong() == context.loritta.discordConfig.applicationId && attributes in RoleplayUtils.RETRIBUTABLE_ACTIONS_BY_LORITTA_EASTER_EGG && context.loritta.random.nextInt(0, 100) == 0

        if (shouldLorittaRetribute) {
            // Wait 5s just so it feels more "natural"
            delay(5_000)

            // We don't care about achievements, because none of the actions that Loritta do *should* trigger a achievement
            val (_, lorittaMessage) = RoleplayUtils.handleRoleplayMessage(
                context.loritta,
                context.i18nContext,
                RetributeRoleplayData(
                    context.user.id, // This doesn't really matter because it will be changed in the handleRoleplayMessage
                    receiver.id,
                    context.user.id,
                    2 // Increase the combo count
                ),
                client,
                attributes
            )

            context.sendMessage(lorittaMessage)
        }
    }
}