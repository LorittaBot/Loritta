package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.social

import dev.kord.common.entity.Snowflake
import kotlinx.serialization.Serializable
import net.perfectdreams.discordinteraktions.common.builder.message.MessageBuilder
import net.perfectdreams.discordinteraktions.common.builder.message.actionRow
import net.perfectdreams.discordinteraktions.common.builder.message.embed
import net.perfectdreams.discordinteraktions.common.entities.User
import net.perfectdreams.discordinteraktions.common.utils.inlineField
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.achievements.AchievementCategory
import net.perfectdreams.loritta.cinnamon.achievements.AchievementType
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.utils.LorittaColors
import net.perfectdreams.loritta.cinnamon.utils.text.TextUtils.shortenWithEllipsis
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.discord.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.discord.utils.ComponentExecutorIds
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.CinnamonSlashCommandExecutor
import net.perfectdreams.discordinteraktions.common.commands.options.SlashCommandArguments
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.social.declarations.AchievementsCommand
import net.perfectdreams.loritta.cinnamon.discord.interactions.components.*
import net.perfectdreams.loritta.cinnamon.discord.interactions.components.data.SingleUserComponentData
import net.perfectdreams.loritta.cinnamon.discord.interactions.components.*
import net.perfectdreams.loritta.cinnamon.discord.utils.ComponentDataUtils
import net.perfectdreams.loritta.cinnamon.discord.utils.getUserAchievements
import net.perfectdreams.loritta.cinnamon.discord.utils.toKordColor
import net.perfectdreams.loritta.cinnamon.pudding.entities.PuddingAchievement

