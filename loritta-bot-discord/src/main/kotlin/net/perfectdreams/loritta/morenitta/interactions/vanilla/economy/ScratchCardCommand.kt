package net.perfectdreams.loritta.morenitta.interactions.vanilla.economy

import com.google.common.cache.CacheBuilder
import dev.minn.jda.ktx.messages.InlineMessage
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.dv8tion.jda.api.components.buttons.ButtonStyle
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.interactions.IntegrationType
import net.dv8tion.jda.api.interactions.InteractionContextType
import net.dv8tion.jda.api.utils.messages.MessageCreateData
import net.perfectdreams.harmony.logging.HarmonyLoggerFactory
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.discord.utils.SonhosUtils
import net.perfectdreams.loritta.cinnamon.emotes.Emotes as CinnamonEmotes
import net.perfectdreams.loritta.cinnamon.pudding.tables.Raspadinhas
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.utils.Emotes
import net.perfectdreams.loritta.common.utils.GACampaigns
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.dao.Profile
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.commands.LegacyMessageCommandContext
import net.perfectdreams.loritta.morenitta.interactions.commands.LorittaLegacyMessageCommandExecutor
import net.perfectdreams.loritta.morenitta.interactions.commands.LorittaSlashCommandExecutor
import net.perfectdreams.loritta.morenitta.interactions.commands.SlashCommandArguments
import net.perfectdreams.loritta.morenitta.interactions.commands.SlashCommandDeclarationWrapper
import net.perfectdreams.loritta.morenitta.interactions.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.morenitta.interactions.commands.options.OptionReference
import net.perfectdreams.loritta.morenitta.interactions.commands.slashCommand
import net.perfectdreams.loritta.morenitta.interactions.components.ComponentContext
import net.perfectdreams.loritta.morenitta.utils.Constants
import net.perfectdreams.loritta.morenitta.utils.RankPaginationUtils
import net.perfectdreams.loritta.morenitta.utils.RankingGenerator
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.count
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.sum
import org.jetbrains.exposed.sql.update
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.ceil

