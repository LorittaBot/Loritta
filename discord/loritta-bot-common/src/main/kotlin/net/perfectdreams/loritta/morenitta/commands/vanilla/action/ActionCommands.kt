package net.perfectdreams.loritta.morenitta.commands.vanilla.action

import dev.minn.jda.ktx.messages.MessageCreateBuilder
import kotlinx.coroutines.delay
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.UserSnowflake
import net.perfectdreams.loritta.cinnamon.discord.utils.AchievementUtils
import net.perfectdreams.loritta.cinnamon.discord.utils.toLong
import net.perfectdreams.loritta.cinnamon.pudding.data.UserId
import net.perfectdreams.loritta.common.commands.ArgumentType
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.api.commands.Command
import net.perfectdreams.loritta.morenitta.api.commands.CommandContext
import net.perfectdreams.loritta.morenitta.interactions.CommandContextCompat
import net.perfectdreams.loritta.morenitta.interactions.vanilla.roleplay.RetributeRoleplayData
import net.perfectdreams.loritta.morenitta.interactions.vanilla.roleplay.RoleplayActionAttributes
import net.perfectdreams.loritta.morenitta.interactions.vanilla.roleplay.RoleplayUtils
import net.perfectdreams.loritta.morenitta.platform.discord.legacy.commands.DiscordAbstractCommandBase
import net.perfectdreams.loritta.morenitta.utils.OutdatedCommandUtils
import java.awt.Color

typealias ActionCommandScope = ActionCommandDSL.() -> Unit
typealias ActionCommandContext = (BaseLocale, User, User) -> String

abstract class ActionCommand(val attributes: RoleplayActionAttributes, loritta: LorittaBot, labels: List<String>): DiscordAbstractCommandBase(loritta, labels, CommandCategory.ROLEPLAY) {
    abstract fun create(): ActionCommandDSL

    override fun command(): Command<CommandContext> = create {
        create().also {
            localizedDescription(it.description)
            localizedExamples("commands.category.action.examples")

            usage {
                argument(ArgumentType.USER) {
                    optional = false
                }
            }

            executesDiscord {
                if (args.isEmpty()) return@executesDiscord explain()

                val receiver = userOrFail(0)

                OutdatedCommandUtils.sendOutdatedCommandMessage(this, this.locale, "roleplay ${labels.first()}")
                
                // This is provided for "retro compatibility" with the original slash only implementation
                // Because a lot of people *still* use "+kiss" instead of "/roleplay kiss"
                // The following code is a "conversion" of RoleplayPictureExecutor to work with the legacy command framework
                val (achievementTargets, message) = RoleplayUtils.handleRoleplayMessage(
                    loritta,
                    i18nContext,
                    RetributeRoleplayData(
                        user.idLong,
                        user.idLong,
                        receiver.id,
                        1
                    ),
                    loritta.randomRoleplayPicturesClient,
                    attributes
                )

                sendMessage(
                    MessageCreateBuilder {
                        message()
                    }.build()
                )

                for ((achievementReceiver, achievement) in achievementTargets) {
                    if (user.idLong == achievementReceiver)
                        AchievementUtils.giveAchievementToUserAndNotifyThem(loritta, CommandContextCompat.LegacyDiscordCommandContextCompat(this), i18nContext, UserSnowflake.fromId(achievementReceiver), achievement)
                    else
                        AchievementUtils.giveAchievementToUser(loritta, UserId(achievementReceiver), achievement)
                }

                // Easter Egg: Small chance for Loritta to retribute the action (1%)
                val shouldLorittaRetribute = receiver.id == loritta.config.loritta.discord.applicationId.toLong() && attributes in RoleplayUtils.RETRIBUTABLE_ACTIONS_BY_LORITTA_EASTER_EGG && loritta.random.nextInt(0, 100) == 0

                if (shouldLorittaRetribute) {
                    // Wait 5s just so it feels more "natural"
                    delay(5_000)

                    // We don't care about achievements, because none of the actions that Loritta do *should* trigger a achievement
                    val (_, lorittaMessage) = RoleplayUtils.handleRoleplayMessage(
                        loritta,
                        i18nContext,
                        RetributeRoleplayData(
                            user.idLong, // This doesn't really matter because it will be changed in the handleRoleplayMessage
                            receiver.id,
                            user.idLong,
                            2 // Increase the combo count
                        ),
                        loritta.randomRoleplayPicturesClient,
                        attributes
                    )

                    sendMessage(
                        MessageCreateBuilder {
                            lorittaMessage()
                        }.build()
                    )
                }
            }
        }
    }
}

class ActionCommandDSL(val command: ActionCommand) {

    var description: String = "commands.command.${command.labels.first()}.description"
    var folderName: String? = command.labels.first()

    lateinit var color: Color
    lateinit var emoji: String
    lateinit var response: ActionCommandContext

    fun response(block: ActionCommandContext) {
        this.response = block
    }
}

fun ActionCommand.action(block: ActionCommandScope) =
    ActionCommandDSL(this).apply(block)


