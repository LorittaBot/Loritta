package net.perfectdreams.loritta.morenitta.interactions.vanilla.economy.drop

import net.dv8tion.jda.api.entities.emoji.Emoji
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.pudding.tables.DropChatChoices
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.utils.extensions.toJDA

sealed interface DropChatChoiceVariant {
    fun getDropTitle(i18nContext: I18nContext): String
    fun getDropEndedTitle(i18nContext: I18nContext): String
    fun getInstructionText(i18nContext: I18nContext, creatorMention: String): String
    fun getCorrectChoiceRevealText(i18nContext: I18nContext, correctChoice: String, creatorMention: String): String
    fun getExtraContextText(i18nContext: I18nContext): String?
    fun getChoiceEmoji(choice: String): Emoji?
    val variantType: DropChatChoices.DropChatChoiceVariantType

    data object Generic : DropChatChoiceVariant {
        override fun getDropTitle(i18nContext: I18nContext) = i18nContext.get(I18nKeysData.Commands.Command.Drop.Choice.SonhosDrop)
        override fun getDropEndedTitle(i18nContext: I18nContext) = i18nContext.get(I18nKeysData.Commands.Command.Drop.Choice.SonhosDropHasEnded)
        override fun getInstructionText(i18nContext: I18nContext, creatorMention: String) = i18nContext.get(I18nKeysData.Commands.Command.Drop.Choice.MakeYourChoice)
        override fun getCorrectChoiceRevealText(i18nContext: I18nContext, correctChoice: String, creatorMention: String) = i18nContext.get(I18nKeysData.Commands.Command.Drop.Choice.CorrectChoiceWas(correctChoice))
        override fun getExtraContextText(i18nContext: I18nContext): String? = null
        override fun getChoiceEmoji(choice: String): Emoji? = null
        override val variantType = DropChatChoices.DropChatChoiceVariantType.GENERIC
    }

    data class EvenOdd(val number: Long) : DropChatChoiceVariant {
        override fun getDropTitle(i18nContext: I18nContext) = i18nContext.get(I18nKeysData.Commands.Command.Drop.EvenOdd.SonhosDrop)
        override fun getDropEndedTitle(i18nContext: I18nContext) = i18nContext.get(I18nKeysData.Commands.Command.Drop.EvenOdd.SonhosDropHasEnded)
        override fun getInstructionText(i18nContext: I18nContext, creatorMention: String) = i18nContext.get(I18nKeysData.Commands.Command.Drop.EvenOdd.MakeYourChoice(creatorMention))
        override fun getCorrectChoiceRevealText(i18nContext: I18nContext, correctChoice: String, creatorMention: String) = i18nContext.get(I18nKeysData.Commands.Command.Drop.EvenOdd.CorrectChoiceWas(number, correctChoice))
        override fun getExtraContextText(i18nContext: I18nContext) = i18nContext.get(I18nKeysData.Commands.Command.Drop.EvenOdd.TheNumberIs(number))
        override fun getChoiceEmoji(choice: String): Emoji? = null
        override val variantType = DropChatChoices.DropChatChoiceVariantType.EVEN_ODD
    }

    data class Jankenpon(val creatorChoice: String) : DropChatChoiceVariant {
        override fun getDropTitle(i18nContext: I18nContext) = i18nContext.get(I18nKeysData.Commands.Command.Drop.Jankenpon.SonhosDrop)
        override fun getDropEndedTitle(i18nContext: I18nContext) = i18nContext.get(I18nKeysData.Commands.Command.Drop.Jankenpon.SonhosDropHasEnded)
        override fun getInstructionText(i18nContext: I18nContext, creatorMention: String) = i18nContext.get(I18nKeysData.Commands.Command.Drop.Jankenpon.MakeYourChoice(creatorMention))
        override fun getCorrectChoiceRevealText(i18nContext: I18nContext, correctChoice: String, creatorMention: String) = i18nContext.get(I18nKeysData.Commands.Command.Drop.Jankenpon.CorrectChoiceWas(creatorMention, creatorChoice, correctChoice))
        override fun getExtraContextText(i18nContext: I18nContext): String? = null
        override fun getChoiceEmoji(choice: String): Emoji? = when (choice) {
            "Pedra" -> Emotes.Rock.toJDA()
            "Papel" -> Emotes.Newspaper.toJDA()
            "Tesoura" -> Emotes.Scissors.toJDA()
            else -> null
        }
        override val variantType = DropChatChoices.DropChatChoiceVariantType.JANKENPON
    }
}
