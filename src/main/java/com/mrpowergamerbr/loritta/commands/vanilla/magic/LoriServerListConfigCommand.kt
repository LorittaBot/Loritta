package com.mrpowergamerbr.loritta.commands.vanilla.magic

import com.mongodb.client.model.Filters
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.tables.Profiles
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.extensions.humanize
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.networkbans.NetworkBanEntry
import com.mrpowergamerbr.loritta.utils.networkbans.NetworkBanType
import org.apache.commons.lang3.RandomStringUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

class LoriServerListConfigCommand : AbstractCommand("lslc", category = CommandCategory.MAGIC, onlyOwner = true) {
	override fun getDescription(locale: BaseLocale): String {
		return "Configura servidores na Lori's Server List"
	}

	override suspend fun run(context: CommandContext,locale: BaseLocale) {
		val arg0 = context.rawArgs.getOrNull(0)
		val arg1 = context.rawArgs.getOrNull(1)
		val arg2 = context.rawArgs.getOrNull(2)
		val arg3 = context.rawArgs.getOrNull(3)

		if (arg0 == "set_dreams" && arg1 != null && arg2 != null) {
			val user = context.getUserAt(2)!!
			transaction(Databases.loritta) {
				Profiles.update({ Profiles.id eq user.idLong }) {
					it[money] = arg1.toDouble()
				}
			}

			context.reply(
					LoriReply(
							"Sonhos de ${user.asMention} foram editados com sucesso!"
					)
			)
			return
		}

		if (arg0 == "commit_bans") {
			val replies = mutableListOf<LoriReply>()
			replies.add(
					LoriReply(
							"**Lista de usuários a serem banidos **GLOBALMENTE**...",
							Emotes.DISCORD_ONLINE
					)
			)

			loritta.networkBanManager.notVerifiedEntries.forEach {
				if (replies.sumBy { it.build(context).length } >= 2000) {
					context.reply(*replies.toTypedArray())
					replies.clear()
				}

				val user = lorittaShards.getUserById(it.id)!!

				val typeEmote = when {
					user.isBot -> Emotes.DISCORD_BOT_TAG
					else -> Emotes.DISCORD_WUMPUS_BASIC
				}

				val mutualGuilds = lorittaShards.getMutualGuilds(user)
				val serverConfigs = loritta.serversColl.find(
						Filters.and(
								Filters.`in`("_id", mutualGuilds.map { it.id }),
								Filters.eq("moderationConfig.useLorittaBansNetwork", true)
						)
				).toMutableList()

				replies.add(
						LoriReply(
								"$typeEmote `${user.name.stripCodeMarks()}#${user.discriminator}` (${user.id}/${it.type.name}) — ${mutualGuilds.size} servidores compartilhados (${serverConfigs.size} com os bans globais ativados)",
								mentionUser = false
						)
				)
			}

			context.reply(*replies.toTypedArray())
			replies.clear()

			val message = context.reply(
					LoriReply(
							"Veja se tudo está correto, caso esteja, veja de novo e caso realmente esteja certo, aperte no ✅",
							Emotes.DISCORD_DO_NOT_DISTURB
					)
			)

			message.addReaction("✅").queue()
			message.addReaction(Constants.ERROR).queue()

			message.onReactionAddByAuthor(context) {
				if (it.reactionEmote.name == "✅") {
					loritta.networkBanManager.notVerifiedEntries.forEach {
						loritta.networkBanManager.addBanEntry(it)
					}
					loritta.networkBanManager.notVerifiedEntries.clear()
					context.reply(
							LoriReply(
									"Todos os usuários da lista foram adicionados na lista de bans globais, yay!"
							)
					)
				} else {
					loritta.networkBanManager.notVerifiedEntries.clear()
					context.reply(
							LoriReply(
									"A lista de bans não verificados foi limpa, whoosh!"
							)
					)
				}
			}
			return
		}

		if (arg0 == "network_ban" && arg1 != null && arg2 != null && arg3 != null) {
			val userId = arg1
			var guildId = arg2
			if (guildId == "null")
				guildId = null

			val rawArgs = context.rawArgs.toMutableList()
			rawArgs.removeAt(0)
			rawArgs.removeAt(0)
			rawArgs.removeAt(0)
			rawArgs.removeAt(0)

			loritta.networkBanManager.addNonVerifiedEntry(
					NetworkBanEntry(
							userId,
							guildId,
							NetworkBanType.valueOf(arg3),
							rawArgs.joinToString(" ")
					)
			)

			val user = lorittaShards.retrieveUserById(userId) ?: run {
				context.reply(
						LoriReply(
								"Usuário ${userId} não existe!"
						)
				)
				return
			}

			context.reply(
					LoriReply(
							"Usuário $userId (`${user.name}`) adicionado na lista de usuárioa a serem banidos na Loritta Bans Network! Use `+lslc commit_bans` para confirmar"
					)
			)
		}

		if (arg0 == "network_unban" && arg1 != null) {
			val userId = arg1

			val filtered = loritta.networkBanManager.networkBannedUsers.filter { it.guildId == userId }
					.toMutableList()

			loritta.networkBanManager.networkBannedUsers = filtered

			context.reply(
					LoriReply(
							"Usuário desbanido na Loritta Bans Network!"
					)
			)
		}

		if (arg0 == "set_sponsor" && arg1 != null && arg2 != null && arg3 != null) {
			val guild = lorittaShards.getGuildById(arg1)!!
			val serverConfig = loritta.getServerConfigForGuild(guild.id)
			val isSponsor = arg2.toBoolean()

			serverConfig.serverListConfig.isSponsored = isSponsor
			serverConfig.serverListConfig.sponsorPaid = arg3.toDouble()

			val rawArgs = context.rawArgs.toMutableList()
			rawArgs.removeAt(0)
			rawArgs.removeAt(0)
			rawArgs.removeAt(0)
			rawArgs.removeAt(0)

			serverConfig.serverListConfig.sponsoredUntil = rawArgs.joinToString(" ").convertToEpochMillis()

			loritta save serverConfig

			context.reply(
					LoriReply(
							"Servidor `${guild.name}` foi marcado como patrociado até `${serverConfig.serverListConfig.sponsoredUntil.humanize(locale)}`"
					)
			)
		}

		if (arg0 == "generate_key" && arg1 != null && arg2 != null) {
			val rawArgs = context.rawArgs.toMutableList()
			rawArgs.removeAt(0)

			val args = rawArgs.joinToString(" ")
					.split("|")
					.map { it.trim() }
					.toMutableList()

			val price = args[0].toDouble()
			val reason = args[1]

			val time = args[2].convertToEpochMillis()

			val key = RandomStringUtils.random(32, 0, 66, true, true, *"ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890@!$&".toCharArray())

			val premiumKey = PremiumKey(
					key,
					reason,
					time,
					price
			)

			loritta.premiumKeys.add(premiumKey)

			loritta.savePremiumKeys()

			context.reply(
					LoriReply(
							"Key gerada! `${premiumKey.name}`"
					)
			)
		}

		if (arg0 == "set_donator" && arg1 != null && arg2 != null && arg3 != null) {
			val user = lorittaShards.getUserById(arg1)!!
			val userConfig = loritta.getOrCreateLorittaProfile(user.id)
			val isDonator = arg2.toBoolean()

			val rawArgs = context.rawArgs.toMutableList()
			rawArgs.removeAt(0)
			rawArgs.removeAt(0)
			rawArgs.removeAt(0)
			rawArgs.removeAt(0)

			transaction(Databases.loritta) {
				userConfig.isDonator = isDonator
				userConfig.donatorPaid = arg3.toDouble()
				userConfig.donationExpiresIn = rawArgs.joinToString(" ").convertToEpochMillis()
				userConfig.donatedAt
			}

			context.reply(
					LoriReply(
							"Usuário `${user.name}` foi marcado como doador até `${userConfig.donationExpiresIn.humanize(locale)}`"
					)
			)
		}
	}
}