package net.perfectdreams.loritta.morenitta.listeners

import com.github.benmanes.caffeine.cache.Caffeine
import dev.minn.jda.ktx.interactions.components.Container
import dev.minn.jda.ktx.interactions.components.Section
import dev.minn.jda.ktx.interactions.components.TextDisplay
import dev.minn.jda.ktx.interactions.components.Thumbnail
import dev.minn.jda.ktx.messages.MessageCreate
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.perfectdreams.harmony.logging.HarmonyLoggerFactory
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.components.actionrow.ActionRow
import net.dv8tion.jda.api.components.buttons.Button
import net.dv8tion.jda.api.components.buttons.ButtonStyle
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageType
import net.dv8tion.jda.api.entities.Role
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel
import net.dv8tion.jda.api.events.message.MessageBulkDeleteEvent
import net.dv8tion.jda.api.events.message.MessageDeleteEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.events.message.MessageUpdateEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.NotifyMessagesRequests
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.GuildCommandConfigs
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.utils.Emotes
import net.perfectdreams.loritta.common.utils.LorittaColors
import net.perfectdreams.loritta.common.utils.LorittaPermission
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.christmas2022event.modules.DropChristmasStuffModule
import net.perfectdreams.loritta.morenitta.dao.ServerConfig
import net.perfectdreams.loritta.morenitta.dao.servers.moduleconfigs.AutoroleConfig
import net.perfectdreams.loritta.morenitta.easter2023event.modules.DropEaster2023StuffModule
import net.perfectdreams.loritta.morenitta.events.LorittaMessageEvent
import net.perfectdreams.loritta.morenitta.messages.LorittaReply
import net.perfectdreams.loritta.morenitta.modules.*
import net.perfectdreams.loritta.morenitta.platform.discord.legacy.entities.jda.JDAUser
import net.perfectdreams.loritta.morenitta.reactionevents.DropPointsStuffModule
import net.perfectdreams.loritta.morenitta.utils.GuildCommandConfigData
import net.perfectdreams.loritta.morenitta.utils.GuildLorittaUser
import net.perfectdreams.loritta.morenitta.utils.LorittaUser
import net.perfectdreams.loritta.morenitta.utils.chance
import net.perfectdreams.loritta.morenitta.utils.debug.DebugLog
import net.perfectdreams.loritta.morenitta.utils.eventlog.EventLog
import net.perfectdreams.loritta.morenitta.utils.extensions.addReaction
import net.perfectdreams.loritta.morenitta.utils.extensions.asUserNameCodeBlockPreviewTag
import net.perfectdreams.loritta.morenitta.utils.extensions.await
import net.perfectdreams.loritta.morenitta.utils.stripCodeMarks
import org.apache.commons.text.similarity.LevenshteinDistance
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.SqlExpressionBuilder.isNotNull
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import java.time.Instant
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

class MessageListener(val loritta: LorittaBot) : ListenerAdapter() {
	companion object {
		private val logger by HarmonyLoggerFactory.logger {}
		// Our blacklist of messages (messages that we'll ignore on command execution)
		private val unavailableMessages = Collections.newSetFromMap(Caffeine.newBuilder().expireAfterWrite(15L, TimeUnit.MINUTES).build<Long, Boolean>().asMap())
		private val SPACE_PLUS_REGEX = Regex(" +")
	}

	private val inviteLinkModule = InviteLinkModule(loritta)
	private val automodModule = AutomodModule(loritta)
	private val experienceModule = ExperienceModule(loritta)
	private val afkModule = AFKModule(loritta)
	private val bomDiaECiaModule = BomDiaECiaModule(loritta)
	private val checkBoostStatusModule = CheckBoostStatusModule(loritta)
	private val addReactionForHeathecliffModule = AddReactionForHeathecliffModule(loritta)
	private val quirkyModule = QuirkyModule(loritta)
	private val christmasStuffModule = DropChristmasStuffModule(loritta)
	private val dropEaster2023StuffModule = DropEaster2023StuffModule(loritta)
	private val dropPointsStuffModule = DropPointsStuffModule(loritta)
    private val crazyManagerModule = CrazyManagerModule(loritta)

	private val messageReceivedModules = mutableListOf(
		automodModule,
		inviteLinkModule,
		christmasStuffModule,
		dropEaster2023StuffModule,
		dropPointsStuffModule,
		experienceModule,
		afkModule,
		bomDiaECiaModule,
		checkBoostStatusModule,
		addReactionForHeathecliffModule,
		quirkyModule,
        crazyManagerModule
	)

