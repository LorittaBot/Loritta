package net.perfectdreams.loritta.morenitta.interactions

import dev.minn.jda.ktx.messages.InlineMessage
import dev.minn.jda.ktx.messages.MessageCreateBuilder
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.interactions.InteractionHook
import net.dv8tion.jda.api.utils.messages.MessageCreateData
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.common.achievements.AchievementType
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.utils.extensions.await
import net.perfectdreams.loritta.serializable.UserId

/**
 * This is an interaction context for the interaction token.
 *
 * @param jda JDA instance
 * @param token Interaction token
 */
class BarebonesInteractionContext(
    val jda: JDA,
    val token: String,
) {
    val hook = InteractionHook.from(jda, token)

    /**
     * Sends a message with the interaction token.
     */
    suspend fun reply(ephemeral: Boolean, block: suspend InlineMessage<MessageCreateData>.() -> Unit): Message? {
        val builtMessage = MessageCreateBuilder {
            block.invoke(this)
        }.build()

        return hook.setEphemeral(ephemeral)
            .sendMessage(builtMessage)
            .await()
    }

    /**
     * Gives an achievement to the user.
     */
    suspend fun giveAchievementToUser(
        loritta: LorittaBot,
        userId: UserId,
        i18nContext: I18nContext,
        type: AchievementType,
        achievedAt: Instant = Clock.System.now()
    ) {
        val profile = loritta.pudding.users.getOrCreateUserProfile(userId)
        val wasAchievementGiven = profile.giveAchievement(
            type,
            achievedAt
        )

        if (wasAchievementGiven)
            reply(true) {
                styled(
                    content = "**${i18nContext.get(I18nKeysData.Achievements.AchievementUnlocked)}**",
                    prefix = Emotes.Sparkles
                )

                styled(
                    "**${i18nContext.get(type.title)}:** ${i18nContext.get(type.description)}",
                    prefix = type.category.emote
                )

                styled(
                    i18nContext.get(I18nKeysData.Achievements.ViewYourAchievements(loritta.commandMentions.achievements)),
                    prefix = Emotes.LoriWow
                )
            }
    }
}