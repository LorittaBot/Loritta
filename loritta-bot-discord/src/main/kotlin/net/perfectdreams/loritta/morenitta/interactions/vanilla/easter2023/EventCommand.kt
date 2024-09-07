package net.perfectdreams.loritta.morenitta.interactions.vanilla.easter2023

import com.github.salomonbrys.kotson.jsonObject
import dev.minn.jda.ktx.messages.InlineMessage
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import net.dv8tion.jda.api.utils.FileUpload
import net.dv8tion.jda.api.utils.TimeFormat
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.economy.declarations.SonhosCommand
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.social.declarations.XpCommand
import net.perfectdreams.loritta.cinnamon.discord.utils.images.ImageFormatType
import net.perfectdreams.loritta.cinnamon.discord.utils.images.ImageUtils.toByteArray
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.pudding.tables.DonationKeys
import net.perfectdreams.loritta.cinnamon.pudding.tables.ProfileDesignsPayments
import net.perfectdreams.loritta.cinnamon.pudding.tables.Profiles
import net.perfectdreams.loritta.cinnamon.pudding.tables.SonhosTransactionsLog
import net.perfectdreams.loritta.cinnamon.pudding.tables.easter2023.CollectedEaster2023Eggs
import net.perfectdreams.loritta.cinnamon.pudding.tables.easter2023.CreatedEaster2023Baskets
import net.perfectdreams.loritta.cinnamon.pudding.tables.easter2023.Easter2023Drops
import net.perfectdreams.loritta.cinnamon.pudding.tables.easter2023.Easter2023Players
import net.perfectdreams.loritta.cinnamon.pudding.tables.transactions.Easter2023SonhosTransactionsLog
import net.perfectdreams.loritta.cinnamon.pudding.utils.PaymentGateway
import net.perfectdreams.loritta.cinnamon.pudding.utils.PaymentReason
import net.perfectdreams.loritta.cinnamon.pudding.utils.SimpleSonhosTransactionsLogUtils
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.utils.LorittaColors
import net.perfectdreams.loritta.common.utils.TransactionType
import net.perfectdreams.loritta.common.utils.easter2023.EasterEggColor
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.dao.Payment
import net.perfectdreams.loritta.morenitta.easter2023event.LorittaEaster2023Event
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.commands.LorittaSlashCommandExecutor
import net.perfectdreams.loritta.morenitta.interactions.commands.SlashCommandArguments
import net.perfectdreams.loritta.morenitta.interactions.commands.SlashCommandDeclarationWrapper
import net.perfectdreams.loritta.morenitta.interactions.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.morenitta.interactions.commands.slashCommand
import net.perfectdreams.loritta.morenitta.utils.Constants
import net.perfectdreams.loritta.morenitta.utils.RankingGenerator
import net.perfectdreams.loritta.morenitta.utils.extensions.await
import net.perfectdreams.loritta.serializable.StoredEaster2023SonhosTransaction
import org.jetbrains.exposed.sql.*
import java.time.Instant
import java.util.*
import kotlin.math.ceil

class EventCommand(val loritta: LorittaBot) : SlashCommandDeclarationWrapper {
    private val I18N_PREFIX = I18nKeysData.Commands.Command.Easter2023event

    override fun command() = slashCommand(I18N_PREFIX.Label, I18N_PREFIX.Description, CommandCategory.FUN, UUID.fromString("a1a03b03-b23d-43b8-93f5-0c714307220d")) {
        subcommand(I18N_PREFIX.Join.Label, I18N_PREFIX.Description, UUID.fromString("a9df2f98-5e35-4716-a8bc-906e566cb5ed")) {
            executor = JoinEventExecutor()
        }

        subcommand(I18N_PREFIX.Stats.Label, I18N_PREFIX.Stats.Description, UUID.fromString("011500c4-9955-49c2-aa72-925f576d6b0c")) {
            executor = StatsEventExecutor()
        }

        subcommand(I18N_PREFIX.Inventory.Label, I18N_PREFIX.Inventory.Description, UUID.fromString("b3766a24-5b2d-4fea-8c9d-7c02b047601b")) {
            executor = InventoryEventExecutor()
        }

        subcommand(I18N_PREFIX.Rank.Label, I18N_PREFIX.Rank.Description, UUID.fromString("39a1ba04-ad37-45cb-a3ba-7497cb1d28e7")) {
            executor = StatsRankExecutor()
        }
    }