class ScratchCardCommand(val loritta: LorittaBot) : SlashCommandDeclarationWrapper {
    companion object {
        private val logger by HarmonyLoggerFactory.logger {}

        private val mutexes = CacheBuilder.newBuilder().expireAfterWrite(1L, TimeUnit.MINUTES).build<Long, Mutex>()
            .asMap()

        private const val TICKET_PRICE = 150L
        private const val WRONG_CLAIM_PENALTY = 1000L

        private const val LORITTA_COMBO = 100_000
        private const val PANTUFA_COMBO = 10_000
        private const val GABI_COMBO = 1_000
        private const val DOKYO_COMBO = 375
        private const val GESSY_COMBO = 250
        private const val TOBIAS_COMBO = 130

        private val EMOTE_BY_CHAR = mapOf(
            'L' to "<:loritta:664849802961485894>",
            'P' to "<:pantufa:664849802793713686>",
            'B' to "<:gabriela:664849802927800351>",
            'D' to "<:dokyo:664849803397562369>",
            'G' to "<:gessy:664849803334909952>",
            'T' to "<:tobias_nosa:450476856303419432>"
        )

        // Invisible-spoiler shifter used to keep newly bought cards hidden after a message edit
        private const val INVISIBLE_SPOILER_ROW = "||‍||||‍||||‍||||‍||||‍||||‍||||‍||||‍||||‍||||‍||||‍||"

        private fun mutexFor(userId: Long) = mutexes.getOrPut(userId, { Mutex() })

        private fun rollPattern(): Array<Array<Char>> {
            val array = Array(3) { Array(3) { 'Z' } }
            for (x in 0 until 3) {
                for (y in 0 until 3) {
                    val randomNumber = LorittaBot.RANDOM.nextInt(1, 101)
                    array[x][y] = when (randomNumber) {
                        100 -> 'L'                  // 1
                        in 94..99 -> 'P'           // 6
                        in 78..93 -> 'B'           // 16
                        in 59..77 -> 'D'           // 19
                        in 34..58 -> 'G'           // 25
                        in 0..33 -> 'T'            // 34
                        else -> 'Z'
                    }
                }
            }
            return array
        }

        private fun renderCardContent(array: Array<Array<Char>>): String {
            fun emote(c: Char) = EMOTE_BY_CHAR[c] ?: error("Unknown emoji char $c")

            return """<:scratch_01:664139718279168002><:scratch_02:664139717889097739><:scratch_03:664139718161596416><:scratch_04:664139718329630721><:scratch_05:664139718304464896><:scratch_06:664139718132236318>
<:scratch_07:664139718337888266>||${emote(array[0][0])}||||${emote(array[1][0])}||||${emote(array[2][0])}||<:scratch_11:664139718220316702><:scratch_12:664139718220316685>
<:scratch_13:664139718308397076>||${emote(array[0][1])}||||${emote(array[1][1])}||||${emote(array[2][1])}||<:scratch_17:664139718321242134><:scratch_18:664139718123978763>
<:scratch_19:664139718140755986>||${emote(array[0][2])}||||${emote(array[1][2])}||||${emote(array[2][2])}||<:scratch_23:664139718266716160><:scratch_24:664139718354665492>
<:scratch_25:664139718354796545><:scratch_26:664139718014795779><:scratch_27:664139718237224981><:scratch_28:664139718388351007><:scratch_29:664139718430162954><:scratch_30:664139717989629968>"""
        }

        private fun parsePattern(stored: String): Array<Array<Char>> {
            val array = Array(3) { Array(3) { 'Z' } }
            val splitted = stored.split("\n")
            for ((y, line) in splitted.withIndex()) {
                for ((x, char) in line.withIndex()) {
                    array[x][y] = char
                }
            }
            return array
        }

        private fun checkArrayFor(array: Array<Array<Char>>, ch: Char): Int {
            var combos = 0
            // horizontal
            if (array[0][0] == ch && array[1][0] == ch && array[2][0] == ch) combos++
            if (array[0][1] == ch && array[1][1] == ch && array[2][1] == ch) combos++
            if (array[0][2] == ch && array[1][2] == ch && array[2][2] == ch) combos++
            // vertical
            if (array[0][0] == ch && array[0][1] == ch && array[0][2] == ch) combos++
            if (array[1][0] == ch && array[1][1] == ch && array[1][2] == ch) combos++
            if (array[2][0] == ch && array[2][1] == ch && array[2][2] == ch) combos++
            // diagonals
            if (array[0][0] == ch && array[1][1] == ch && array[2][2] == ch) combos++
            if (array[2][0] == ch && array[1][1] == ch && array[0][2] == ch) combos++
            return combos
        }

        suspend fun showInfo(context: UnleashedContext) {
            val raspadinhaCount = context.loritta.pudding.transaction {
                Raspadinhas.selectAll().where {
                    Raspadinhas.receivedById eq context.user.idLong
                }.count()
            }
            val earningsCol = Raspadinhas.value.sum()
            val raspadinhaEarnings = context.loritta.pudding.transaction {
                Raspadinhas.select(Raspadinhas.receivedById, earningsCol).where {
                    Raspadinhas.receivedById eq context.user.idLong
                }.groupBy(Raspadinhas.receivedById).firstOrNull()
            }

            val i18nContext = context.i18nContext
            context.reply(false) {
                styled(i18nContext.get(I18nKeysData.Commands.Command.Scratchcard.Title), "<:loritta:331179879582269451>")
                styled(i18nContext.get(I18nKeysData.Commands.Command.Scratchcard.Intro), "<:starstruck:540988091117076481>")
                styled(i18nContext.get(I18nKeysData.Commands.Command.Scratchcard.HowToPlay), "🎫")
                styled(i18nContext.get(I18nKeysData.Commands.Command.Scratchcard.Combos), "👥")
                styled(i18nContext.get(I18nKeysData.Commands.Command.Scratchcard.Loritta(LORITTA_COMBO)))
                styled(i18nContext.get(I18nKeysData.Commands.Command.Scratchcard.Pantufa(PANTUFA_COMBO)))
                styled(i18nContext.get(I18nKeysData.Commands.Command.Scratchcard.Gabriela(GABI_COMBO)))
                styled(i18nContext.get(I18nKeysData.Commands.Command.Scratchcard.Dokyo(DOKYO_COMBO)))
                styled(i18nContext.get(I18nKeysData.Commands.Command.Scratchcard.Gessy(GESSY_COMBO)))
                styled(i18nContext.get(I18nKeysData.Commands.Command.Scratchcard.Tobias(TOBIAS_COMBO)))
                styled(i18nContext.get(I18nKeysData.Commands.Command.Scratchcard.Stats(raspadinhaCount, raspadinhaEarnings?.get(earningsCol) ?: 0)))
                styled(
                    i18nContext.get(
                        I18nKeysData.Commands.Command.Scratchcard.BuyHint(context.loritta.commandMentions.scratchCardBuy)
                    ),
                    "💵"
                )
            }
        }

        /**
         * Shared buy logic. Sends a fresh reply when [editVia] is null, otherwise edits the existing message.
         * [boughtScratchCardsInThisMessage] is incremented across edits to keep newly added spoilers hidden.
         */
        suspend fun buy(
            context: UnleashedContext,
            profile: Profile,
            editVia: ComponentContext? = null,
            boughtScratchCardsInThisMessage: Int = 0
        ) {
            val loritta = context.loritta
            mutexFor(context.user.idLong).withLock {
                if (TICKET_PRICE > profile.money) {
                    val builder: suspend InlineMessage<*>.() -> Unit = {
                        styled(context.i18nContext.get(I18nKeysData.Commands.Command.Scratchcard.NotEnoughSonhos), CinnamonEmotes.LoriSob)
                        styled(
                            context.i18nContext.get(
                                GACampaigns.sonhosBundlesUpsellDiscordMessage(
                                    loritta.config.loritta.dashboard.url,
                                    "scratch-card",
                                    "bet-not-enough-sonhos"
                                )
                            ),
                            Emotes.LORI_RICH.asMention
                        )
                    }
                    if (editVia != null) editVia.editMessage(false) { builder() }
                    else context.reply(false) { builder() }
                    return@withLock
                }

                loritta.newSuspendedTransaction {
                    profile.takeSonhosNested(TICKET_PRICE)
                }

                logger.info { "User ${context.user.idLong} bought one raspadinha ticket!" }

                val array = rollPattern()

                val id = loritta.pudding.transaction {
                    Raspadinhas.insertAndGetId {
                        it[receivedById] = context.user.idLong
                        it[receivedAt] = System.currentTimeMillis()
                        it[pattern] = array.joinToString("\n") { row -> row.joinToString("") }
                        it[scratched] = false
                    }
                }

                // When editing a message, Discord keeps spoilers OPEN if the user already revealed them.
                // We work around it by prepending invisible spoilers, shifting the position of the visible ones
                // so Discord treats them as new spoilers.
                var spoilerCount = boughtScratchCardsInThisMessage
                val card = renderCardContent(array)
                val intro = context.i18nContext.get(
                    I18nKeysData.Commands.Command.Scratchcard.Bought(
                        userMention = context.user.asMention,
                        id = id.value
                    )
                )
                val baseContent = "${Emotes.LORI_RICH} **|** $intro\n$card"
                var contentWithSpoilers = INVISIBLE_SPOILER_ROW.repeat(spoilerCount) + baseContent

                // Discord caps message content at 2000 chars; if the invisible-spoiler trick blows past that,
                // reset by replacing the message with a placeholder, then sending the card on its own.
                if (contentWithSpoilers.length >= 2000 && editVia != null) {
                    editVia.editMessage(false) { content = "..." }
                    contentWithSpoilers = baseContent
                    spoilerCount = 0
                }

                val nextSpoilerCount = spoilerCount + 1

                val buyAnotherButton = loritta.interactivityManager.buttonForUser(
                    context.user,
                    false,
                    ButtonStyle.PRIMARY,
                    context.i18nContext.get(I18nKeysData.Commands.Command.Scratchcard.BuyAnother),
                    builder = { emoji = Emoji.fromUnicode("🔄") }
                ) { btn ->
                    buy(btn, loritta.getOrCreateLorittaProfile(btn.user.idLong), btn, nextSpoilerCount)
                }

                val claimPrizeButton = loritta.interactivityManager.buttonForUser(
                    context.user,
                    false,
                    ButtonStyle.SUCCESS,
                    context.i18nContext.get(I18nKeysData.Commands.Command.Scratchcard.ClaimPrize),
                    builder = { loriEmoji = CinnamonEmotes.LoriRich }
                ) { btn ->
                    btn.deferChannelMessage(false)
                    claim(btn, loritta.getOrCreateLorittaProfile(btn.user.idLong), id.value)
                }

                val builder: suspend InlineMessage<*>.() -> Unit = {
                    content = contentWithSpoilers
                    actionRow(claimPrizeButton, buyAnotherButton)
                }

                if (editVia != null) editVia.editMessage(false) { builder() }
                else context.reply(false) { builder() }
            }
        }

        suspend fun claim(context: UnleashedContext, profile: Profile, id: Long?) {
            val loritta = context.loritta
            mutexFor(context.user.idLong).withLock {
                val raspadinha = loritta.pudding.transaction {
                    Raspadinhas.selectAll().where { Raspadinhas.id eq id }.firstOrNull()
                }

                if (raspadinha == null) {
                    context.reply(false) {
                        styled(context.i18nContext.get(I18nKeysData.Commands.Command.Scratchcard.NotFound), CinnamonEmotes.LoriSob)
                    }
                    return@withLock
                }

                if (raspadinha[Raspadinhas.receivedById] != context.user.idLong) {
                    context.reply(false) {
                        styled(context.i18nContext.get(I18nKeysData.Commands.Command.Scratchcard.NotYours), CinnamonEmotes.LoriSob)
                    }
                    return@withLock
                }

                if (raspadinha[Raspadinhas.scratched]) {
                    context.reply(false) {
                        styled(context.i18nContext.get(I18nKeysData.Commands.Command.Scratchcard.AlreadyClaimed), CinnamonEmotes.LoriSob)
                    }
                    return@withLock
                }

                val array = parsePattern(raspadinha[Raspadinhas.pattern])

                val loriCombos = checkArrayFor(array, 'L')
                val pantufaCombos = checkArrayFor(array, 'P')
                val gabiCombos = checkArrayFor(array, 'B')
                val dokyoCombos = checkArrayFor(array, 'D')
                val gessyCombos = checkArrayFor(array, 'G')
                val tobiasCombos = checkArrayFor(array, 'T')

                val prize = (loriCombos * LORITTA_COMBO) +
                    (pantufaCombos * PANTUFA_COMBO) +
                    (gabiCombos * GABI_COMBO) +
                    (dokyoCombos * DOKYO_COMBO) +
                    (gessyCombos * GESSY_COMBO) +
                    (tobiasCombos * TOBIAS_COMBO)

                loritta.pudding.transaction {
                    Raspadinhas.update({ Raspadinhas.id eq id }) {
                        it[scratched] = true
                        it[value] = prize
                    }
                }

                if (prize == 0) {
                    loritta.newSuspendedTransaction {
                        if (WRONG_CLAIM_PENALTY > profile.money) {
                            profile.money = 0
                        } else {
                            profile.takeSonhosNested(WRONG_CLAIM_PENALTY)
                        }
                    }
                    context.reply(false) {
                        styled("${context.i18nContext.get(I18nKeysData.Commands.Command.Scratchcard.NothingWon)} ${Emotes.LORI_SHRUG}")
                    }
                } else {
                    logger.info { "User ${context.user.idLong} won $prize sonhos in the raspadinha! Combos: Lori: $loriCombos; Pantufa: $pantufaCombos; Gabi: $gabiCombos; Dokyo: $dokyoCombos; Gessy: $gessyCombos; Tobias: $tobiasCombos" }
                    loritta.newSuspendedTransaction {
                        profile.addSonhosNested(prize.toLong())
                    }
                    context.reply(false) {
                        styled(
                            context.i18nContext.get(I18nKeysData.Commands.Command.Scratchcard.YouWon(prize)),
                            Emotes.LORI_PAT.asMention
                        )
                    }
                }
            }
        }
    }

