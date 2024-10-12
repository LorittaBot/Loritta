package net.perfectdreams.loritta.morenitta.interactions.vanilla.economy

import dev.minn.jda.ktx.messages.InlineMessage
import dev.minn.jda.ktx.messages.MessageEdit
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import net.dv8tion.jda.api.utils.FileUpload
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.economy.sonhosrank.SonhosRankType
import net.perfectdreams.loritta.cinnamon.discord.utils.images.ImageFormatType
import net.perfectdreams.loritta.cinnamon.discord.utils.images.ImageUtils.toByteArray
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.pudding.services.UsersService
import net.perfectdreams.loritta.cinnamon.pudding.tables.Profiles
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.GuildProfiles
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.commands.LegacyMessageCommandContext
import net.perfectdreams.loritta.morenitta.interactions.commands.LorittaLegacyMessageCommandExecutor
import net.perfectdreams.loritta.morenitta.interactions.commands.LorittaSlashCommandExecutor
import net.perfectdreams.loritta.morenitta.interactions.commands.SlashCommandArguments
import net.perfectdreams.loritta.morenitta.interactions.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.morenitta.interactions.commands.options.OptionReference
import net.perfectdreams.loritta.morenitta.utils.ImageFormat
import net.perfectdreams.loritta.morenitta.utils.RankingGenerator
import net.perfectdreams.loritta.morenitta.utils.extensions.await
import net.perfectdreams.loritta.morenitta.utils.extensions.getIconUrl
import org.jetbrains.exposed.sql.*
import kotlin.math.ceil

