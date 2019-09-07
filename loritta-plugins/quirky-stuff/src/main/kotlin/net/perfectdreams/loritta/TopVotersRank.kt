package net.perfectdreams.loritta

import com.mrpowergamerbr.loritta.LorittaLauncher
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.extensions.await
import com.mrpowergamerbr.loritta.utils.extensions.editMessageIfContentWasChanged
import com.mrpowergamerbr.loritta.utils.lorittaShards
import com.mrpowergamerbr.loritta.utils.stripCodeMarks
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mu.KotlinLogging
import net.perfectdreams.loritta.tables.BotVotes
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.count
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.sum
import org.jetbrains.exposed.sql.transactions.transaction

class TopVotersRank(val m: QuirkyStuff, val config: QuirkyConfig) {
	companion object {
		private val logger = KotlinLogging.logger {}
	}

	var task: Job? = null

	fun start() {
		logger.info { "Starting Top Voters Rank Task..." }

		task = GlobalScope.launch(LorittaLauncher.loritta.coroutineDispatcher) {
			while (true) {
				try {
					val guild = lorittaShards.getGuildById(Constants.PORTUGUESE_SUPPORT_GUILD_ID)

					if (guild != null) {
						val role1 = guild.getRoleById(config.topVotersRank.topRole1)!!
						val role2 = guild.getRoleById(config.topVotersRank.topRole2)!!
						val role3 = guild.getRoleById(config.topVotersRank.topRole3)!!

						val userIdCount = BotVotes.userId.count()
						val votedAtSum = BotVotes.votedAt.sum()

						val topVoterUsers = transaction(Databases.loritta) {
							BotVotes.slice(BotVotes.userId, userIdCount, votedAtSum).selectAll()
									.groupBy(BotVotes.userId)
									.orderBy(
											userIdCount to SortOrder.DESC,
											votedAtSum to SortOrder.ASC
									)
									.limit(15)
									.toMutableList()
						}

						val message = StringBuilder()

						val topMoneyAsDisplayEntry = "${topVoterUsers[0][userIdCount]} votos"

						val currentTop1 = guild.getMembersWithRoles(role1).firstOrNull()
						val currentTop2 = guild.getMembersWithRoles(role2).firstOrNull()
						val currentTop3 = guild.getMembersWithRoles(role3).firstOrNull()

						// Remover todos que não merece mais
						if (currentTop1 != null && currentTop1.idLong != topVoterUsers.getOrNull(0)?.get(BotVotes.userId)) {
							guild.removeRoleFromMember(currentTop1, role1).await()
						}
						if (currentTop2 != null && currentTop2.idLong != topVoterUsers.getOrNull(1)?.get(BotVotes.userId)) {
							guild.removeRoleFromMember(currentTop2, role2).await()
						}
						if (currentTop3 != null && currentTop3.idLong != topVoterUsers.getOrNull(2)?.get(BotVotes.userId)) {
							guild.removeRoleFromMember(currentTop3, role3).await()
						}

						topVoterUsers.forEachIndexed { index, entry ->
							val userId = entry[BotVotes.userId]
							val user = guild.getMemberById(entry[BotVotes.userId])

							if (user != null) {
								val newRoles = user.roles.toMutableList()

								newRoles.remove(role1)
								newRoles.remove(role2)
								newRoles.remove(role3)

								if (index == 0) {
									if (!newRoles.contains(role1)) {
										newRoles.add(role1)
									}
								} else if (index == 1) {
									if (!newRoles.contains(role2)) {
										newRoles.add(role2)
									}
								} else if (index == 2) {
									if (!newRoles.contains(role3)) {
										newRoles.add(role3)
									}
								}

								if (!(newRoles.containsAll(user.roles) && user.roles.containsAll(newRoles)))
									guild.modifyMemberRoles(user, newRoles).await()
							}

							val rankEmoji = when (index) {
								0 -> "<:nothing:592370648031166524>\uD83E\uDD47"
								1 -> "<:nothing:592370648031166524>\uD83E\uDD48"
								2 -> "<:nothing:592370648031166524>\uD83E\uDD49"
								3 -> "<:nothing:592370648031166524><:kawaii_four:542823233448050688>"
								4 -> "<:nothing:592370648031166524><a:kawaii_five:542823247826386997>"
								5 -> "<:nothing:592370648031166524><:kawaii_six:542823279858286592>"
								6 -> "<:nothing:592370648031166524><a:kawaii_seven:542823307414601734>"
								7 -> "<:nothing:592370648031166524><:kawaii_eight:542823334652411936>"
								8 -> "<:nothing:592370648031166524><:kawaii_nine:542823384917213200>"
								9 -> "<:kawaii_one:542823112220344350><a:kawaii_zero:542823087649849414>"
								10 -> "<:kawaii_one:542823112220344350><:kawaii_one:542823112220344350>"
								11 -> "<:kawaii_one:542823112220344350><a:kawaii_two:542823168465829907>"
								12 -> "<:kawaii_one:542823112220344350><a:kawaii_three:542823194445348885>"
								13 -> "<:kawaii_one:542823112220344350><:kawaii_four:542823233448050688>"
								14 -> "<:kawaii_one:542823112220344350><a:kawaii_five:542823247826386997>"
								15 -> "<:kawaii_one:542823112220344350><:kawaii_six:542823279858286592>"
								16 -> "<:kawaii_one:542823112220344350><a:kawaii_seven:542823307414601734>"
								17 -> "<:kawaii_one:542823112220344350><:kawaii_eight:542823334652411936>"
								18 -> "<:kawaii_one:542823112220344350><:kawaii_nine:542823384917213200>"
								19 -> "<a:kawaii_two:542823168465829907><a:kawaii_zero:542823087649849414>"
								else -> RuntimeException("There is >$index entries, but we only support up to 19!")
							}

							message.append(rankEmoji)
							message.append(" • ")
							val moneyDisplay = "${entry[userIdCount]} votos"
							message.append("`${moneyDisplay.padEnd(topMoneyAsDisplayEntry.length, ' ')}` - ")
							message.append("**")
							if (user != null) {
								message.append(user.asMention)
							} else {
								val globalUser = lorittaShards.getUserById(userId)
								if (globalUser != null) {
									message.append("${globalUser.name.stripCodeMarks()}#${globalUser.discriminator}")
								} else {
									message.append(userId.toString())
								}
							}
							message.append("**")
							message.append("\n")
						}

						for (channelId in config.topVotersRank.channels) {
							val channel = lorittaShards.getTextChannelById(channelId.toString())

							if (channel != null) {
								val loriMessage = channel.history.retrievePast(1)
										.await()
										.firstOrNull()

								if (loriMessage?.author?.id == LorittaLauncher.loritta.discordConfig.discord.clientId) {
									loriMessage.editMessageIfContentWasChanged(message.toString())
								}
							}
						}
					}
				} catch (e: Exception) {
					logger.warn(e) { "Error while updating top voters rank task!" }
				}

				delay(60_000)
			}
		}
	}
}