package net.perfectdreams.loritta.morenitta.commands.vanilla.social

import net.perfectdreams.loritta.morenitta.commands.AbstractCommand
import net.perfectdreams.loritta.morenitta.commands.CommandContext
import net.perfectdreams.loritta.morenitta.utils.extensions.isEmote
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.perfectdreams.loritta.morenitta.utils.onReactionAddByAuthor
import net.dv8tion.jda.api.EmbedBuilder
import net.perfectdreams.loritta.common.utils.Gender
import net.perfectdreams.loritta.morenitta.messages.LorittaReply
import net.perfectdreams.loritta.morenitta.utils.OutdatedCommandUtils
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.utils.extensions.addReaction

class GenderCommand(loritta: LorittaBot) : AbstractCommand(loritta, "gender", listOf("gênero", "genero"), net.perfectdreams.loritta.common.commands.CommandCategory.SOCIAL) {
    override fun getDescriptionKey() = LocaleKeyData("commands.command.gender.description")

    override suspend fun run(context: CommandContext, locale: BaseLocale) {
        OutdatedCommandUtils.sendOutdatedCommandMessage(context, locale, "gender")

        val embed = EmbedBuilder()
                .setTitle(locale["commands.command.gender.whatAreYou"])
                .setDescription(locale["commands.command.gender.whyShouldYouSelect"])
                .build()


        val message = context.sendMessageEmbeds(embed)

        message.addReaction("male:384048518853296128").queue()
        message.addReaction("female:384048518337265665").queue()
        message.addReaction("❓").queue()

        message.onReactionAddByAuthor(context) {
            message.delete().queue()

            if (it.emoji.asCustom().id == "384048518853296128") {
                loritta.newSuspendedTransaction {
                    context.lorittaUser.profile.settings.gender = Gender.MALE
                }

                context.reply(
						LorittaReply(
								locale["commands.command.gender.successfullyChanged"],
								"\uD83C\uDF89"
						)
				)
            }

            if (it.emoji.asCustom().id == "384048518337265665") {
                loritta.newSuspendedTransaction {
                    context.lorittaUser.profile.settings.gender = Gender.FEMALE
                }

                context.reply(
						LorittaReply(
								locale["commands.command.gender.successfullyChanged"],
								"\uD83C\uDF89"
						)
				)
            }

            if (it.emoji.isEmote("❓")) {
                loritta.newSuspendedTransaction {
                    context.lorittaUser.profile.settings.gender = Gender.UNKNOWN
                }

                context.reply(
						LorittaReply(
								locale["commands.command.gender.successfullyChanged"],
								"\uD83C\uDF89"
						)
				)
            }
        }
    }
}