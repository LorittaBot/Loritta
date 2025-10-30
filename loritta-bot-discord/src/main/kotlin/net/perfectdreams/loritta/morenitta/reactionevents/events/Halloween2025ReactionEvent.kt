package net.perfectdreams.loritta.morenitta.reactionevents.events

import dev.minn.jda.ktx.messages.InlineMessage
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.common.emojis.LorittaEmojiReference
import net.perfectdreams.loritta.common.emojis.LorittaEmojis
import net.perfectdreams.loritta.common.emotes.DiscordEmote
import net.perfectdreams.loritta.common.emotes.UnicodeEmote
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.reactionevents.ReactionEvent
import net.perfectdreams.loritta.morenitta.reactionevents.ReactionEventReward
import net.perfectdreams.loritta.morenitta.reactionevents.ReactionSet
import net.perfectdreams.loritta.morenitta.utils.Constants
import net.perfectdreams.loritta.morenitta.utils.extensions.toJDA
import java.time.Instant
import java.time.ZonedDateTime
import java.util.*
import kotlin.random.Random

object Halloween2025ReactionEvent : ReactionEvent() {
    override val internalId: String = "Halloween2025"
    override val startsAt: Instant = ZonedDateTime.of(2025, 10, 29, 21, 0, 0, 0, Constants.LORITTA_TIMEZONE).toInstant()
    override val endsAt: Instant = ZonedDateTime.of(2025, 11, 2, 0, 0, 0, 0, Constants.LORITTA_TIMEZONE).toInstant()

    private val candy1 = ReactionSet(
        UUID.fromString("67b638c8-804c-46ea-8017-75957519ca54"),
        null,
        LorittaEmojiReference.UnicodeEmoji("\uD83C\uDF6C"),
        { 0.050 },
        1
    )

    private val candy2 = ReactionSet(
        UUID.fromString("3fe5d5e3-f9d9-49a6-a8ce-35646047ef28"),
        null,
        LorittaEmojiReference.UnicodeEmoji("\uD83C\uDF6D"),
        { 0.050 },
        1
    )

    private val candy3 = ReactionSet(
        UUID.fromString("a80dbf2a-a564-470d-8525-c8ad5f1d287b"),
        null,
        LorittaEmojiReference.UnicodeEmoji("\uD83C\uDF6B"),
        { 0.050 },
        1
    )

    private val candy4 = ReactionSet(
        UUID.fromString("9337b800-3bc6-4173-b225-782ce4a41b6b"),
        null,
        LorittaEmojiReference.UnicodeEmoji("\uD83E\uDDC1"),
        { 0.050 },
        1
    )

    private val loritta = ReactionSet(
        UUID.fromString("8563a786-e651-41ce-b193-db0e26b74664"),
        null,
        LorittaEmojis.LoriHead,
        {
            if (it?.idLong == Constants.PORTUGUESE_SUPPORT_GUILD_ID) {
                0.025
            } else {
                0.010
            }
        },
        1
    )

    private val pantufa = ReactionSet(
        UUID.fromString("c6f6f3e6-66d7-4c24-aceb-80a864c37452"),
        null,
        LorittaEmojis.PantufaHead,
        {
            if (it?.idLong == Constants.SPARKLYPOWER_GUILD_ID) {
                0.025
            } else {
                0.010
            }
        },
        1
    )

    private val gabriela = ReactionSet(
        UUID.fromString("fe987e10-f9f6-4e43-a1d0-6f3f8bc8c1af"),
        null,
        LorittaEmojis.GabrielaHead,
        {
            if (it?.idLong != Constants.PORTUGUESE_SUPPORT_GUILD_ID && it?.idLong != Constants.SPARKLYPOWER_GUILD_ID) {
                0.025
            } else {
                0.010
            }
        },
        1
    )

    private val gessy = ReactionSet(
        UUID.fromString("09bfdb6d-1a69-4439-b881-33c05bb3e25e"),
        null,
        LorittaEmojis.GessyHead,
        {
            if (it?.idLong != Constants.PORTUGUESE_SUPPORT_GUILD_ID && it?.idLong != Constants.SPARKLYPOWER_GUILD_ID) {
                0.025
            } else {
                0.010
            }
        },
        1
    )

    private val pumpkin = LorittaEmojiReference.UnicodeEmoji("\uD83C\uDF83")

    override val reactionSets = listOf(
        candy1,
        candy2,
        candy3,
        candy4,
        loritta,
        pantufa,
        gabriela,
        gessy
    )

    override val rewards = listOf(
        ReactionEventReward.BadgeReward(10, false),
        ReactionEventReward.SonhosReward(100, false, 50_000),
        ReactionEventReward.SonhosReward(125, false, 150_000),
        ReactionEventReward.SonhosReward(150, false, 300_000),
        ReactionEventReward.SonhosReward(175, false, 500_000),
        ReactionEventReward.SonhosReward(200, false, 600_000),
        ReactionEventReward.SonhosReward(225, false, 700_000),
        ReactionEventReward.SonhosReward(250, false, 800_000),
        ReactionEventReward.SonhosReward(275, false, 900_000),
        ReactionEventReward.SonhosReward(300, false, 1_000_000),
        ReactionEventReward.BadgeReward(300, false),
    )

    override fun createEventTitle(i18nContext: I18nContext) = i18nContext.get(I18nKeysData.ReactionEvents.Event.Halloween2025.EventName)