class SonhosRankExecutor(private val loritta: LorittaBot) : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
    companion object {
        suspend fun createRankMessage(
            loritta: LorittaBot,
            context: UnleashedContext,
            page: Long,
            guild: Guild?
        ): suspend InlineMessage<*>.() -> (Unit) = {
            // It's quite simple, if the guild is null, the rank is global, otherwise it's local.
            // The Kord version has two functions to do the same stuff, but I'm going to use only one function, this function.
            styled(
                context.i18nContext.get(
                    SonhosCommand.TRANSACTIONS_I18N_PREFIX.Page(page + 1)
                ),
                Emotes.LoriReading
            )

            val (totalCount, profiles) = loritta.pudding.transaction {
                if (guild != null) {
                    val totalCount = Profiles.innerJoin(GuildProfiles, { Profiles.id }, { GuildProfiles.userId })
                        .selectAll().where { GuildProfiles.guildId eq guild.id.toLong() and (GuildProfiles.isInGuild eq true) }.count()

                    val profilesInTheQuery = Profiles.innerJoin(GuildProfiles, { Profiles.id }, { GuildProfiles.userId })
                        .selectAll().where {
                            GuildProfiles.guildId eq guild.id.toLong() and (GuildProfiles.isInGuild eq true) and (GuildProfiles.userId notInSubQuery UsersService.validBannedUsersList(
                                System.currentTimeMillis()
                            ))
                        }
                        .orderBy(Profiles.money, SortOrder.DESC)
                        .limit(5, page * 5)
                        .toList()

                    Pair(totalCount, profilesInTheQuery)
                } else {
                    val profiles = Profiles
                        .selectAll().where { Profiles.id notInSubQuery UsersService.validBannedUsersList(System.currentTimeMillis()) }
                        .orderBy(Profiles.money, SortOrder.DESC)
                        .limit(5, page * 5)
                        .toList()

                    Pair(
                        // TODO: Fix the total Count, however there isn't an easy way to fix this
                        // While you could be thinking "well, I can just use a SELECT COUNT(*) FROM profiles query!", that gonna be very resource intensive on the db side.
                        // Because Loritta has a looooooooot of profiles
                        // (Besides, Loritta will always have more than (pageSize * RankingGenerator.VALID_RANKING_PAGES.last) profiles, heh
                        5 * RankingGenerator.VALID_RANKING_PAGES.last,
                        profiles
                    )
                }
            }

            val maxPage = ceil(totalCount / 5.0)
            val maxPageZeroIndexed = maxPage - 1

            val file = if (guild == null) {
                FileUpload.fromData(
                    RankingGenerator.generateRanking(
                        loritta,
                        page * 5,
                        context.i18nContext.get(
                            SonhosCommand.SONHOS_RANK_I18N_PREFIX.GlobalSonhosRank
                        ),
                        null,
                        profiles.map {
                            RankingGenerator.UserRankInformation(
                                it[Profiles.id].value,
                                context.i18nContext.get(
                                    I18nKeysData.Commands.SonhosWithQuantity(it[Profiles.money])
                                )
                            )
                        }
                    ).toByteArray(ImageFormatType.PNG).inputStream(),
                    "rank.png"
                )
            } else {
                FileUpload.fromData(
                    RankingGenerator.generateRanking(
                        loritta,
                        page * 5,
                        context.i18nContext.get(SonhosCommand.SONHOS_RANK_I18N_PREFIX.LocalSonhosRank),
                        guild.getIconUrl(2048, ImageFormat.PNG),
                        profiles.map {
                            RankingGenerator.UserRankInformation(
                                it[Profiles.id].value,
                                context.i18nContext.get(I18nKeysData.Commands.SonhosWithQuantity(it[Profiles.money]))
                            )
                        }
                    ) {
                        loritta.pudding.transaction {
                            GuildProfiles.update({ GuildProfiles.id eq it.toLong() and (GuildProfiles.guildId eq guild.id.toLong()) }) {
                                it[isInGuild] = false
                            }
                        }
                        null
                    }.toByteArray(ImageFormatType.PNG).inputStream(),
                    "rank.png"
                )
            }

            files.plusAssign(file)

            actionRow(
                // left button
                loritta.interactivityManager.buttonForUser(
                    context.user,
                    ButtonStyle.PRIMARY,
                    "",
                    {
                        loriEmoji = Emotes.ChevronLeft
                        disabled = page !in RankingGenerator.VALID_RANKING_PAGES
                    }
                ) {
                    val hook = it.updateMessageSetLoadingState()

                    val builtMessage = createRankMessage(loritta, it, page - 1, guild)

                    val asMessageEditData = MessageEdit {
                        builtMessage()
                    }

                    hook.editOriginal(asMessageEditData).await()
                },
                
                // right button
                loritta.interactivityManager.buttonForUser(
                    context.user,
                    ButtonStyle.PRIMARY,
                    "",
                    {
                        loriEmoji = Emotes.ChevronRight
                        disabled = page + 2 !in RankingGenerator.VALID_RANKING_PAGES || page >= maxPageZeroIndexed
                    }
                ) {
                    val hook = it.updateMessageSetLoadingState()

                    val builtMessage = createRankMessage(loritta, it, page + 1, guild)

                    val asMessageEditData = MessageEdit {
                        builtMessage()
                    }

                    hook.editOriginal(asMessageEditData).await()
                }
            )
        }
    }

    inner class Options : ApplicationCommandOptions() {
        val rankType = string("rank_type", SonhosCommand.SONHOS_RANK_I18N_PREFIX.Options.RankType.Text) {
            choice(SonhosCommand.SONHOS_RANK_I18N_PREFIX.GlobalSonhosRank, SonhosRankType.GLOBAL.name)
            choice(SonhosCommand.SONHOS_RANK_I18N_PREFIX.LocalSonhosRank, SonhosRankType.LOCAL.name)
        }

        val page = optionalLong("page", SonhosCommand.SONHOS_RANK_I18N_PREFIX.Options.Page.Text, RankingGenerator.VALID_RANKING_PAGES)
    }

    override val options = Options()

    override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
        val type = SonhosRankType.valueOf(args[options.rankType])
        val guild = if (type == SonhosRankType.LOCAL) context.guild else null

        context.deferChannelMessage(false)

        val userPage = args[options.page] ?: 1L
        val page = userPage - 1

        val message = createRankMessage(loritta, context, page, guild)

        context.reply(false) {
            message()
        }
    }

    override suspend fun convertToInteractionsArguments(
        context: LegacyMessageCommandContext,
        args: List<String>
    ): Map<OptionReference<*>, Any?> {
        val arg0 = args.getOrNull(0)
        val arg1 = args.getOrNull(1)
        val isLocal = arg0 == "local"

        val page = if (isLocal) {
            arg1?.toLongOrNull()
        } else {
            arg0?.toLongOrNull()
        }

        return mapOf(
            options.rankType to if (isLocal) SonhosRankType.LOCAL.name else SonhosRankType.GLOBAL.name,
            options.page to page
        )
    }
}
