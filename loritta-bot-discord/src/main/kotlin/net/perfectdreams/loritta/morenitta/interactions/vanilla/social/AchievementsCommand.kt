package net.perfectdreams.loritta.morenitta.interactions.vanilla.social

import dev.minn.jda.ktx.interactions.components.option
import dev.minn.jda.ktx.messages.InlineMessage
import dev.minn.jda.ktx.messages.MessageEdit
import net.dv8tion.jda.api.components.buttons.ButtonStyle
import net.dv8tion.jda.api.entities.User
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
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
import net.perfectdreams.loritta.morenitta.interactions.vanilla.economy.SonhosCommand
import net.perfectdreams.loritta.morenitta.utils.extensions.await
import net.perfectdreams.loritta.morenitta.utils.extensions.toJDA
import net.perfectdreams.loritta.serializable.UserId
import java.util.*
import kotlin.math.ceil

class AchievementsCommand(private val loritta: LorittaBot) : SlashCommandDeclarationWrapper {
    companion object {
        private val I18N_PREFIX = I18nKeysData.Commands.Command.Achievements
        private val ACHIEVEMENTS_PER_PAGE = 25
    }

    override fun command() = slashCommand(I18N_PREFIX.Label, I18N_PREFIX.Description, CommandCategory.SOCIAL, UUID.fromString("68ab22f2-fe44-4394-bd34-93665c9b1980")) {
        executor = AchievementsExecutor()
    }

    class AchievementsExecutor : LorittaSlashCommandExecutor() {
        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            context.reply(false) {
                apply(
                    createMessage(
                        context.loritta,
                        context.user,
                        context,
                        context.i18nContext,
                        null,
                        0
                    )
                )
            }
        }

        suspend fun createMessage(
            loritta: LorittaBot,
            user: User,
            context: UnleashedContext,
            i18nContext: I18nContext,
            category: AchievementCategory?,
            page: Long = 0
        ): InlineMessage<*>.() -> (Unit) {
            val achievements = context.loritta.pudding.users.getUserAchievements(UserId(context.user.idLong))

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

            // Sort the achievements by when they were achieved, so more recent achievements -> older achievements
            val sortedAchievements = achievementsOfTheCurrentCategory.sortedByDescending { it.achievedAt }

            val startIndex = (page * ACHIEVEMENTS_PER_PAGE).toInt()
            val endIndex = minOf(startIndex + ACHIEVEMENTS_PER_PAGE, sortedAchievements.size)
            val paginatedAchievements = if (sortedAchievements.isNotEmpty() && startIndex < sortedAchievements.size) {
                sortedAchievements.subList(startIndex, endIndex)
            } else {
                emptyList()
            }

            // Calculate max page
            val maxPage = ceil(sortedAchievements.size / ACHIEVEMENTS_PER_PAGE.toDouble())
            val maxPageZeroIndexed = maxPage - 1

            return {
                // Display page information
                styled(
                    i18nContext.get(
                        SonhosCommand.TRANSACTIONS_I18N_PREFIX.Page(page + 1)
                    ),
                    Emotes.LoriReading
                )

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

                    if (sortedAchievements.isEmpty()) {
                        description.append(
                            i18nContext.get(
                                if (category == null)
                                    I18N_PREFIX.YouDontHaveAnyAchievements
                                else
                                    I18N_PREFIX.YouDontHaveAnyAchievementsInTheCategory
                            )
                        )
                    } else {
                        // Display the paginated achievements
                        for (achievement in paginatedAchievements) {
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
                        context.alwaysEphemeral,
                        {
                            fun insertOption(optionCategory: AchievementCategory) {
                                val userAchievementsInCategoryCount =
                                    achievements.count { it.type.category == optionCategory }
                                val totalAchievementsInCategoryCount = AchievementType.entries
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

                            AchievementCategory.entries.forEach {
                                insertOption(it)
                            }
                        }
                    ) { context, values ->
                        val hook = context.deferEdit()

                        val newCategory = values.first()

                        hook.editOriginal(
                            MessageEdit {
                                apply(
                                    createMessage(
                                        loritta,
                                        user,
                                        context,
                                        context.i18nContext,
                                        try {
                                            AchievementCategory.valueOf(newCategory)
                                        } catch (e: IllegalArgumentException) {
                                            if (newCategory == "ALL") // null = All categories
                                                null
                                            else
                                                throw e
                                        },
                                        0 // Reset to first page when changing categories
                                    )
                                )
                            }
                        ).await()
                    }
                )

                // Only show pagination buttons if there are multiple pages
                if (sortedAchievements.isNotEmpty() && maxPage > 1) {
                    actionRow(
                        // left button
                        loritta.interactivityManager.buttonForUser(
                            user,
                            context.alwaysEphemeral,
                            ButtonStyle.PRIMARY,
                            "",
                            {
                                loriEmoji = Emotes.ChevronLeft
                            }
                        ) {
                            val hook = it.deferEdit()

                            hook.editOriginal(
                                MessageEdit {
                                    apply(
                                        createMessage(
                                            loritta,
                                            user,
                                            it,
                                            it.i18nContext,
                                            category,
                                            page - 1
                                        )
                                    )
                                }
                            ).await()
                        },

                        // right button
                        loritta.interactivityManager.buttonForUser(
                            user,
                            context.alwaysEphemeral,
                            ButtonStyle.PRIMARY,
                            "",
                            {
                                loriEmoji = Emotes.ChevronRight
                                disabled = page >= maxPageZeroIndexed
                            }
                        ) {
                            val hook = it.deferEdit()

                            hook.editOriginal(
                                MessageEdit {
                                    apply(
                                        createMessage(
                                            loritta,
                                            user,
                                            it,
                                            it.i18nContext,
                                            category,
                                            page + 1
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
}