	private val messageEditedModules = mutableListOf(
		inviteLinkModule,
		automodModule
	)

	override fun onMessageReceived(event: MessageReceivedEvent) {
		if (event.author.isBot) // Se uma mensagem de um bot, ignore a mensagem!
			return

		if (DebugLog.cancelAllEvents)
			return

		if (event.message.type != MessageType.DEFAULT && event.message.type != MessageType.INLINE_REPLY) // Existem vários tipos de mensagens no Discord, mas apenas estamos interessados nas mensagens padrões de texto
			return

		loritta.launchMessageJob(event) {
			try {
				if (event.isFromType(ChannelType.PRIVATE)) {
					val serverConfig = loritta.getOrCreateServerConfig(-1, true)
					val profile = loritta.getOrCreateLorittaProfile(event.author.idLong) // Carregar perfil do usuário
					val lorittaUser = LorittaUser(loritta, event.author, EnumSet.noneOf(LorittaPermission::class.java), profile)
					val currentLocale = loritta.newSuspendedTransaction {
						profile.settings.language ?: "default"
					}
					val locale = loritta.localeManager.getLocaleById(currentLocale)
					val i18nContext = loritta.languageManager.getI18nContextByLegacyLocaleId(serverConfig.localeId)

					if (isMentioningOnlyMe(event.message.contentRaw)) {
						event.channel.sendMessage(locale["commands.commandsInDirectMessage", event.message.author.asMention, locale["commands.helpCommandName"]]).queue()
						return@launchMessageJob
					}

					val lorittaMessageEvent = LorittaMessageEvent(
						event.author,
						null,
						event.message,
						event.messageId,
						null,
						event.channel,
						null,
						serverConfig,
						locale,
						lorittaUser
					)

					// Executar comandos
					if (checkCommandsAndDispatch(lorittaMessageEvent, serverConfig, locale, i18nContext, lorittaUser))
						return@launchMessageJob
				} else {
					val member = event.member
					if (member == null) { // This may seem dumb, but it works!
						logger.warn { "${event.author} saiu do servidor ${event.guild.id} antes de eu poder processar a mensagem" }
						return@launchMessageJob
					}

					val enableProfiling = loritta.isOwner(member.idLong)

					var start = System.nanoTime()

					val serverConfigJob = loritta.getOrCreateServerConfigDeferred(event.guild.idLong, true)
						.logOnCompletion(enableProfiling) { "Loading Server Config took {time}ns for ${event.author.idLong}" }
					val lorittaProfileJob = loritta.getLorittaProfileDeferred(event.author.idLong)
						.logOnCompletion(enableProfiling) { "Loading user's profile took {time}ns for ${event.author.idLong}" }

					val serverConfig = serverConfigJob.await()

					val autoroleConfigJob = serverConfig.getCachedOrRetreiveFromDatabaseDeferred<AutoroleConfig?>(
						loritta,
						ServerConfig::autoroleConfig
					)
						.logOnCompletion(enableProfiling) { "Loading Server Config's autorole took {time}ns for ${event.author.idLong}" }

					val lorittaProfile = lorittaProfileJob.await()
					val autoroleConfig = autoroleConfigJob.await()

					start = System.nanoTime()

					logIfEnabled(enableProfiling) { "Migration Checks took ${System.nanoTime() - start}ns for ${event.author.idLong}" }

					start = System.nanoTime()
					val currentLocale = loritta.newSuspendedTransaction {
						(lorittaProfile?.settings?.language ?: serverConfig.localeId)
					}
					val locale = loritta.localeManager.getLocaleById(currentLocale)
					val i18nContext = loritta.languageManager.getI18nContextByLegacyLocaleId(serverConfig.localeId)
					logIfEnabled(enableProfiling) { "Loading ${locale.id} locale took ${System.nanoTime() - start}ns for ${event.author.idLong}" }

					start = System.nanoTime()
					// We use "loadMemberRolesLorittaPermissions(...)" to avoid unnecessary retrievals later on, because we recheck the role permission later
					val rolesLorittaPermissions = serverConfig.getOrLoadGuildRolesLorittaPermissions(loritta, event.guild)
					logIfEnabled(enableProfiling) { "Loading Loritta's role permissions in ${event.guild.idLong} took ${System.nanoTime() - start}ns for ${event.author.idLong}" }

					start = System.nanoTime()
					val memberLorittaPermissions = LorittaUser.convertRolePermissionsMapToMemberPermissionList(member, rolesLorittaPermissions)
					logIfEnabled(enableProfiling) { "Converting Loritta's role permissions to member permissions in ${event.guild.idLong} took ${System.nanoTime() - start}ns for ${event.author.idLong}" }

					start = System.nanoTime()
					val lorittaUser = GuildLorittaUser(loritta, member, memberLorittaPermissions, lorittaProfile)
					logIfEnabled(enableProfiling) { "Wrapping $member and $lorittaProfile in a GuildLorittaUser took ${System.nanoTime() - start}ns for ${event.author.idLong}" }

					start = System.nanoTime()
					EventLog.onMessageReceived(loritta, serverConfig, event.message)
					logIfEnabled(enableProfiling) { "Logging to EventLog took ${System.nanoTime() - start}ns for ${event.author.idLong}" }

					start = System.nanoTime()

					logIfEnabled(enableProfiling) { "Checking user mention took ${System.nanoTime() - start}ns for ${event.author.idLong}" }

					start = System.nanoTime()
					if (isMentioningOnlyMe(event.message.contentRaw)) {
						if (chance(25.0))
							event.message.addReaction("smol_lori_putassa_ping:397748526362132483").queue()

						var response = locale["commands.mention.response", member.asMention, serverConfig.commandPrefix]

						if (lorittaUser.hasPermission(LorittaPermission.IGNORE_COMMANDS)) {
							// Usuário não pode usar comandos

							// Qual é o cargo que não permite utilizar os meus comandos?
							val roles = member.roles.toMutableList()

							roles.add(member.guild.publicRole)
							roles.sortByDescending { it.position }

							var ignoringCommandsRole: Role? = null
							for (role in roles) {
								val permissions = rolesLorittaPermissions[role.idLong] ?: continue
								if (permissions.contains(LorittaPermission.IGNORE_COMMANDS)) {
									ignoringCommandsRole = role
									break
								}
							}

							if (ignoringCommandsRole == event.guild.publicRole)
								response =
									locale["commands.mention.responseEveryoneBlocked", event.message.author.asMention, serverConfig.commandPrefix]
							else
								response =
									locale["commands.mention.responseRoleBlocked", event.message.author.asMention, serverConfig.commandPrefix, "`${ignoringCommandsRole?.name}`"]
						} else {
							if (serverConfig.blacklistedChannels.contains(event.channel.idLong) && !lorittaUser.hasPermission(
									LorittaPermission.BYPASS_COMMAND_BLACKLIST
								)
							) {
								// Vamos pegar um canal que seja possível usar comandos
								val useCommandsIn = event.guild.textChannels.firstOrNull {
									!serverConfig.blacklistedChannels.contains(it.idLong) && it.canTalk(member)
								}

								response = if (useCommandsIn != null) {
									// Canal não bloqueado!
									locale["commands.mention.responseBlocked", event.message.author.asMention, serverConfig.commandPrefix, useCommandsIn.asMention]
								} else {
									// Nenhum canal disponível...
									locale["commands.mention.responseBlockedNoChannels", event.message.author.asMention, serverConfig.commandPrefix]
								}
							}
						}

						val responseBuilder = MessageCreateBuilder()
							.setAllowedMentions(listOf(Message.MentionType.USER, Message.MentionType.CHANNEL))
							.setContent("<:loritta:331179879582269451> **|** $response")

						if (event.channel.canTalk()) {
							event.channel.sendMessage(responseBuilder.build()).queue()
						} else {
							val privateChannel = loritta.getOrRetrievePrivateChannelForUser(event.author)
							privateChannel.sendMessage(responseBuilder.build()).queue()
						}
					}
					logIfEnabled(enableProfiling) { "Checking self mention for help took ${System.nanoTime() - start}ns for ${event.author.idLong}" }

					// Track sent message
					start = System.nanoTime()
					loritta.transaction {
						if (lorittaProfile != null && lorittaProfile.isAfk) {
							lorittaProfile.isAfk = false
						}
					}
					logIfEnabled(enableProfiling) { "Updating AFK state took ${System.nanoTime() - start}ns for ${event.author.idLong}" }

					start = System.nanoTime()
					val lorittaMessageEvent = LorittaMessageEvent(
						event.author,
						member,
						event.message,
						event.messageId,
						event.guild,
						event.channel,
						if (event.channel.type == ChannelType.TEXT) event.channel.asTextChannel() else null,
						serverConfig,
						locale,
						lorittaUser
					)
					logIfEnabled(enableProfiling) { "Creating a LorittaMessageEvent took ${System.nanoTime() - start}ns for ${event.author.idLong}" }

					start = System.nanoTime()

					if (autoroleConfig != null && autoroleConfig.enabled && autoroleConfig.giveOnlyAfterMessageWasSent && event.guild.selfMember.hasPermission(
							Permission.MANAGE_ROLES
						)
					) // Está ativado?
						AutoroleModule.giveRoles(member, autoroleConfig)

					logIfEnabled(enableProfiling) { "Giving auto role on message took ${System.nanoTime() - start}ns for ${event.author.idLong}" }

					for (module in messageReceivedModules) {
						start = System.nanoTime()
						if (module.matches(lorittaMessageEvent, lorittaUser, lorittaProfile, serverConfig, locale, i18nContext) && module.handle(lorittaMessageEvent, lorittaUser, lorittaProfile, serverConfig, locale, i18nContext))
							return@launchMessageJob
						logIfEnabled(enableProfiling) { "Executing ${module::class.simpleName} took ${System.nanoTime() - start}ns for ${event.author.idLong}" }
					}

                    val channel = event.channel
                    if (channel is GuildChannel) {
                        val now = Instant.now()

                        val requests = loritta.transaction {
                            val requestsAny = NotifyMessagesRequests.selectAll()
                                .where {
                                    NotifyMessagesRequests.notifyUserId eq null and (NotifyMessagesRequests.channelId eq event.channel.idLong) and (NotifyMessagesRequests.userId neq event.author.idLong) and (NotifyMessagesRequests.processedAt.isNull())
                                }
                                .toList()

                            val requestsSpecific = NotifyMessagesRequests.selectAll()
                                .where {
                                    NotifyMessagesRequests.notifyUserId eq event.author.idLong and (NotifyMessagesRequests.channelId eq event.channel.idLong) and (NotifyMessagesRequests.processedAt.isNull())
                                }
                                .toList()

                            val requests = requestsAny + requestsSpecific

                            NotifyMessagesRequests.update({ NotifyMessagesRequests.id inList requests.map { it[NotifyMessagesRequests.id] } }) {
                                it[NotifyMessagesRequests.processedAt] = now
                            }

                            requests
                        }

                        GlobalScope.launch {
                            for (request in requests) {
                                try {
                                    val member = event.guild.retrieveMemberById(request[NotifyMessagesRequests.userId]).await()

                                    if (member.hasPermission(channel, Permission.VIEW_CHANNEL, Permission.MESSAGE_HISTORY)) {
                                        val userDM = loritta.getOrRetrievePrivateChannelForUser(member.user)

                                        // TODO: Proper i18n!
                                        val i18nContext = loritta.languageManager.defaultI18nContext

                                        userDM.sendMessage(
                                            MessageCreate {
                                                this.useComponentsV2 = true

                                                this.components += Container {
                                                    this.accentColorRaw = LorittaColors.LorittaAqua.rgb

                                                    this.components += Section(Thumbnail(event.author.effectiveAvatarUrl)) {
                                                        this.components += TextDisplay(
                                                            buildString {
                                                                appendLine(i18nContext.get(I18nKeysData.Commands.Command.Notifymessage.UserSentMessage(event.author.asUserNameCodeBlockPreviewTag(), event.channel.asMention)))
                                                                appendLine()
                                                                appendLine(i18nContext.get(I18nKeysData.Commands.Command.Notifymessage.NowThatItHasBeenNotified))
                                                            }
                                                        )
                                                    }
                                                }

                                                this.components += ActionRow.of(
                                                    Button.of(ButtonStyle.LINK, event.message.jumpUrl, i18nContext.get(I18nKeysData.Commands.Command.Notifymessage.JumpToMessage))
                                                )
                                            }
                                        ).await()
                                    }
                                } catch (e: Exception) {
                                    logger.warn(e) { "Something went wrong while trying to send message notify request for ${request[NotifyMessagesRequests.userId]}" }
                                }
                            }
                        }
                    }

                    start = System.nanoTime()
					if (lorittaUser.hasPermission(LorittaPermission.IGNORE_COMMANDS))
						return@launchMessageJob
					logIfEnabled(enableProfiling) { "Checking for ignore permission took ${System.nanoTime() - start}ns for ${event.author.idLong}" }

					// Executar comandos
					start = System.nanoTime()
					if (checkCommandsAndDispatch(lorittaMessageEvent, serverConfig, locale, i18nContext, lorittaUser))
						return@launchMessageJob
					logIfEnabled(enableProfiling) { "All commands check took ${System.nanoTime() - start}ns for ${event.author.idLong}" }

					start = System.nanoTime()
					loritta.messageInteractionCache.values.toMutableList().forEach {
						if (it.onMessageReceived != null)
							it.onMessageReceived!!.invoke(lorittaMessageEvent)

						if (it.guildId == event.guild.idLong && it.channelId == event.channel.idLong) {
							if (it.onResponse != null)
								it.onResponse!!.invoke(lorittaMessageEvent)

							if (it.onResponseByAuthor != null) {
								if (it.originalAuthor == event.author.idLong)
									it.onResponseByAuthor!!.invoke(lorittaMessageEvent)
							}
						}
					}
					logIfEnabled(enableProfiling) { "Checking for interaction cache took ${System.nanoTime() - start}ns for ${event.author.idLong}" }

					start = System.nanoTime()
					if (event.channel.canTalk() && serverConfig.warnOnUnknownCommand) {
						val startsWithCommandPattern = Regex("^" + Pattern.quote(serverConfig.commandPrefix) + "[A-ZA-z].*")

						if (event.message.contentRaw.matches(startsWithCommandPattern)) {
							val command = event.message.contentDisplay.split(" ")[0].stripCodeMarks()
								.substring(serverConfig.commandPrefix.length)

							val list = mutableListOf(
								LorittaReply(
									"${locale["commands.unknownCommand", command, "${serverConfig.commandPrefix}${locale["commands.helpCommandName"]}"]} ${Emotes.LORI_OWO}",
									"\uD83E\uDD37"
								)
							)

							val allCommandLabels = mutableListOf<String>()

							val interaKTionsUnleashedDisabledCommands = loritta.transaction {
								GuildCommandConfigs.selectAll()
									.where {
										GuildCommandConfigs.guildId eq event.guild.idLong
									}
									.associate { it[GuildCommandConfigs.commandId] to GuildCommandConfigData.fromResultRowOrDefault(it) }
							}

							loritta.interactionsListener
								.manager
								.legacyCommandPathToDeclarations
								.forEach {
									val isEnabled = interaKTionsUnleashedDisabledCommands[it.value.slashDeclaration.uniqueId]?.enabled ?: true

									if (isEnabled)
										allCommandLabels.add(it.key)
								}

							loritta.commandMap.commands.forEach {
								if (!it.onlyOwner && !serverConfig.disabledCommands.contains(it.javaClass.simpleName) && !it.hideInHelp)
									allCommandLabels.addAll(it.labels)
							}

							loritta.legacyCommandManager.commandMap.forEach {
								if (!it.onlyOwner && !serverConfig.disabledCommands.contains(it.javaClass.simpleName)) {
									allCommandLabels.add(it.label)
									allCommandLabels.addAll(it.aliases)
								}
							}

							var diff = 999
							var nearestCommand: String? = null

							for (label in allCommandLabels) {
								val _diff = LevenshteinDistance.getDefaultInstance().apply(command, label)

								if (diff > _diff) {
									nearestCommand = label
									diff = _diff
								}
							}

							if (nearestCommand != null && 6 > diff) {
								list.add(
									LorittaReply(
										prefix = Emotes.LORI_HM,
										message = locale["commands.didYouMeanCommand", serverConfig.commandPrefix + nearestCommand],
										mentionUser = false
									)
								)
							}

							event.channel.sendMessage(list.joinToString("\n") { it.build(JDAUser(event.author)) })
								.queue {
									it.delete().queueAfter(5000, TimeUnit.MILLISECONDS)
								}
						}
					}
					logIfEnabled(enableProfiling) { "Checking for similar commands took ${System.nanoTime() - start}ns for ${event.author.idLong}" }
				}
			} catch (e: Exception) {
				logger.error(e) { "[${event.guild.name}] Erro ao processar mensagem de ${event.author.name} (${event.author.id} - ${event.message.contentRaw}" }
			}
		}
	}

