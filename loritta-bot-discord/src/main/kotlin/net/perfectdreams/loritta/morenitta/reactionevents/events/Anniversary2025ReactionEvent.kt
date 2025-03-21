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

object Anniversary2025ReactionEvent : ReactionEvent() {
    override val internalId: String = "Anniversary2025"
    override val startsAt: Instant = ZonedDateTime.of(2025, 3, 25, 21, 0, 0, 0, Constants.LORITTA_TIMEZONE).toInstant()
    override val endsAt: Instant = ZonedDateTime.of(2025, 3, 30, 0, 0, 0, 0, Constants.LORITTA_TIMEZONE).toInstant()

    private val toy1 = ReactionSet(
        UUID.fromString("f218f80e-3f55-4276-a1a2-ad4f103d1de5"),
        null,
        LorittaEmojis.CirnoFumo,
        { 0.088 },
        1
    )

    private val toy2 = ReactionSet(
        UUID.fromString("76479544-696b-4594-966f-10064258bf7d"),
        null,
        LorittaEmojis.RalseiPlush,
        { 0.088 },
        1
    )

    private val toy3 = ReactionSet(
        UUID.fromString("8a629e26-1955-4497-9a47-a7db8ceaf61b"),
        null,
        LorittaEmojis.PomniPlush,
        { 0.088 },
        1
    )

    private val toy4 = ReactionSet(
        UUID.fromString("4e9ec4fa-bce2-46fa-947a-ff7b47b97bb2"),
        null,
        LorittaEmojis.TailsPlush,
        { 0.088 },
        1
    )

    private val pantufa = ReactionSet(
        UUID.fromString("55b0360b-4063-42eb-89eb-77054dc18180"),
        null,
        LorittaEmojis.PantufaHead,
        {
            if (it?.idLong == Constants.SPARKLYPOWER_GUILD_ID) {
                0.012
            } else {
                0.006
            }
        },
        1
    )

    private val gabriela = ReactionSet(
        UUID.fromString("708810ff-8389-4509-b1e4-201d29d3606b"),
        null,
        LorittaEmojis.GabrielaHead,
        {
            if (it?.idLong != Constants.PORTUGUESE_SUPPORT_GUILD_ID && it?.idLong != Constants.SPARKLYPOWER_GUILD_ID) {
                0.012
            } else {
                0.006
            }
        },
        1
    )

    private val gessy = ReactionSet(
        UUID.fromString("1187db6e-9b5f-4e5f-9a8d-a23104cd5b29"),
        null,
        LorittaEmojis.GessyHead,
        {
            if (it?.idLong != Constants.PORTUGUESE_SUPPORT_GUILD_ID && it?.idLong != Constants.SPARKLYPOWER_GUILD_ID) {
                0.012
            } else {
                0.006
            }
        },
        1
    )

    private val gift = LorittaEmojiReference.UnicodeEmoji("\uD83C\uDF81")

    override val reactionSets = listOf(
        toy1,
        toy2,
        toy3,
        toy4,
        pantufa,
        gabriela,
        gessy
    )

    override val rewards = listOf(
        ReactionEventReward.BadgeReward(10, false),
        ReactionEventReward.SonhosReward(100, false, 50_000),
        ReactionEventReward.SonhosReward(150, false, 150_000),
        ReactionEventReward.SonhosReward(200, false, 300_000),
        ReactionEventReward.SonhosReward(250, false, 500_000),
        ReactionEventReward.SonhosReward(300, false, 600_000),
        ReactionEventReward.SonhosReward(350, false, 700_000),
        ReactionEventReward.SonhosReward(400, false, 800_000),
        ReactionEventReward.SonhosReward(450, false, 900_000),
        ReactionEventReward.SonhosReward(500, false, 1_000_000),
        ReactionEventReward.BadgeReward(500, false),
    )

    override fun createEventTitle(i18nContext: I18nContext) = i18nContext.get(I18nKeysData.ReactionEvents.Event.Anniversary2025.EventName)

