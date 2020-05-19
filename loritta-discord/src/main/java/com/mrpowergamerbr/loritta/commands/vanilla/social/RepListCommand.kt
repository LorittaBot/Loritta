package com.mrpowergamerbr.loritta.commands.vanilla.social

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.dao.Reputation
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.tables.Reputations
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import net.dv8tion.jda.api.EmbedBuilder
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.tables.SonhosTransaction
import net.perfectdreams.loritta.utils.Emotes
import net.perfectdreams.loritta.utils.SonhosPaymentReason
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant
import java.time.ZoneId

class RepListCommand : AbstractCommand("replist", listOf("reps"), CommandCategory.SOCIAL) {
	override fun getDescription(locale: LegacyBaseLocale): String {
		return locale.toNewLocale()["commands.social.repList.description"]
	}

	override fun canUseInPrivateChannel(): Boolean {
		return false
	}

	override suspend fun run(context: CommandContext, locale: LegacyBaseLocale) {
		val user = context.getUserAt(0) ?: context.userHandle

		val reputations = transaction(Databases.loritta) {
			Reputations.select {
				Reputations.givenById eq user.idLong or (Reputations.receivedById eq user.idLong)
			}.orderBy(Reputations.receivedAt, SortOrder.DESC)
					.limit(20)
					.toMutableList()
		}

		val description = buildString {
			if(reputations.size == 0){
				this.append(context.locale["commands.social.repList.noReps"])
			}
			else {
				val totalReputationReceived = reputations.filter {	it[Reputations.receivedById] == user.idLong	}.size;
				val totalReputationGiven = reputations.filter {it[Reputations.givenById] == user.idLong}.size;
				this.append(context.locale["commands.social.repList.reputationsTotalDescription", totalReputationReceived, totalReputationGiven])
				this.append("\n")
				this.append("\n")

				for (reputation in reputations) {
					val receivedReputation = reputation[Reputations.receivedById] == user.idLong

					val givenAtTime = Instant.ofEpochMilli(reputation[Reputations.receivedAt])
							.atZone(ZoneId.systemDefault())
					val year = givenAtTime.year
					val month = givenAtTime.monthValue.toString().padStart(2, '0')
					val day = givenAtTime.dayOfMonth.toString().padStart(2, '0')
					val hour = givenAtTime.hour.toString().padStart(2, '0')
					val minute = givenAtTime.minute.toString().padStart(2, '0')
					this.append("`[$day/$month/$year $hour:$minute]` ")

					val emoji = if (receivedReputation)
						"\uD83D\uDCE5"
					else
						"\uD83D\uDCE4"
					this.append(emoji)
					this.append(" ")

					val receivedByUser = if (receivedReputation) {
						lorittaShards.retrieveUserById(reputation[Reputations.givenById])
					} else {
						lorittaShards.retrieveUserById(reputation[Reputations.receivedById])
					}

					val name = (receivedByUser?.name + "#" + receivedByUser?.discriminator)
					val content = if(reputation[Reputations.content] != null)
						"`${reputation[Reputations.content]}`"
					else
						null;

					val receivedByLoritta = reputation[Reputations.givenById] == loritta.discordConfig.discord.clientId.toLong()
					if(receivedByLoritta){
						this.append(context.locale["commands.social.repList.receivedReputationByLoritta", "`${user.name + "#" + user.discriminator}`"])
					}
					else{
						if(receivedReputation){
							if(content == null){
								this.append(context.locale["commands.social.repList.receivedReputation", "`${name}`"])
							}
							else{
								this.append(context.locale["commands.social.repList.receivedReputationWithContent", "`${name}`", content])
							}
						}
						else{
							if(content == null){
								this.append(context.locale["commands.social.repList.sentReputation", "`${name}`"])
							}
							else{
								this.append(context.locale["commands.social.repList.sentReputationWithContent", "`${name}`", content])
							}
						}
					}
					this.append("\n")
				}
			}
		}

		val embed = EmbedBuilder()
				.setTitle(
						"${Emotes.LORI_RICH} " +
								if (user != context.userHandle)
									context.locale["commands.social.repList.otherUserRepList", user.asTag]
								else
									context.locale["commands.social.repList.title"]
				)
				.setColor(Constants.LORITTA_AQUA)
				.setDescription(description)

		context.sendMessage(context.getAsMention(true), embed.build())
	}
}