    override fun command() = slashCommand(
        I18nKeysData.Commands.Command.Scratchcard.Label,
        I18nKeysData.Commands.Command.Scratchcard.Description,
        CommandCategory.ECONOMY,
        UUID.fromString("9b7aaa91-59db-41f8-b4cf-bb3e44753c77")
    ) {
        enableLegacyMessageSupport = true
        this.integrationTypes = listOf(IntegrationType.GUILD_INSTALL, IntegrationType.USER_INSTALL)

        subcommand(
            I18nKeysData.Commands.Command.Scratchcard.Info.Label,
            I18nKeysData.Commands.Command.Scratchcard.Info.Description,
            UUID.fromString("11586206-3254-48fa-8180-12426cfe0857")
        ) {
            alternativeLegacyAbsoluteCommandPaths.apply {
                add("raspadinha")
                add("scratchcard")
                add("raspadinha info")
                add("scratchcard info")
            }
            executor = InfoExecutor()
        }

        subcommand(
            I18nKeysData.Commands.Command.Scratchcard.Comprar.Label,
            I18nKeysData.Commands.Command.Scratchcard.Comprar.Description,
            UUID.fromString("8692ad06-98ae-4f63-9597-e74fd64ad5af")
        ) {
            alternativeLegacyAbsoluteCommandPaths.apply {
                add("raspadinha comprar")
                add("raspadinha buy")
                add("scratchcard comprar")
                add("scratchcard buy")
            }
            executor = BuyExecutor()
        }

        subcommand(
            I18nKeysData.Commands.Command.Scratchcard.Ganhar.Label,
            I18nKeysData.Commands.Command.Scratchcard.Ganhar.Description,
            UUID.fromString("1cf83f5d-5de4-4b26-a410-ef11b396a96e")
        ) {
            alternativeLegacyAbsoluteCommandPaths.apply {
                add("raspadinha ganhar")
                add("raspadinha win")
                add("raspadinha claim")
                add("scratchcard ganhar")
                add("scratchcard win")
                add("scratchcard claim")
            }
            executor = ClaimExecutor()
        }

        subcommand(
            I18nKeysData.Commands.Command.Scratchcard.Top.Label,
            I18nKeysData.Commands.Command.Scratchcard.Top.Description,
            UUID.fromString("4f6b1c3a-6c8a-4f6d-9c0f-a2c1b3f7dd02")
        ) {
            alternativeLegacyAbsoluteCommandPaths.apply {
                add("raspadinha top")
                add("scratchcard top")
            }
            executor = ScratchCardTopExecutor()
        }
    }

