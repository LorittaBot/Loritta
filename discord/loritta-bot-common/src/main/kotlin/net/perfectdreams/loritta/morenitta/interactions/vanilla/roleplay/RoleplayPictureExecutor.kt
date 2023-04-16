package net.perfectdreams.loritta.morenitta.interactions.vanilla.roleplay

import kotlinx.coroutines.delay
import net.perfectdreams.loritta.cinnamon.discord.utils.AchievementUtils
import net.perfectdreams.loritta.cinnamon.discord.utils.toLong
import net.perfectdreams.loritta.cinnamon.pudding.data.UserId
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.interactions.commands.ApplicationCommandContext
import net.perfectdreams.loritta.morenitta.interactions.commands.LorittaSlashCommandExecutor
import net.perfectdreams.loritta.morenitta.interactions.commands.SlashCommandArguments
import net.perfectdreams.loritta.morenitta.interactions.commands.options.ApplicationCommandOptions
import net.perfectdreams.randomroleplaypictures.client.RandomRoleplayPicturesClient

abstract class RoleplayPictureExecutor(
    loritta: LorittaBot,
    private val client: RandomRoleplayPicturesClient,
    private val attributes: RoleplayActionAttributes
) : LorittaSlashCommandExecutor() {
    inner class Options : ApplicationCommandOptions() {
        val user = user("user", attributes.userI18nDescription)
    }

    override val options = Options()

    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
        context.deferChannelMessage(false)

        val receiver = args[options.user]

        val (achievementTargets, message) = RoleplayUtils.handleRoleplayMessage(
            context.loritta,
            context.i18nContext,
            RetributeRoleplayData(
                context.user.idLong,
                context.user.idLong,
                receiver.user.idLong,
                1
            ),
            client,
            attributes
        )

        context.reply(false) {
            message()
        }

        for ((achievementReceiver, achievement) in achievementTargets) {
            if (context.user.idLong == achievementReceiver)
                context.giveAchievementAndNotify(achievement)
            else
                AchievementUtils.giveAchievementToUser(context.loritta, UserId(achievementReceiver), achievement)
        }

        // Easter Egg: Small chance for Loritta to retribute the action (1%)
        val shouldLorittaRetribute = receiver.user.idLong == context.loritta.config.loritta.discord.applicationId.toLong() && attributes in RoleplayUtils.RETRIBUTABLE_ACTIONS_BY_LORITTA_EASTER_EGG && context.loritta.random.nextInt(0, 100) == 0

        if (shouldLorittaRetribute) {
            // Wait 5s just so it feels more "natural"
            delay(5_000)

            // We don't care about achievements, because none of the actions that Loritta do *should* trigger a achievement
            val (_, lorittaMessage) = RoleplayUtils.handleRoleplayMessage(
                context.loritta,
                context.i18nContext,
                RetributeRoleplayData(
                    context.user.idLong, // This doesn't really matter because it will be changed in the handleRoleplayMessage
                    receiver.user.idLong,
                    context.user.idLong,
                    2 // Increase the combo count
                ),
                client,
                attributes
            )

            context.reply(false) {
                lorittaMessage()
            }
        }
    }
}