package net.perfectdreams.loritta.morenitta.interactions.vanilla.social

import dev.minn.jda.ktx.interactions.components.option
import dev.minn.jda.ktx.messages.InlineMessage
import dev.minn.jda.ktx.messages.MessageEdit
import net.dv8tion.jda.api.entities.User
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.pudding.entities.PuddingAchievement
import net.perfectdreams.loritta.common.achievements.AchievementCategory
import net.perfectdreams.loritta.common.achievements.AchievementType
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.utils.LorittaColors
import net.perfectdreams.loritta.common.utils.text.TextUtils.shortenWithEllipsis
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.commands.LorittaSlashCommandExecutor
import net.perfectdreams.loritta.morenitta.interactions.commands.SlashCommandArguments
import net.perfectdreams.loritta.morenitta.interactions.commands.SlashCommandDeclarationWrapper
import net.perfectdreams.loritta.morenitta.interactions.commands.slashCommand
import net.perfectdreams.loritta.morenitta.utils.extensions.await
import net.perfectdreams.loritta.morenitta.utils.extensions.toJDA
import net.perfectdreams.loritta.serializable.UserId
import java.util.*

class AchievementsCommand(private val loritta: LorittaBot) : SlashCommandDeclarationWrapper {
    companion object {
        private val I18N_PREFIX = I18nKeysData.Commands.Command.Achievements
    }

    override fun command() = slashCommand(I18N_PREFIX.Label, I18N_PREFIX.Description, CommandCategory.SOCIAL, UUID.fromString("68ab22f2-fe44-4394-bd34-93665c9b1980")) {
        executor = AchievementsExecutor()
    }

    class AchievementsExecutor : LorittaSlashCommandExecutor() {
        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            val achievements = context.loritta.pudding.users.getUserAchievements(UserId(context.user.idLong))

            context.reply(false) {
                apply(
                    createMessage(
                        context.loritta,
                        context.user,
                        context.i18nContext,
                        achievements,
                        null
                    )
                )
            }
        }

        fun createMessage(
            loritta: LorittaBot,
            user: User,
            i18nContext: I18nContext,
            achievements: List<PuddingAchievement>,
            category: AchievementCategory?
        ): InlineMessage<*>.() -> (Unit) {
            val userAchievementsInAllCategoriesCount = achievements.size
            val totalAchievementsInAllCategoriesCount = AchievementType.values().size
            val userAchievementsInCurrentCategoryCount: Int
            val totalAchievementsInCurrentCategoryCount: Int
            val achievementsOfTheCurrentCategory: List<PuddingAchievement>

            if (category == null) {
                userAchievementsInCurrentCategoryCount = userAchievementsInAllCategoriesCount
                totalAchievementsInCurrentCategoryCount = totalAchievementsInAllCategoriesCount
                achievementsOfTheCurrentCategory = achievements
            } else {
                userAchievementsInCurrentCategoryCount = achievements.count { it.type.category == category }
                totalAchievementsInCurrentCategoryCount = AchievementType.values().count { it.category == category }
                achievementsOfTheCurrentCategory = achievements.filter { it.type.category == category }
            }

            return {
                embed {
                    val description = StringBuilder()

                    if (category == null)
                        title =
                            "${Emotes.Sparkles} ${i18nContext.get(I18nKeysData.Achievements.Category.All.Title)} [$userAchievementsInCurrentCategoryCount/$totalAchievementsInCurrentCategoryCount]"
                    else {
                        title =
                            "${category.emote} ${i18nContext.get(category.title)} [$userAchievementsInCurrentCategoryCount/$totalAchievementsInCurrentCategoryCount]"
                        description.append(i18nContext.get(category.description))
                        // We add new lines here because of the "You don't have any achievements..." text down below
                        description.append("\n\n")
                    }

                    if (achievementsOfTheCurrentCategory.isEmpty()) {
                        description.append(
                            i18nContext.get(
                                if (category == null)
                                    I18N_PREFIX.YouDontHaveAnyAchievements
                                else
                                    I18N_PREFIX.YouDontHaveAnyAchievementsInTheCategory
                            )
                        )
                    } else {
                        // Sort the achievements by when they were achieved, so more recent achievements -> older achievements
                        for (achievement in achievementsOfTheCurrentCategory.sortedByDescending { it.achievedAt }) {
                            field(
                                "${achievement.type.category.emote} ${i18nContext.get(achievement.type.title)}",
                                """${i18nContext.get(achievement.type.description)}
                                    **${i18nContext.get(I18N_PREFIX.AchievedAt)}:** <t:${achievement.achievedAt.epochSeconds}:f>
                                    """,
                                true
                            )
                        }
                    }

                    this.description = description.toString()
                    this.color = (category?.color ?: LorittaColors.LorittaAqua).rgb
                }

                actionRow(
                    loritta.interactivityManager.stringSelectMenuForUser(
                        user,
                        {
                            fun insertOption(optionCategory: AchievementCategory) {
                                val userAchievementsInCategoryCount =
                                    achievements.count { it.type.category == optionCategory }
                                val totalAchievementsInCategoryCount = AchievementType.values()
                                    .count { it.category == optionCategory }

                                this.option(
                                    "${i18nContext.get(optionCategory.title)} [$userAchievementsInCategoryCount/$totalAchievementsInCategoryCount]",
                                    optionCategory.name,
                                    i18nContext.get(optionCategory.description)
                                        .shortenWithEllipsis(100),
                                    emoji = optionCategory.emote.toJDA(),
                                    default = category == optionCategory, // Set as default if we are currently looking at this category
                                )
                            }

                            this.option(
                                "${i18nContext.get(I18nKeysData.Achievements.Category.All.Title)} [$userAchievementsInAllCategoriesCount/$totalAchievementsInAllCategoriesCount]",
                                "ALL",
                                emoji = Emotes.Sparkles.toJDA(),
                                default = category == null, // Set "ALL" as the default if the category is null (which means... "all categories")
                            )

                            AchievementCategory.values().forEach {
                                insertOption(it)
                            }
                        }
                    ) { context, values ->
                        val hook = context.deferEdit()

                        val newCategory = values.first()
                        val achievements = loritta.pudding.users.getUserAchievements(UserId(user.idLong))

                        hook.editOriginal(
                            MessageEdit {
                                apply(
                                    createMessage(
                                        loritta,
                                        user,
                                        context.i18nContext,
                                        achievements,
                                        try {
                                            AchievementCategory.valueOf(newCategory)
                                        } catch (e: IllegalArgumentException) {
                                            if (newCategory == "ALL") // null = All categories
                                                null
                                            else
                                                throw e
                                        }
                                    )
                                )
                            }
                        ).await()
                    }
                )
            }
        }
    }
}