    inner class JoinEventExecutor : LorittaSlashCommandExecutor() {
        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            context.deferChannelMessage(true)

            if (!LorittaEaster2023Event.isEventActive()) {
                context.reply(true) {
                    styled(
                        "Infelizmente o evento já acabou...",
                        Emotes.LoriSob
                    )
                }
                return
            }

            val alreadyJoined = loritta.newSuspendedTransaction {
                val alreadyJoined = Easter2023Players.select {
                    Easter2023Players.id eq context.user.idLong
                }.count() != 0L

                if (!alreadyJoined) {
                    Easter2023Players.insert {
                        it[Easter2023Players.id] = context.user.idLong
                        it[Easter2023Players.joinedAt] = Instant.now()
                    }
                }

                alreadyJoined
            }

            if (alreadyJoined) {
                context.reply(true) {
                    styled(
                        "Você já está participando do evento!",
                        Emotes.LoriShrug
                    )
                }
                return
            } else {
                context.reply(true) {
                    styled(
                        "Você está participando do evento! Obrigada a ajudar a Loritta a encontrar os ovos de páscoa para os amigos dela!",
                        Emotes.LoriHi
                    )

                    styled(
                        "Os ovos de páscoa ${LorittaEaster2023Event.eggEmojis.joinToString("") { it.asMention }} estão espalhados pelo chat, aparecendo como reações nas conversas.",
                        Emotes.LoriHm
                    )

                    styled(
                        "Ao encontrar algum ovo de páscoa, reaja nele para coletá-lo. Mas seja rápido, pois os ovos expiram! Por que eles expiram? Pois os ovos são de liquidação de estoque e vão vencer em breve.",
                        Emotes.LoriWow
                    )

                    styled(
                        "Como a Loritta é estrelinha, os ovos de páscoa só aparecem em servidores que possuem mais de mil membros!",
                        Emotes.LoriHmpf
                    )

                    styled(
                        "Após coletar ovos, você precisa criar as cestas de páscoa usando ${loritta.commandMentions.eventInventory} e, com as cestas, você receberá recompensas!",
                        LorittaEaster2023Event.basketEmoji.name
                    )

                    styled(
                        "Boa caça ao tesou... Quer dizer, caça aos ovos de páscoa!",
                        Emotes.LoriHeart
                    )
                }
                return
            }
        }
    }

    inner class StatsEventExecutor : LorittaSlashCommandExecutor() {
        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            context.deferChannelMessage(true)

            val (joined, collectedPoints) = loritta.newSuspendedTransaction {
                val joined = Easter2023Players.select {
                    Easter2023Players.id eq context.user.idLong
                }.count() != 0L

                val count = CreatedEaster2023Baskets.select {
                    CreatedEaster2023Baskets.user eq context.user.idLong
                }.count()

                Pair(joined, count)
            }

            if (!joined) {
                context.reply(true) {
                    styled("Você precisa entrar no evento antes de poder ver as suas estatísticas!")
                }
                return
            }

            context.reply(true) {
                styled("Você criou **$collectedPoints cestas ${LorittaEaster2023Event.basketEmoji.name}**! Crie cestas com os ovos coletados em ${loritta.commandMentions.eventInventory}")

                styled("O evento irá acabar às ${TimeFormat.DATE_TIME_LONG.format(LorittaEaster2023Event.endOfEvent)}!")
                styled("Lembre-se que cestas apenas aparecem em servidores que possuem mais de mil membros!")

                for (reward in LorittaEaster2023Event.eventRewards.sortedBy { it.requiredPoints }) {
                    when (reward) {
                        is LorittaEaster2023Event.EventReward.BadgeReward -> {
                            styled(
                                buildString {
                                    append("**[${reward.requiredPoints} cestas]")
                                    if (reward.prestige) {
                                        append(" (Prestígio \uD83D\uDD25)")
                                    }
                                    append("** ")
                                    append("Badge para o seu ${loritta.commandMentions.profileView}")
                                },
                                prefix = if (collectedPoints >= reward.requiredPoints) "✅" else "❌"
                            )
                        }

                        is LorittaEaster2023Event.EventReward.SonhosReward -> {
                            styled(
                                buildString {
                                    append("**[${reward.requiredPoints} cestas]")
                                    if (reward.prestige) {
                                        append(" (Prestígio \uD83D\uDD25)")
                                    }
                                    append("** ")
                                    append("${reward.sonhos} sonhos")
                                },
                                prefix = if (collectedPoints >= reward.requiredPoints) "✅" else "❌"
                            )
                        }

                        is LorittaEaster2023Event.EventReward.PremiumKeyReward -> {
                            styled(
                                buildString {
                                    append("**[${reward.requiredPoints} cestas]")
                                    if (reward.prestige) {
                                        append(" (Prestígio \uD83D\uDD25)")
                                    }
                                    append("** ")
                                    append("Todas as vantagens premiums (R\$ 99,99) por três meses")
                                },
                                prefix = if (collectedPoints >= reward.requiredPoints) "✅" else "❌"
                            )
                        }

                        is LorittaEaster2023Event.EventReward.ProfileDesignReward -> {
                            styled(
                                buildString {
                                    append("**[${reward.requiredPoints} cestas]")
                                    if (reward.prestige) {
                                        append(" (Prestígio \uD83D\uDD25)")
                                    }
                                    append("** ")
                                    append("Design de Perfil ${context.locale["profileDesigns.${reward.profileName}.title"]}")
                                },
                                prefix = if (collectedPoints >= reward.requiredPoints) "✅" else "❌"
                            )
                        }
                    }
                }

                styled("*Ovos apenas aparecem em mensagens de membros que estão participando do evento. Você precisa de um servidor com membros participando? Então entre na Comunidade da Loritta! https://discord.gg/lori *")
            }
        }
    }

    inner class InventoryEventExecutor : LorittaSlashCommandExecutor() {
        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            context.deferChannelMessage(true)

            val eggColorCount = Easter2023Drops.eggColor.count()

            val (joined, eggCounts, basketCount) = loritta.newSuspendedTransaction {
                val joined = Easter2023Players.select {
                    Easter2023Players.id eq context.user.idLong
                }.count() != 0L

                val eggCounts = CollectedEaster2023Eggs.innerJoin(Easter2023Drops)
                    .slice(Easter2023Drops.eggColor, eggColorCount)
                    .select {
                        CollectedEaster2023Eggs.user eq context.user.idLong and (CollectedEaster2023Eggs.associatedWithBasket.isNull())
                    }
                    .groupBy(Easter2023Drops.eggColor)
                    .associate { Pair(it[Easter2023Drops.eggColor], it[eggColorCount]) }

                val basketCount = CreatedEaster2023Baskets.select {
                    CreatedEaster2023Baskets.user eq context.user.idLong
                }.count()

                Triple(joined, eggCounts, basketCount)
            }

            if (!joined) {
                context.reply(true) {
                    styled("Você precisa entrar no evento antes de poder ver as suas estatísticas!")
                }
                return
            }

            val activeBasket = LorittaEaster2023Event.getUserCurrentActiveBasket(context.user.idLong, basketCount)

            val btn = loritta.interactivityManager.buttonForUser(
                context.user,
                ButtonStyle.PRIMARY,
                "Criar Cesta",
                {
                    emoji = LorittaEaster2023Event.basketEmoji
                }
            ) { context ->
                context.deferChannelMessage(true)

                // Attempt to create a basket
                val basketCreationResult = loritta.newSuspendedTransaction {
                    val basketCount = CreatedEaster2023Baskets.select {
                        CreatedEaster2023Baskets.user eq context.user.idLong
                    }.count()

                    val activeBasket = LorittaEaster2023Event.getUserCurrentActiveBasket(context.user.idLong, basketCount)

                    val redEggIds = CollectedEaster2023Eggs.innerJoin(Easter2023Drops)
                        .select {
                            CollectedEaster2023Eggs.user eq context.user.idLong and (CollectedEaster2023Eggs.associatedWithBasket.isNull()) and (Easter2023Drops.eggColor eq EasterEggColor.RED)
                        }.map { it[CollectedEaster2023Eggs.id] }

                    val blueEggIds = CollectedEaster2023Eggs.innerJoin(Easter2023Drops)
                        .select {
                            CollectedEaster2023Eggs.user eq context.user.idLong and (CollectedEaster2023Eggs.associatedWithBasket.isNull()) and (Easter2023Drops.eggColor eq EasterEggColor.BLUE)
                        }.map { it[CollectedEaster2023Eggs.id] }

                    val greenEggIds = CollectedEaster2023Eggs.innerJoin(Easter2023Drops)
                        .select {
                            CollectedEaster2023Eggs.user eq context.user.idLong and (CollectedEaster2023Eggs.associatedWithBasket.isNull()) and (Easter2023Drops.eggColor eq EasterEggColor.GREEN)
                        }.map { it[CollectedEaster2023Eggs.id] }

                    val yellowEggIds = CollectedEaster2023Eggs.innerJoin(Easter2023Drops)
                        .select {
                            CollectedEaster2023Eggs.user eq context.user.idLong and (CollectedEaster2023Eggs.associatedWithBasket.isNull()) and (Easter2023Drops.eggColor eq EasterEggColor.YELLOW)
                        }.map { it[CollectedEaster2023Eggs.id] }

                    if (activeBasket.redEggs > redEggIds.size || activeBasket.blueEggs > blueEggIds.size || activeBasket.greenEggs > greenEggIds.size || activeBasket.yellowEggs > yellowEggIds.size)
                        return@newSuspendedTransaction BasketCreationResult.InsufficientEggs

                    // Okay, we do have enough eggs! Let's create a :sparkles: basket :sparkles:
                    val basket = CreatedEaster2023Baskets.insertAndGetId {
                        it[CreatedEaster2023Baskets.user] = context.user.idLong
                        it[CreatedEaster2023Baskets.createdAt] = Instant.now()
                    }

                    // And then update every egg ID to reference the newly created basket
                    CollectedEaster2023Eggs.update({
                        // We only want to update enough eggs for our basket
                        CollectedEaster2023Eggs.id inList (redEggIds.take(activeBasket.redEggs) + blueEggIds.take(activeBasket.blueEggs) + greenEggIds.take(activeBasket.greenEggs) + yellowEggIds.take(activeBasket.yellowEggs))
                    }) {
                        it[CollectedEaster2023Eggs.associatedWithBasket] = basket
                    }

                    // How many baskets do they now have?
                    val newBasketCount = (basketCount + 1).toInt()
                    for (reward in LorittaEaster2023Event.eventRewards) {
                        if (reward.requiredPoints != newBasketCount)
                            continue

                        when (reward) {
                            is LorittaEaster2023Event.EventReward.SonhosReward -> {
                                // Cinnamon transactions log
                                SimpleSonhosTransactionsLogUtils.insert(
                                    context.user.idLong,
                                    Instant.now(),
                                    TransactionType.EVENTS,
                                    reward.sonhos,
                                    StoredEaster2023SonhosTransaction(reward.requiredPoints)
                                )

                                Profiles.update({ Profiles.id eq context.user.idLong }) {
                                    with(SqlExpressionBuilder) {
                                        it[money] = money + reward.sonhos
                                    }
                                }
                            }
                            is LorittaEaster2023Event.EventReward.PremiumKeyReward -> {
                                DonationKeys.insert {
                                    it[DonationKeys.userId] = context.user.idLong
                                    it[value] = 100.0
                                    it[expiresAt] = System.currentTimeMillis() + (Constants.DONATION_ACTIVE_MILLIS * 3)
                                    it[metadata] = jsonObject("type" to "LorittaEaster2023event").toString()
                                }

                                Payment.new {
                                    this.createdAt = System.currentTimeMillis()
                                    this.discount = 0.0
                                    this.paidAt = System.currentTimeMillis()
                                    this.expiresAt = System.currentTimeMillis() + (Constants.DONATION_ACTIVE_MILLIS * 3)
                                    this.userId = context.user.idLong
                                    this.gateway = PaymentGateway.OTHER
                                    this.reason = PaymentReason.DONATION
                                    this.money = 100.0.toBigDecimal()
                                }
                            }
                            is LorittaEaster2023Event.EventReward.ProfileDesignReward -> {
                                val internalName = reward.profileName
                                val alreadyHasTheBackground = ProfileDesignsPayments.select { ProfileDesignsPayments.userId eq context.user.idLong and (ProfileDesignsPayments.profile eq internalName) }
                                    .count() != 0L

                                if (!alreadyHasTheBackground) {
                                    ProfileDesignsPayments.insert {
                                        it[ProfileDesignsPayments.userId] = context.user.idLong
                                        it[cost] = 0
                                        it[profile] = internalName
                                        it[boughtAt] = System.currentTimeMillis()
                                    }
                                }
                            }
                            is LorittaEaster2023Event.EventReward.BadgeReward -> {
                                // noop
                            }
                        }
                    }

                    return@newSuspendedTransaction BasketCreationResult.Success
                }

                when (basketCreationResult) {
                    BasketCreationResult.InsufficientEggs -> {
                        context.reply(true) {
                            styled(
                                "Você não tem ovos suficientes para criar uma cesta!",
                                Emotes.LoriSob
                            )
                        }
                    }

                    BasketCreationResult.Success -> {
                        context.reply(true) {
                            styled(
                                "Você criou uma cesta de páscoa!",
                                LorittaEaster2023Event.basketEmoji.name
                            )
                        }
                    }
                }
            }

            context.reply(true) {
                embed {
                    description = buildString {
                        appendLine("Atualmente você possui $basketCount cestas! Veja as suas recompensas em ${loritta.commandMentions.eventStats}.")
                        appendLine()
                        appendLine("**Ovos no seu inventário:**")
                        appendLine("${LorittaEaster2023Event.eggRed.asMention} ${eggCounts[EasterEggColor.RED] ?: 0}x")
                        appendLine("${LorittaEaster2023Event.eggBlue.asMention} ${eggCounts[EasterEggColor.BLUE] ?: 0}x")
                        appendLine("${LorittaEaster2023Event.eggGreen.asMention} ${eggCounts[EasterEggColor.GREEN] ?: 0}x")
                        appendLine("${LorittaEaster2023Event.eggYellow.asMention} ${eggCounts[EasterEggColor.YELLOW] ?: 0}x")
                        appendLine()
                        appendLine("**A sua próxima cesta necessita dos seguintes ovos:**")
                        appendLine("${LorittaEaster2023Event.eggRed.asMention} ${activeBasket.redEggs}x")
                        appendLine("${LorittaEaster2023Event.eggBlue.asMention} ${activeBasket.blueEggs}x")
                        appendLine("${LorittaEaster2023Event.eggGreen.asMention} ${activeBasket.greenEggs}x")
                        appendLine("${LorittaEaster2023Event.eggYellow.asMention} ${activeBasket.yellowEggs}x")

                        color = LorittaColors.LorittaAqua.rgb

                        // Discord, for some reason, isn't rendering the non @1280w version, so let's scale the image down to help poor discord
                        image = "https://stuff.loritta.website/loritta-easter-2023@1280w.png"
                    }
                }

                actionRow(btn)
            }
        }
    }

    sealed class BasketCreationResult {
        object Success : BasketCreationResult()
        object InsufficientEggs : BasketCreationResult()
    }

    inner class StatsRankExecutor : LorittaSlashCommandExecutor() {
        inner class Options : ApplicationCommandOptions() {
            val page = optionalLong("page", XpCommand.XP_RANK_I18N_PREFIX.Options.Page.Text) /* {
                // range = RankingGenerator.VALID_RANKING_PAGES
            } */
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            val page = (args[options.page]?.minus(1)) ?: 0

            context.deferChannelMessage(false)

            context.reply(false) {
                createRankMessage(context, page)()
            }
        }

        private suspend fun createRankMessage(context: UnleashedContext, page: Long): suspend InlineMessage<*>.() -> (Unit) = {
            styled(
                context.i18nContext.get(SonhosCommand.TRANSACTIONS_I18N_PREFIX.Page(page + 1)),
                Emotes.LoriReading
            )

            val countColumn = CreatedEaster2023Baskets.user.count()

            val (totalCount, profiles) = loritta.pudding.transaction {
                val totalCount = CreatedEaster2023Baskets
                    .slice(CreatedEaster2023Baskets.user)
                    .selectAll()
                    .groupBy(CreatedEaster2023Baskets.user)
                    .count()

                val profilesInTheQuery =
                    CreatedEaster2023Baskets
                        .slice(CreatedEaster2023Baskets.user, countColumn)
                        .selectAll()
                        .groupBy(CreatedEaster2023Baskets.user)
                        .orderBy(countColumn to SortOrder.DESC)
                        .limit(5, page * 5)
                        .toList()

                Pair(totalCount, profilesInTheQuery)
            }

            // Calculates the max page
            val maxPage = ceil(totalCount / 5.0)
            val maxPageZeroIndexed = maxPage - 1

            files += FileUpload.fromData(
                RankingGenerator.generateRanking(
                    loritta,
                    page * 5,
                    "Evento de Páscoa",
                    null,
                    profiles.map {
                        val presentesCount = it[countColumn]

                        RankingGenerator.UserRankInformation(
                            it[CreatedEaster2023Baskets.user].value,
                            "$presentesCount cestas"
                        )
                    }
                ) {
                    null
                }.toByteArray(ImageFormatType.PNG).inputStream(),
                "rank.png"
            )

            actionRow(
                loritta.interactivityManager.buttonForUser(
                    context.user,
                    ButtonStyle.PRIMARY,
                    builder = {
                        loriEmoji = Emotes.ChevronLeft
                        disabled = page !in RankingGenerator.VALID_RANKING_PAGES
                    }
                ) {
                    it.deferEdit()
                        .editOriginal(
                            InlineMessage(MessageEditBuilder())
                                .apply {
                                    createRankMessage(
                                        context,
                                        page - 1
                                    )()
                                }.build()
                        )
                        .await()
                },
                loritta.interactivityManager.buttonForUser(
                    context.user,
                    ButtonStyle.PRIMARY,
                    builder = {
                        loriEmoji = Emotes.ChevronRight
                        disabled = page + 2 !in RankingGenerator.VALID_RANKING_PAGES || page >= maxPageZeroIndexed
                    }
                ) {
                    it.deferEdit()
                        .editOriginal(
                            InlineMessage(MessageEditBuilder())
                                .apply {
                                    createRankMessage(
                                        context,
                                        page + 1
                                    )()
                                }.build()
                        )
                        .await()
                },
            )
        }
    }
}