    override fun createJoinMessage(context: UnleashedContext): InlineMessage<*>.() -> (Unit) = {
        val items = reactionSets.joinToString("") {
            val emote = context.loritta.emojiManager.get(it.reaction)

            when (emote) {
                is DiscordEmote -> emote.asMentionWithGenericName
                is UnicodeEmote -> emote.asMention
            }
        }

        styled(
            "O Halloween está chegando! E advinha quem esqueceu de comprar doces DE NOVO para o Halloween...",
            Emotes.LoriHi
        )

        styled(
            "Eu acho que esse pessoal deveria colocar os eventos importantes em um calendário para não esquecerem deles. Mas elas esquecerem de datas comemorativas é bom, né? Já que você é recompensado com sonhos.",
            Emotes.LoriBonk
        )

        styled(
            "Os itens $items estão espalhados pelo chat, aparecendo como reações nas conversas.",
            Emotes.LoriHm
        )

        styled(
            "Ao encontrar algum item, reaja nele para coletá-lo. Mas seja rápido, pois os itens expiram! Por que eles expiram? Pois elas são chiques e não querem itens velhos. Ninguém gosta de comer aquele Bis que ficou murcho.",
            Emotes.LoriWow
        )

        styled(
            "Como a Loritta é estrelinha, os itens só aparecem em servidores que possuem mais de mil membros! E tem algo especial nelas... A Loritta ${context.loritta.emojiManager.get(loritta.reaction).toJDA().formatted} tem mais chance de aparecer no servidor do Apartamento da Loritta, a Pantufa ${context.loritta.emojiManager.get(pantufa.reaction).toJDA().formatted} tem mais chance de aparecer no SparklyPower, e a Gabriela ${context.loritta.emojiManager.get(gabriela.reaction).toJDA().formatted} e o Gessy ${context.loritta.emojiManager.get(gessy.reaction).toJDA().formatted} tem mais chance de aparecer em outros servidores!",
            Emotes.LoriHmpf
        )

        styled(
            "Após coletar itens, você precisa criar as cestas de doces usando ${context.loritta.commandMentions.eventInventory} e, com as cestas, você receberá recompensas!",
            context.loritta.emojiManager.get(pantufa.reaction).toJDA().formatted
        )

        styled(
            "Se você quer saber mais sobre o evento, entre no servidor da comunidade da Loritta! <https://discord.gg/loritta>",
            Emotes.LoriHeart
        )
    }

    override fun getCurrentActiveCraft(userId: Long, alreadyCraftedQuantity: Long): Map<UUID, Int> {
        val rand = Random(userId + alreadyCraftedQuantity)
        val count = 4

        val expectedSum = count + (alreadyCraftedQuantity.toInt() / 30)
        val randomCounts = generateNormalizedIntegers(rand, count, expectedSum)

        val randCharacter = rand.nextInt(4)

        val reactionSets = mutableMapOf(
            this.candy1.reactionSetId to randomCounts[0],
            this.candy2.reactionSetId to randomCounts[1],
            this.candy3.reactionSetId to randomCounts[2],
            this.candy4.reactionSetId to randomCounts[3]
        )

        val value = when (randCharacter) {
            0 -> this.loritta
            1 -> this.pantufa
            2 -> this.gabriela
            3 -> this.gessy
            else -> error("Unknown character with ID $randCharacter")
        }

        reactionSets[value.reactionSetId] = 1

        return reactionSets
    }

    override fun createSonhosRewardTransactionMessage(
        i18nContext: I18nContext,
        sonhos: Long,
        craftedCount: Int
    ) = i18nContext.get(I18nKeysData.Commands.Command.Transactions.Types.Events.Halloween2025(sonhos, craftedCount))

    override fun createCraftItemButtonMessage(i18nContext: I18nContext) = TextAndEmoji(
        i18nContext.get(I18nKeysData.ReactionEvents.Event.Halloween2025.CraftItem),
        pumpkin
    )

    override fun createHowManyCraftedItemsYouHaveMessage(i18nContext: I18nContext, craftedCount: Long, commandMention: String) = i18nContext.get(I18nKeysData.ReactionEvents.Event.Halloween2025.CurrentlyYouHave(craftedCount, commandMention))

    override fun createItemsInYourInventoryMessage(i18nContext: I18nContext) = i18nContext.get(I18nKeysData.ReactionEvents.Event.Halloween2025.ItemsInYourInventory)

    override fun createYourNextCraftIngredientsAreMessage(i18nContext: I18nContext) = i18nContext.get(I18nKeysData.ReactionEvents.Event.Halloween2025.YourNextCraftIngredientsAre)

    override fun createYouDontHaveEnoughItemsMessage(i18nContext: I18nContext) = i18nContext.get(I18nKeysData.ReactionEvents.Event.Halloween2025.YouDontHaveEnoughItems)

    override fun createYouCraftedAItemMessage(i18nContext: I18nContext, combo: Int): TextAndEmoji {
        return if (combo >= 3) {
            TextAndEmoji(
                i18nContext.get(I18nKeysData.ReactionEvents.Event.Halloween2025.YouCreatedAnItemCombo(combo)),
                LorittaEmojiReference.UnicodeEmoji("\uD83D\uDD25"),
            )
        } else {
            TextAndEmoji(
                i18nContext.get(I18nKeysData.ReactionEvents.Event.Halloween2025.YouCraftedAnItem),
                pumpkin
            )
        }
    }

    override fun createShortCraftedItemMessage(i18nContext: I18nContext, quantity: Int) = i18nContext.get(I18nKeysData.ReactionEvents.Event.Halloween2025.ShortCraftedItem(quantity))

    override fun createCraftedXItemsMessage(loritta: LorittaBot, i18nContext: I18nContext, quantity: Long, commandMention: String) = i18nContext.get(I18nKeysData.ReactionEvents.Event.Halloween2025.YouCraftedXItems(quantity, loritta.emojiManager.get(pumpkin).toJDA().formatted, commandMention))
}