	override fun onMessageUpdate(event: MessageUpdateEvent) {
		// If message was pinned, let's add it to our "blacklist"
		if (event.message.isPinned) unavailableMessages.add(event.messageIdLong)
		// If message is in our blacklist, lets ignore the event
		if (unavailableMessages.contains(event.messageIdLong)) return

		// Checking if message was sent before 15 minutes ago (900 000ms)
		if (System.currentTimeMillis() - 900_000 >= event.message.timeCreated.toEpochSecond() * 1000) return

		if (event.author.isBot) // Se uma mensagem de um bot, ignore a mensagem!
			return

		if (DebugLog.cancelAllEvents)
			return

		if (event.channel.type == ChannelType.TEXT) { // Mensagens em canais de texto
			GlobalScope.launch(loritta.coroutineDispatcher) {
				val member = event.member

				if (member == null) { // This may seem dumb, but it works!
					logger.warn { "${event.author} saiu do servidor ${event.guild.id} antes de eu poder processar a mensagem" }
					return@launch
				}

				val serverConfig = loritta.getOrCreateServerConfig(event.guild.idLong, true)
				val lorittaProfile = loritta.getOrCreateLorittaProfile(event.author.idLong)
				val locale = loritta.localeManager.getLocaleById(serverConfig.localeId)
				val i18nContext = loritta.languageManager.getI18nContextByLegacyLocaleId(serverConfig.localeId)
				val lorittaUser = GuildLorittaUser(
					loritta,
					member,
					LorittaUser.convertRolePermissionsMapToMemberPermissionList(
						member,
						serverConfig.getOrLoadGuildRolesLorittaPermissions(loritta, event.guild)
					),
					lorittaProfile)

				EventLog.onMessageUpdate(loritta, serverConfig, locale, event.message)

				val lorittaMessageEvent = LorittaMessageEvent(
					event.author,
					event.member,
					event.message,
					event.messageId,
					event.guild,
					event.channel,
					event.channel.asTextChannel(),
					serverConfig,
					locale,
					lorittaUser
				)

				for (module in messageEditedModules) {
					if (module.matches(lorittaMessageEvent, lorittaUser, lorittaProfile, serverConfig, locale, i18nContext) && module.handle(lorittaMessageEvent, lorittaUser, lorittaProfile, serverConfig, locale, i18nContext))
						return@launch
				}

				// When executing commands from an edited message, we will only execute them if the message itself is marked as edited
				// We need to do that because nowadays, Discord sends the full message object when a message is edited, instead of only sending what was edited
				// While this doesn't seem that bad, that also means that if you paste a link in chat, Discord will send the original message (without embed) and then send that the message was edited (to include the link preview)
				// And that's bad when you are using commands that you can paste a link
				// Of course, that means that if you edit a message to include a link the issue would still trigger, but hey, better this than nothing!
				// (Because to really fix this issue, we would need to cache messages and check if the message was ACTUALLY edited or not, and that's harder)
				if (event.message.isEdited) {
					if (checkCommandsAndDispatch(lorittaMessageEvent, serverConfig, locale, i18nContext, lorittaUser))
						return@launch
				}
			}
		}
	}