    override fun createJoinMessage(context: UnleashedContext): InlineMessage<*>.() -> (Unit) = {
        val items = reactionSets.joinToString("") {
            val emote = context.loritta.emojiManager.get(it.reaction)

            when (emote) {
                is DiscordEmote -> emote.asMentionWithGenericName
                is UnicodeEmote -> emote.asMention
            }
        }

        styled(
            "O Aniversário da Loritta está chegando!",
            Emotes.LoriHi
        )

        styled("Calma, ele está chegando? M-mas a gente nem comprou presentes para ela! E agora?!??!")

        styled("Enquanto os três se reuniam na casa da Gabriela para decidir o que iriam dar para a Loritta, a Pantufa deu a ideia de comprar pelúcias para a Loritta, já que uma vez a Loritta disse que queria mais pelúcias para enfeitar o quarto dela. A Gabriela disse que ela acha que dar pelúcias seria algo infantil e, enquanto a Gabriela soltava essa pérola, a Pantufa e o Gessy percebem que atrás dela tem uma parede cheia de Funko Pop... A Gabriela percebeu o olhar atrevido dos dois, e decidiu seguir a ideia da Pantufa.")

        styled(
            "Os itens $items estão espalhados pelo chat, aparecendo como reações nas conversas.",
            Emotes.LoriHm
        )

        styled(
            "Ao encontrar algum item, reaja nele para coletá-lo. Mas seja rápido, pois os itens expiram! Por que eles expiram? Pois elas são chiques e não querem itens velhos.",
            Emotes.LoriWow
        )

        styled(
            "Como a Loritta é estrelinha, os itens só aparecem em servidores que possuem mais de mil membros! E tem algo especial nelas... A Gabriela ${context.loritta.emojiManager.get(gabriela.reaction).toJDA().formatted} tem mais chance de aparecer no servidor do Apartamento da Loritta, a Pantufa ${context.loritta.emojiManager.get(pantufa.reaction).toJDA().formatted} tem mais chance de aparecer no SparklyPower, e o Gessy ${context.loritta.emojiManager.get(gessy.reaction).toJDA().formatted} tem mais chance de aparecer em outros servidores!",
            Emotes.LoriHmpf
        )

        styled(
            "Após coletar itens, você precisa criar os presentes usando ${context.loritta.commandMentions.eventInventory} e, com os presentes, você receberá recompensas!",
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

        val randCharacter = rand.nextInt(3)

        val reactionSets = mutableMapOf(
            this.toy1.reactionSetId to randomCounts[0],
            this.toy2.reactionSetId to randomCounts[1],
            this.toy3.reactionSetId to randomCounts[2],
            this.toy4.reactionSetId to randomCounts[3]
        )

        val value = when (randCharacter) {
            0 -> this.pantufa
            1 -> this.gabriela
            2 -> this.gessy
            else -> error("Unknown character with ID $randCharacter")
        }

        reactionSets[value.reactionSetId] = 1

        return reactionSets
    }

    override fun createSonhosRewardTransactionMessage(
        i18nContext: I18nContext,
        sonhos: Long,
        craftedCount: Int
    ) = i18nContext.get(I18nKeysData.Commands.Command.Transactions.Types.Events.Anniversary2025(sonhos, craftedCount))

    override fun createCraftItemButtonMessage(i18nContext: I18nContext) = TextAndEmoji(
        i18nContext.get(I18nKeysData.ReactionEvents.Event.Anniversary2025.CraftItem),
        gift
    )

    override fun createHowManyCraftedItemsYouHaveMessage(i18nContext: I18nContext, craftedCount: Long, commandMention: String) = i18nContext.get(I18nKeysData.ReactionEvents.Event.Anniversary2025.CurrentlyYouHave(craftedCount, commandMention))

    override fun createItemsInYourInventoryMessage(i18nContext: I18nContext) = i18nContext.get(I18nKeysData.ReactionEvents.Event.Anniversary2025.ItemsInYourInventory)

    override fun createYourNextCraftIngredientsAreMessage(i18nContext: I18nContext) = i18nContext.get(I18nKeysData.ReactionEvents.Event.Anniversary2025.YourNextCraftIngredientsAre)

    override fun createYouDontHaveEnoughItemsMessage(i18nContext: I18nContext) = i18nContext.get(I18nKeysData.ReactionEvents.Event.Anniversary2025.YouDontHaveEnoughItems)

    override fun createYouCraftedAItemMessage(i18nContext: I18nContext, combo: Int): TextAndEmoji {
        return if (combo >= 3) {
            TextAndEmoji(
                i18nContext.get(I18nKeysData.ReactionEvents.Event.Anniversary2025.YouCreatedAnItemCombo(combo)),
                LorittaEmojiReference.UnicodeEmoji("\uD83D\uDD25"),
            )
        } else {
            TextAndEmoji(
                i18nContext.get(I18nKeysData.ReactionEvents.Event.Anniversary2025.YouCraftedAnItem),
                gift
            )
        }
    }

    override fun createShortCraftedItemMessage(i18nContext: I18nContext, quantity: Int) = i18nContext.get(I18nKeysData.ReactionEvents.Event.Anniversary2025.ShortCraftedItem(quantity))

    override fun createCraftedXItemsMessage(loritta: LorittaBot, i18nContext: I18nContext, quantity: Long, commandMention: String) = i18nContext.get(I18nKeysData.ReactionEvents.Event.Anniversary2025.YouCraftedXItems(quantity, loritta.emojiManager.get(gift).toJDA().formatted, commandMention))
}