    class InfoExecutor : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        override val options = ApplicationCommandOptions.NO_OPTIONS

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            if (SonhosUtils.checkIfEconomyIsDisabled(context)) return
            showInfo(context)
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?> = emptyMap()
    }

    class BuyExecutor : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        override val options = ApplicationCommandOptions.NO_OPTIONS

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            if (SonhosUtils.checkIfEconomyIsDisabled(context)) return
            buy(context, context.lorittaUser.profile)
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?> = emptyMap()
    }

    class ClaimExecutor : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        class Options : ApplicationCommandOptions() {
            val id = long("id", I18nKeysData.Commands.Command.Scratchcard.Ganhar.Options.Id.Text)
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            if (SonhosUtils.checkIfEconomyIsDisabled(context)) return
            claim(context, context.lorittaUser.profile, args[options.id])
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?>? {
            val id = args.firstOrNull()?.toLongOrNull()
            if (id == null) {
                context.explain()
                return null
            }
            return mapOf(options.id to id)
        }
    }

    class ScratchCardTopExecutor : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        class Options : ApplicationCommandOptions() {
            val page = optionalLong(
                "page",
                I18nKeysData.Commands.Command.Scratchcard.Top.Options.Page.Text,
                RankingGenerator.VALID_RANKING_PAGES
            )
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            if (SonhosUtils.checkIfEconomyIsDisabled(context)) return
            context.deferChannelMessage(false)

            val userPage = args[options.page] ?: 1L
            val page = userPage - 1

            val message = createRankMessage(context, page)

            context.reply(false) {
                message()
            }
        }

        suspend fun createRankMessage(
            context: UnleashedContext,
            page: Long
        ): suspend InlineMessage<*>.() -> (Unit) = {
            val loritta = context.loritta
            val userId = Raspadinhas.receivedById
            val ticketCount = Raspadinhas.receivedById.count()
            val moneySum = Raspadinhas.value.sum()

            val (totalCount, userData) = loritta.newSuspendedTransaction {
                val total = Raspadinhas.select(userId)
                    .groupBy(userId)
                    .having { moneySum.isNotNull() }
                    .count()

                val rows = Raspadinhas.select(userId, ticketCount, moneySum)
                    .groupBy(userId)
                    .having { moneySum.isNotNull() }
                    .orderBy(moneySum, SortOrder.DESC)
                    .limit(5)
                    .offset(page * 5)
                    .toList()

                Pair(total, rows)
            }

            val maxPage = ceil(totalCount / 5.0)

            val rankingImage = RankingGenerator.generateRanking(
                loritta,
                page * 5,
                context.i18nContext.get(I18nKeysData.Commands.Command.Scratchcard.Top.GlobalRanking),
                null,
                userData.map {
                    RankingGenerator.UserRankInformation(
                        it[userId],
                        context.i18nContext.get(
                            I18nKeysData.Commands.Command.Scratchcard.Top.WonTickets(
                                sonhos = it[moneySum] ?: 0,
                                tickets = it[ticketCount]
                            )
                        )
                    )
                }
            )

            RankPaginationUtils.createRankMessage(
                loritta,
                context,
                page,
                maxPage.toInt(),
                rankingImage
            ) {
                createRankMessage(context, it)
            }.invoke(this)
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?>? {
            val page = args.getOrNull(0)?.toLongOrNull()

            if (page != null && !RankingGenerator.isValidRankingPage(page)) {
                context.reply(false) {
                    styled(
                        context.locale["commands.invalidRankingPage"],
                        Constants.ERROR
                    )
                }
                return null
            }

            return mapOf(options.page to page)
        }
    }
}