	override fun onMessageDelete(event: MessageDeleteEvent) {
		loritta.messageInteractionCache.remove(event.messageIdLong)
	}

	override fun onMessageBulkDelete(event: MessageBulkDeleteEvent) {
		// If the messages are bulk deleted, we also need to remove them from the message interaction cache too!
		//
		// If not, this can cause interactions to be persisted, causing issues related to "Loritta never stops replying to this message"
		// because the "source" message was deleted.
		event.messageIds.forEach { loritta.messageInteractionCache.remove(it.toLong()) }
	}

	/**
	 * Checks and dispatches commands
	 *
	 * @return if a command was dispatched
	 */
	suspend fun checkCommandsAndDispatch(lorittaMessageEvent: LorittaMessageEvent, serverConfig: ServerConfig, locale: BaseLocale, i18nContext: I18nContext, lorittaUser: LorittaUser): Boolean {
		// If Loritta can't speak in the current channel, do *NOT* try to process a command! If we try to process, Loritta will have issues that she wants to talk in a channel, but she doesn't have the "canTalk()" permission!
		if (lorittaMessageEvent.channel is TextChannel && !lorittaMessageEvent.channel.canTalk())
			return false

		val author = lorittaMessageEvent.author
		val enableProfiling = loritta.isOwner(author.idLong)

		val rawMessage = lorittaMessageEvent.message.contentRaw

		val rawArguments = rawMessage
			.split(SPACE_PLUS_REGEX)
			.toMutableList()

		val firstLabel = rawArguments.first()
		val startsWithCommandPrefix = firstLabel.startsWith(serverConfig.commandPrefix)
		val startsWithLorittaMention = firstLabel == "<@${loritta.config.loritta.discord.applicationId.toString()}>" || firstLabel == "<@!${loritta.config.loritta.discord.applicationId.toString()}>"

		if (startsWithCommandPrefix || startsWithLorittaMention) {
			if (startsWithCommandPrefix) // If it is a command prefix, remove the prefix
				rawArguments[0] = rawArguments[0].removePrefix(serverConfig.commandPrefix)
			else if (startsWithLorittaMention) { // If it is a mention, remove the first argument (which is Loritta's mention)
				rawArguments.removeAt(0)
				if (rawArguments.isEmpty()) // If it is empty, then it means that it was only Loritta's mention, so just return false
					return false
			}

			// To fix commands like "+eval" *line break* "code code code", we are going to remove the first new line from the first argument
			if (rawArguments[0].contains("\n")) {
				val splitNewLines = rawArguments[0].split(Regex("(?=\n+)", RegexOption.MULTILINE))
				rawArguments[0] = splitNewLines[0]
					.replace("\n", "")

				rawArguments.addAll(1, splitNewLines.drop(1))
			}

			// Executar comandos
			var start = System.nanoTime()
			if (loritta.commandMap.dispatch(lorittaMessageEvent, rawArguments, serverConfig, locale, i18nContext, lorittaUser)) {
				logIfEnabled(enableProfiling) { "Checking for command map commands (success) took ${System.nanoTime() - start}ns for ${author.idLong}" }
				return true
			}
			logIfEnabled(enableProfiling) { "Checking for command map commands (fail) took ${System.nanoTime() - start}ns for ${author.idLong}" }

			start = System.nanoTime()
			if (loritta.legacyCommandManager.matches(lorittaMessageEvent, rawArguments, serverConfig, locale, i18nContext, lorittaUser)) {
				logIfEnabled(enableProfiling) { "Checking for legacy command manager commands (success) took ${System.nanoTime() - start}ns for ${author.idLong}" }
				return true
			}
			logIfEnabled(enableProfiling) { "Checking for legacy command manager commands (fail) took ${System.nanoTime() - start}ns for ${author.idLong}" }

			start = System.nanoTime()
			if (loritta.interactionsListener.manager.matches(lorittaMessageEvent, rawArguments, serverConfig, locale, i18nContext, lorittaUser)) {
				logIfEnabled(enableProfiling) { "Checking for InteraKTions Unleashed map commands (success) took ${System.nanoTime() - start}ns for ${author.idLong}" }
				return true
			}
			logIfEnabled(enableProfiling) { "Checking for InteraKTions Unleashed map commands (fail) took ${System.nanoTime() - start}ns for ${author.idLong}" }

			start = System.nanoTime()
			if (loritta.legacyCommandManager.matchesNashornCommands(lorittaMessageEvent, rawArguments, serverConfig, locale, i18nContext, lorittaUser)) {
				logIfEnabled(enableProfiling) { "Checking for legacy command manager (nashorn commands) (success) commands took ${System.nanoTime() - start}ns for ${author.idLong}" }
				return true
			}
			logIfEnabled(enableProfiling) { "Checking for legacy command manager (nashorn commands) (fail) commands took ${System.nanoTime() - start}ns for ${author.idLong}" }
		}

		return false
	}

	/**
	 * Checks if the message contains only a mention for me
	 *
	 * @param contentRaw the raw content of the message
	 * @returns if the message is mentioning only me
	 */
	fun isMentioningOnlyMe(contentRaw: String): Boolean = contentRaw.replace("!", "").trim() == "<@${loritta.config.loritta.discord.applicationId.toString()}>"

	fun logIfEnabled(doLog: Boolean, msg: () -> String) {
		if (doLog)
			logger.info(msg)
	}

	fun <T> Deferred<T>.logOnCompletion(doLog: Boolean, msg: () -> String): Deferred<T> {
		val start = System.nanoTime()

		if (doLog)
			this.invokeOnCompletion {
				logger.info { msg.invoke().toString().replace("{time}", (System.nanoTime() - start).toString()) }
			}

		return this
	}
}
