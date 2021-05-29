package com.mrpowergamerbr.loritta.commands.vanilla.social

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.extensions.isEmote
import net.perfectdreams.loritta.common.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.locale.Gender
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.onReactionAddByAuthor
import net.dv8tion.jda.api.EmbedBuilder
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.api.messages.LorittaReply

class GenderCommand : AbstractCommand("gender", listOf("gênero", "genero"), CommandCategory.SOCIAL) {
    override fun getDescriptionKey() = LocaleKeyData("commands.command.gender.description")

    override suspend fun run(context: CommandContext, locale: BaseLocale) {
        val embed = EmbedBuilder()
                .setTitle(locale["commands.command.gender.whatAreYou"])
                .setDescription(locale["commands.command.gender.whyShouldYouSelect"])
                .build()


        val message = context.sendMessage(embed)

        message.addReaction("male:384048518853296128").queue()
        message.addReaction("female:384048518337265665").queue()
        message.addReaction("❓").queue()

        message.onReactionAddByAuthor(context) {
            message.delete().queue()

            if (it.reactionEmote.id == "384048518853296128") {
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


            if (it.reactionEmote.id == "384048518337265665") {
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

            if (it.reactionEmote.isEmote("❓")) {
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