class AchievementsExecutor(loritta: LorittaCinnamon) : CinnamonSlashCommandExecutor(loritta) {
    companion object {
        suspend fun createMessage(
            loritta: LorittaCinnamon,
            user: User,
            i18nContext: I18nContext,
            achievements: List<PuddingAchievement>,
            category: net.perfectdreams.loritta.cinnamon.achievements.AchievementCategory?
        ): MessageBuilder.() -> (Unit) {
            val data = ChangeCategoryData(
                user.id,
                0,
                category
            )

            val userAchievementsInAllCategoriesCount = achievements.size
            val totalAchievementsInAllCategoriesCount = net.perfectdreams.loritta.cinnamon.achievements.AchievementType.values().size
            val userAchievementsInCurrentCategoryCount: Int
            val totalAchievementsInCurrentCategoryCount: Int
            val achievementsOfTheCurrentCategory: List<PuddingAchievement>

            if (category == null) {
                userAchievementsInCurrentCategoryCount = userAchievementsInAllCategoriesCount
                totalAchievementsInCurrentCategoryCount = totalAchievementsInAllCategoriesCount
                achievementsOfTheCurrentCategory = achievements
            } else {
                userAchievementsInCurrentCategoryCount = achievements.count { it.type.category == category }
                totalAchievementsInCurrentCategoryCount = net.perfectdreams.loritta.cinnamon.achievements.AchievementType.values().count { it.category == category }
                achievementsOfTheCurrentCategory = achievements.filter { it.type.category == category }
            }

            return {
                embed {
                    val description = StringBuilder()

                    if (category == null)
                        title = "${net.perfectdreams.loritta.cinnamon.emotes.Emotes.Sparkles} ${i18nContext.get(I18nKeysData.Achievements.Category.All.Title)} [$userAchievementsInCurrentCategoryCount/$totalAchievementsInCurrentCategoryCount]"
                    else {
                        title = "${category.emote} ${i18nContext.get(category.title)} [$userAchievementsInCurrentCategoryCount/$totalAchievementsInCurrentCategoryCount]"
                        description.append(i18nContext.get(category.description))
                        // We add new lines here because of the "You don't have any achievements..." text down below
                        description.append("\n\n")
                    }

                    if (achievementsOfTheCurrentCategory.isEmpty()) {
                        description.append(
                            i18nContext.get(
                                if (category == null)
                                    AchievementsCommand.I18N_PREFIX.YouDontHaveAnyAchievements
                                else
                                    AchievementsCommand.I18N_PREFIX.YouDontHaveAnyAchievementsInTheCategory
                            )
                        )
                    } else {
                        // Sort the achievements by when they were achieved, so more recent achievements -> older achievements
                        for (achievement in achievementsOfTheCurrentCategory.sortedByDescending { it.achievedAt }) {
                            inlineField(
                                "${achievement.type.category.emote} ${i18nContext.get(achievement.type.title)}",
                                """${i18nContext.get(achievement.type.description)}
                                    **${i18nContext.get(AchievementsCommand.I18N_PREFIX.AchievedAt)}:** <t:${achievement.achievedAt.epochSeconds}:f>
                                    """
                            )
                        }
                    }

                    this.description = description.toString()
                    this.color = (category?.color ?: LorittaColors.LorittaAqua).toKordColor()
                }

                actionRow {
                    selectMenu(
                        ChangeCategoryMenuExecutor,
                        ComponentDataUtils.encode(data)
                    ) {
                        fun insertOption(optionCategory: net.perfectdreams.loritta.cinnamon.achievements.AchievementCategory) {
                            val userAchievementsInCategoryCount = achievements.count { it.type.category == optionCategory }
                            val totalAchievementsInCategoryCount = net.perfectdreams.loritta.cinnamon.achievements.AchievementType.values()
                                .count { it.category == optionCategory }

                            this.option(
                                "${i18nContext.get(optionCategory.title)} [$userAchievementsInCategoryCount/$totalAchievementsInCategoryCount]",
                                optionCategory.name
                            ) {
                                if (category == optionCategory)
                                    default = true // Set as default if we are currently looking at this category

                                description = i18nContext.get(optionCategory.description)
                                    .shortenWithEllipsis(100)
                                loriEmoji = optionCategory.emote
                            }
                        }

                        this.option(
                            "${i18nContext.get(I18nKeysData.Achievements.Category.All.Title)} [$userAchievementsInAllCategoriesCount/$totalAchievementsInAllCategoriesCount]",
                            "ALL"
                        ) {
                            if (category == null)
                                default = true // Set "ALL" as the default if the category is null (which means... "all categories")

                            loriEmoji = net.perfectdreams.loritta.cinnamon.emotes.Emotes.Sparkles
                        }

                        net.perfectdreams.loritta.cinnamon.achievements.AchievementCategory.values().forEach {
                            insertOption(it)
                        }
                    }
                }
            }
        }
    }

    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
        val achievements = context.loritta.services.users.getUserAchievements(context.user)

        context.sendMessage(
            createMessage(
                context.loritta,
                context.user,
                context.i18nContext,
                achievements,
                null
            )
        )
    }

    class ChangeCategoryMenuExecutor(loritta: LorittaCinnamon) : CinnamonSelectMenuExecutor(loritta) {
        companion object : SelectMenuExecutorDeclaration(ComponentExecutorIds.CHANGE_CATEGORY_MENU_EXECUTOR)

        override suspend fun onSelect(
            user: User,
            context: ComponentContext,
            values: List<String>
        ) {
            // Yes, this is unused because we haven't implemented buttons yet :(
            val deserialized = context.decodeDataFromComponentAndRequireUserToMatch<ChangeCategoryData>()

            val newCategory = values.first()
            val achievements = loritta.services.users.getUserAchievements(user)

            context.updateMessage(
                createMessage(
                    loritta,
                    user,
                    context.i18nContext,
                    achievements,
                    try {
                        net.perfectdreams.loritta.cinnamon.achievements.AchievementCategory.valueOf(newCategory)
                    } catch (e: IllegalArgumentException) {
                        if (newCategory == "ALL") // null = All categories
                            null
                        else
                            throw e
                    }
                )
            )
        }
    }

    @Serializable
    class ChangeCategoryData(
        override val userId: Snowflake,
        val page: Int,
        val category: net.perfectdreams.loritta.cinnamon.achievements.AchievementCategory?
    ) : SingleUserComponentData
}