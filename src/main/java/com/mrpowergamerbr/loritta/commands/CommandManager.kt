package com.mrpowergamerbr.loritta.commands

import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.vanilla.`fun`.*
import com.mrpowergamerbr.loritta.commands.vanilla.actions.*
import com.mrpowergamerbr.loritta.commands.vanilla.administration.*
import com.mrpowergamerbr.loritta.commands.vanilla.discord.*
import com.mrpowergamerbr.loritta.commands.vanilla.economy.*
import com.mrpowergamerbr.loritta.commands.vanilla.images.*
import com.mrpowergamerbr.loritta.commands.vanilla.magic.*
import com.mrpowergamerbr.loritta.commands.vanilla.minecraft.*
import com.mrpowergamerbr.loritta.commands.vanilla.misc.*
import com.mrpowergamerbr.loritta.commands.vanilla.music.*
import com.mrpowergamerbr.loritta.commands.vanilla.pokemon.PokedexCommand
import com.mrpowergamerbr.loritta.commands.vanilla.roblox.RbGameCommand
import com.mrpowergamerbr.loritta.commands.vanilla.roblox.RbUserCommand
import com.mrpowergamerbr.loritta.commands.vanilla.social.*
import com.mrpowergamerbr.loritta.commands.vanilla.undertale.UndertaleBattleCommand
import com.mrpowergamerbr.loritta.commands.vanilla.undertale.UndertaleBoxCommand
import com.mrpowergamerbr.loritta.commands.vanilla.utils.*
import com.mrpowergamerbr.loritta.events.LorittaMessageEvent
import com.mrpowergamerbr.loritta.userdata.ServerConfig
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.config.EnvironmentType
import com.mrpowergamerbr.loritta.utils.extensions.await
import com.mrpowergamerbr.loritta.utils.extensions.localized
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.entities.ChannelType
import net.dv8tion.jda.core.exceptions.ErrorResponseException
import java.util.*

class CommandManager {
	companion object {
		val DEFAULT_COMMAND_OPTIONS = CommandOptions()
	}

	var commandMap: MutableList<AbstractCommand> = ArrayList()
	var defaultCmdOptions: MutableMap<String, Class<*>> = HashMap()

	init {
		commandMap.add(RollCommand())
		commandMap.add(FaustaoCommand())
		commandMap.add(CaraCoroaCommand())
		commandMap.add(PedraPapelTesouraCommand())
		commandMap.add(VaporondaCommand())
		commandMap.add(QualidadeCommand())
		commandMap.add(VaporQualidadeCommand())
		// commandMap.add(TristeRealidadeCommand())
		commandMap.add(TretaNewsCommand())
		commandMap.add(MagicBallCommand())
		commandMap.add(SAMCommand())
		commandMap.add(NyanCatCommand())
		commandMap.add(WikiaCommand())
		commandMap.add(PrimeirasPalavrasCommand())
		commandMap.add(DrakeCommand())
		commandMap.add(InverterCommand())
		// commandMap.add(SpinnerCommand())
		commandMap.add(LavaCommand())
		commandMap.add(LavaReversoCommand())
		commandMap.add(ShipCommand())
		commandMap.add(AvaliarWaifuCommand())
		commandMap.add(RazoesCommand())
		commandMap.add(QuadroCommand())
		commandMap.add(DeusCommand())
		commandMap.add(PerfeitoCommand())
		commandMap.add(TrumpCommand())
		commandMap.add(CepoCommand())
		commandMap.add(DeusesCommand())
		commandMap.add(GangueCommand())
		commandMap.add(AmigosCommand())
		commandMap.add(DiscordiaCommand())
		commandMap.add(AmizadeCommand())
		commandMap.add(PerdaoCommand())
		commandMap.add(RipVidaCommand())
		commandMap.add(AtaCommand())
		commandMap.add(JoojCommand())
		commandMap.add(OjjoCommand())
		commandMap.add(AkinatorCommand())
		commandMap.add(GameJoltCommand())
		commandMap.add(TwitchCommand())

		// =======[ IMAGENS ]======
		commandMap.add(GetOverHereCommand())
		commandMap.add(RomeroBrittoCommand())
		commandMap.add(StudiopolisTvCommand())
		commandMap.add(ManiaTitleCardCommand())
		commandMap.add(LaranjoCommand())
		commandMap.add(SustoCommand())
		commandMap.add(TriggeredCommand())
		commandMap.add(GumballCommand())
		commandMap.add(ContentAwareScaleCommand())
		commandMap.add(ArtCommand())
		commandMap.add(PepeDreamCommand())
		commandMap.add(SwingCommand())
		commandMap.add(DemonCommand())
		commandMap.add(KnuxThrowCommand())
		commandMap.add(LoriSignCommand())
		commandMap.add(TextCraftCommand())
		commandMap.add(BolsonaroCommand())
		commandMap.add(BolsoDrakeCommand())
		commandMap.add(DrawnMaskCommand())

		// =======[ DIVERSÃO ]======
		commandMap.add(CongaParrotCommand())
		commandMap.add(GabrielaCommand())
		commandMap.add(BemBoladaCommand())
		commandMap.add(RandomNaoEntreAkiCommand())
		commandMap.add(TodoGrupoTemCommand())
		commandMap.add(TioDoPaveCommand())
		commandMap.add(VemDeZapCommand())

		// =======[ MISC ]======
		commandMap.add(AjudaCommand())
		commandMap.add(PingCommand())
		commandMap.add(QuoteCommand())
		commandMap.add(SayCommand())
		commandMap.add(EscolherCommand())
		commandMap.add(LanguageCommand())
		commandMap.add(PatreonCommand())
		commandMap.add(FanArtsCommand())
		commandMap.add(DiscordBotListCommand())
		commandMap.add(ActivateKeyCommand())
		commandMap.add(VotarCommand())

		// =======[ SOCIAL ]======
		commandMap.add(PerfilCommand())
		commandMap.add(BackgroundCommand())
		commandMap.add(SobreMimCommand())
		commandMap.add(DiscriminatorCommand())
		commandMap.add(RepCommand())
		commandMap.add(RankCommand())
		commandMap.add(EditarXPCommand())
		commandMap.add(AfkCommand())
		commandMap.add(MarryCommand())
		commandMap.add(DivorceCommand())
		commandMap.add(GenderCommand())
		if (Loritta.config.environment == EnvironmentType.CANARY)
			commandMap.add(RegisterCommand())

		// =======[ ACTIONS ]======
		commandMap.add(AttackCommand())
		commandMap.add(DanceCommand())
		commandMap.add(HugCommand())
		commandMap.add(KissCommand())
		commandMap.add(SlapCommand())

		// =======[ UTILS ]=======
		commandMap.add(TranslateCommand())
		commandMap.add(EncurtarCommand())
		commandMap.add(WikipediaCommand())
		commandMap.add(MoneyCommand())
		commandMap.add(ColorInfoCommand())
		commandMap.add(LembrarCommand())
		commandMap.add(DicioCommand())
		commandMap.add(TempoCommand())
		commandMap.add(PackageInfoCommand())
		commandMap.add(IsUpCommand())
		commandMap.add(KnowYourMemeCommand())
		commandMap.add(AnagramaCommand())
		commandMap.add(CalculadoraCommand())
		commandMap.add(MorseCommand())
		commandMap.add(OCRCommand())
		commandMap.add(EmojiSearchCommand())
		commandMap.add(ReceitasCommand())
		commandMap.add(EncodeCommand())
		commandMap.add(LyricsCommand())

		// =======[ DISCORD ]=======
		commandMap.add(BotInfoCommand())
		commandMap.add(AvatarCommand())
		commandMap.add(ServerIconCommand())
		commandMap.add(EmojiCommand())
		commandMap.add(ServerInfoCommand())
		commandMap.add(InviteCommand())
		commandMap.add(UserInfoCommand())
		commandMap.add(ChatLogCommand())
		commandMap.add(InviteInfoCommand())
		commandMap.add(AddEmojiCommand())
		commandMap.add(RemoveEmojiCommand())
		if (false && Loritta.config.environment == EnvironmentType.CANARY)
			commandMap.add(UserInvitesCommand())
		commandMap.add(EmojiInfoCommand())
		commandMap.add(OldMembersCommand())

		// =======[ MINECRAFT ]========
		commandMap.add(OfflineUUIDCommand())
		commandMap.add(McAvatarCommand())
		commandMap.add(McQueryCommand())
		commandMap.add(McUUIDCommand())
		commandMap.add(McStatusCommand())
		commandMap.add(McHeadCommand())
		commandMap.add(McBodyCommand())
		commandMap.add(SpigotMcCommand())
		commandMap.add(McConquistaCommand())
		commandMap.add(PeQueryCommand())
		commandMap.add(McSkinCommand())
		commandMap.add(McMoletomCommand())

		// =======[ ROBLOX ]========
		commandMap.add(RbUserCommand())
		commandMap.add(RbGameCommand())

		// =======[ UNDERTALE ]========
		commandMap.add(UndertaleBoxCommand())
		commandMap.add(UndertaleBattleCommand())

		// =======[ POKÉMON ]========
		commandMap.add(PokedexCommand())

		// =======[ ANIME ]========
		// commandMap.add(MALAnimeCommand())
		// commandMap.add(MALMangaCommand())

		// =======[ ADMIN ]========
		commandMap.add(LimparCommand())
		commandMap.add(RoleIdCommand())
		commandMap.add(SoftBanCommand())
		commandMap.add(MuteCommand())
		commandMap.add(UnmuteCommand())
		commandMap.add(SlowModeCommand())
		// commandMap.add(TempBanCommand())
		if (false && Loritta.config.environment == EnvironmentType.CANARY)
			commandMap.add(TempRoleCommand())
		commandMap.add(KickCommand())
		commandMap.add(BanCommand())
		commandMap.add(UnbanCommand())
		commandMap.add(WarnCommand())
		commandMap.add(UnwarnCommand())
		commandMap.add(WarnListCommand())
		commandMap.add(QuickPunishmentCommand())
		if (Loritta.config.environment == EnvironmentType.CANARY)
			commandMap.add(LockCommand())
		if (Loritta.config.environment == EnvironmentType.CANARY)
			commandMap.add(UnlockCommand())

		// =======[ MAGIC ]========
		commandMap.add(ReloadCommand())
		commandMap.add(EvalCommand())
		commandMap.add(NashornTestCommand())
		commandMap.add(ServerInvitesCommand())
		commandMap.add(LorittaBanCommand())
		commandMap.add(LorittaUnbanCommand())
		commandMap.add(LoriServerListConfigCommand())
		commandMap.add(TicTacToeCommand())
		commandMap.add(EvalKotlinCommand())
		if (Loritta.config.environment == EnvironmentType.CANARY)
			commandMap.add(AntiRaidCommand())

		// =======[ MÚSICA ]========
		commandMap.add(TocarCommand())
		commandMap.add(MusicInfoCommand())
		commandMap.add(VolumeCommand())
		commandMap.add(PlaylistCommand())
		commandMap.add(PularCommand())
		commandMap.add(PausarCommand())
		commandMap.add(ResumirCommand())
		commandMap.add(SeekCommand())
		commandMap.add(YouTubeCommand())
		commandMap.add(RestartSongCommand())
		commandMap.add(TocarAgoraCommand())
		commandMap.add(ShuffleCommand())
		commandMap.add(PararCommand())

		// =======[ ECONOMIA ]========
		commandMap.add(LoraffleCommand())
		commandMap.add(DailyCommand())
		commandMap.add(PagarCommand())
		commandMap.add(SonhosCommand())
		commandMap.add(LigarCommand())
		commandMap.add(SonhosTopCommand())
		if (false && Loritta.config.environment == EnvironmentType.CANARY)
			commandMap.add(ExchangeCommand())

		for (cmdBase in this.commandMap) {
			defaultCmdOptions[cmdBase.javaClass.simpleName] = CommandOptions::class.java
		}

	}

	fun getCommandsDisabledIn(conf: ServerConfig): List<AbstractCommand> {
		return commandMap.filter { conf.disabledCommands.contains(it.javaClass.simpleName) }
	}

	suspend fun matches(ev: LorittaMessageEvent, conf: ServerConfig, locale: BaseLocale, legacyLocale: LegacyBaseLocale, lorittaUser: LorittaUser): Boolean {
		val rawMessage = ev.message.contentRaw

		// É necessário remover o new line para comandos como "+eval", etc
		val rawArguments = rawMessage.replace("\n", "").split(" ")

		// Primeiro os comandos vanilla da Loritta(tm)
		for (command in commandMap.filter { !conf.disabledCommands.contains(it.javaClass.simpleName) }) {
			if (matches(command, rawArguments, ev, conf, locale, legacyLocale, lorittaUser))
				return true
		}

		// E depois os comandos usando JavaScript (Nashorn)
		for (command in conf.nashornCommands) {
			if (matches(command, rawArguments, ev, conf, locale, legacyLocale, lorittaUser))
				return true
		}

		return false
	}

	/**
	 * Checks if the command should be handled (if all conditions are valid, like labels, etc)
	 *
	 * @param ev          the event wrapped in a LorittaMessageEvent
	 * @param conf        the server configuration
	 * @param legacyLocale      the language of the server
	 * @param lorittaUser the user that is executing this command
	 * @return            if the command was handled or not
	 */
	suspend fun matches(command: AbstractCommand, rawArguments: List<String>, ev: LorittaMessageEvent, conf: ServerConfig, locale: BaseLocale, legacyLocale: LegacyBaseLocale, lorittaUser: LorittaUser): Boolean {
		val message = ev.message.contentDisplay
		val member = ev.message.member
		val baseLocale = locale

		// Carregar as opções de comandos
		val cmdOptions = conf.getCommandOptionsFor(command)
		val prefix = if (cmdOptions.enableCustomPrefix) cmdOptions.customPrefix else conf.commandPrefix

		val labels = mutableListOf(command.label)

		labels.addAll(command.aliases)
		if (cmdOptions.enableCustomAliases) // Adicionar labels customizadas no painel
			labels.addAll(cmdOptions.aliases)

		// ignoreCase = true ~ Permite usar "+cOmAnDo"
		var valid = labels.any { rawArguments[0].equals(prefix + it, true) }
		var byMention = false

		if (rawArguments.getOrNull(1) != null && (rawArguments[0] == "<@${Loritta.config.clientId}>" || rawArguments[0] == "<@!${Loritta.config.clientId}>")) {
			// by mention
			valid = labels.any { rawArguments[1].equals(it, true) }
			byMention = true
		}

		if (valid) {
			val isPrivateChannel = ev.isFromType(ChannelType.PRIVATE)
			val start = System.currentTimeMillis()

			var args = message.replace("@${ev.guild?.selfMember?.effectiveName ?: ""}", "").stripCodeMarks().split(" ").toTypedArray().remove(0)
			var rawArgs = ev.message.contentRaw.stripCodeMarks().split(" ").toTypedArray().remove(0)
			var strippedArgs = ev.message.contentStripped.stripCodeMarks().split(" ").toTypedArray().remove(0)
			if (byMention) {
				args = args.remove(0)
				rawArgs = rawArgs.remove(0)
				strippedArgs = strippedArgs.remove(0)
			}

			var locale = legacyLocale
			if (!isPrivateChannel) { // TODO: Migrar isto para que seja customizável
				when (ev.channel.id) {
					"414839559721975818" -> locale = loritta.getLegacyLocaleById("default") // português (default)
					"404713176995987466" -> locale = loritta.getLegacyLocaleById("en-us") // inglês
					"414847180285935622" -> locale = loritta.getLegacyLocaleById("es-es") // espanhol
					"414847291669872661" -> locale = loritta.getLegacyLocaleById("pt-pt") // português de portugal
					"414847379670564874" -> locale = loritta.getLegacyLocaleById("pt-funk") // português funk
				}
			}

			val context = CommandContext(conf, lorittaUser, baseLocale, legacyLocale, ev, command, args, rawArgs, strippedArgs)

			try {
				if (ev.message.isFromType(ChannelType.TEXT)) {
					AbstractCommand.logger.info("(${ev.message.guild.name} -> ${ev.message.channel.name}) ${ev.author.name}#${ev.author.discriminator} (${ev.author.id}): ${ev.message.contentDisplay}")
				} else {
					AbstractCommand.logger.info("(Direct Message) ${ev.author.name}#${ev.author.discriminator} (${ev.author.id}): ${ev.message.contentDisplay}")
				}

				conf.lastCommandReceivedAt = System.currentTimeMillis()
				loritta.serversColl.updateOne(
						Filters.eq("_id", conf.guildId),
						Updates.set("lastCommandReceivedAt", conf.lastCommandReceivedAt)
				)

				if (conf != loritta.dummyServerConfig && ev.textChannel != null && !ev.textChannel.canTalk()) { // Se a Loritta não pode falar no canal de texto, avise para o dono do servidor para dar a permissão para ela
					LorittaUtils.warnOwnerNoPermission(ev.guild, ev.textChannel, conf)
					return true
				}

				if (conf.blacklistedChannels.contains(ev.channel.id) && !lorittaUser.hasPermission(LorittaPermission.BYPASS_COMMAND_BLACKLIST)) {
					if (!conf.miscellaneousConfig.enableBomDiaECia || (conf.miscellaneousConfig.enableBomDiaECia && command !is LigarCommand)) {
						if (conf.warnIfBlacklisted) {
							if (conf.blacklistWarning.isNotEmpty() && ev.guild != null && ev.member != null && ev.textChannel != null) {
								val generatedMessage = MessageUtils.generateMessage(
										conf.blacklistWarning,
										listOf(ev.member, ev.textChannel),
										ev.guild
								)
								ev.textChannel.sendMessage(generatedMessage).queue()
							}
						}
						return true // Ignorar canais bloqueados (return true = fast break, se está bloqueado o canal no primeiro comando que for executado, os outros obviamente também estarão)
					}
				}

				if (cmdOptions.override && cmdOptions.blacklistedChannels.contains(ev.channel.id))
					return true // Ignorar canais bloqueados

				// Cooldown
				val diff = System.currentTimeMillis() - loritta.userCooldown.getOrDefault(ev.author.idLong, 0L)

				if (1250 > diff && ev.author.id != Loritta.config.ownerId) { // Tá bom, é alguém tentando floodar, vamos simplesmente ignorar
					loritta.userCooldown.put(ev.author.idLong, System.currentTimeMillis()) // E vamos guardar o tempo atual
					return true
				}

				val profile = lorittaUser.profile
				var cooldown = command.cooldown
				if (profile.isActiveDonator() && profile.donatorPaid >= 19.99) {
					cooldown /= 2
				}

				if (cooldown > diff && ev.author.id != Loritta.config.ownerId) {
					val fancy = DateUtils.formatDateDiff((cooldown - diff) + System.currentTimeMillis(), locale)
					context.reply(
							LoriReply(
									locale.format(fancy, "\uD83D\uDE45") { commands.pleaseWaitCooldown },
									"\uD83D\uDD25"
							)
					)
					return true
				}

				loritta.userCooldown[ev.author.idLong] = System.currentTimeMillis()

				LorittaUtilsKotlin.executedCommands++
				command.executedCount++

				if (command.hasCommandFeedback() && !conf.commandOutputInPrivate) {
					ev.channel.sendTyping().await()
				}

				// Se estamos dentro de uma guild... (Já que mensagens privadas não possuem permissões)
				if (!isPrivateChannel && ev.guild != null && ev.member != null && ev.textChannel != null) {
					// Verificar se a Loritta possui todas as permissões necessárias
					val botPermissions = ArrayList<Permission>(command.getBotPermissions())
					botPermissions.add(Permission.MESSAGE_EMBED_LINKS)
					botPermissions.add(Permission.MESSAGE_EXT_EMOJI)
					botPermissions.add(Permission.MESSAGE_ADD_REACTION)
					botPermissions.add(Permission.MESSAGE_HISTORY)
					val missingPermissions = ArrayList<Permission>(botPermissions.filterNot { ev.guild.selfMember.hasPermission(ev.textChannel, it) })

					if (missingPermissions.isNotEmpty()) {
						// oh no
						val required = missingPermissions.joinToString(", ", transform = { "`" + it.localized(locale) + "`" })
						context.reply(
								LoriReply(
										locale.format(required, "\uD83D\uDE22", "\uD83D\uDE42") { commands.loriDoesntHavePermissionDiscord },
										Constants.ERROR
								)
						)
						return true
					}
				}

				if (!isPrivateChannel && ev.member != null && ev.textChannel != null) {
					val missingPermissions = command.lorittaPermissions.filterNot { lorittaUser.hasPermission(it) }

					if (missingPermissions.isNotEmpty()) {
						// oh no
						val required = missingPermissions.joinToString(", ", transform = { "`" + locale["LORIPERMISSION_${it.name}"] + "`"})
						var message = locale["LORIPERMISSION_MissingPermissions", required]

						if (ev.member.hasPermission(Permission.ADMINISTRATOR) || ev.member.hasPermission(Permission.MANAGE_SERVER)) {
							message += " ${locale["LORIPERMISSION_MissingPermCanConfigure", Loritta.config.websiteUrl]}"
						}
						ev.textChannel.sendMessage(Constants.ERROR + " **|** ${ev.member.asMention} $message").queue()
						return true
					}
				}

				if (args.isNotEmpty() && args[0] == "🤷") { // Usar a ajuda caso 🤷 seja usado
					command.explain(context)
					return true
				}

				if (LorittaUtilsKotlin.handleIfBanned(context, lorittaUser.profile)) {
					return true
				}

				if (context.cmd.onlyOwner && context.userHandle.id != Loritta.config.ownerId) {
					context.reply(
							LoriReply(
									locale.format { commands.commandOnlyForOwner },
									Constants.ERROR
							)
					)
					return true
				}

				if (!context.canUseCommand()) {
					val requiredPermissions = command.getDiscordPermissions().filter { !ev.message.member.hasPermission(ev.message.textChannel, it) }
					val required = requiredPermissions.joinToString(", ", transform = { "`" + it.localized(locale) + "`" })
					context.reply(
							LoriReply(
									locale.format(required) { commands.doesntHavePermissionDiscord },
									Constants.ERROR
							)
					)
					return true
				}

				if (context.isPrivateChannel && !command.canUseInPrivateChannel()) {
					context.sendMessage(Constants.ERROR + " **|** " + context.getAsMention(true) + locale["CANT_USE_IN_PRIVATE"])
					return true
				}

				if (command.needsToUploadFiles()) {
					if (!LorittaUtils.canUploadFiles(context)) {
						return true
					}
				}

				if (command.requiresMusicEnabled()) {
					if (!context.config.musicConfig.isEnabled || context.config.musicConfig.channelId == null) {
						val canManage = context.handle.hasPermission(Permission.MANAGE_SERVER) || context.handle.hasPermission(Permission.ADMINISTRATOR)
						context.sendMessage(Constants.ERROR + " **|** " + context.getAsMention(true) + locale["DJ_LORITTA_DISABLED"] + " \uD83D\uDE1E" + if (canManage) locale["DJ_LORITTA_HOW_TO_ENABLE", "${Loritta.config.websiteUrl}dashboard"] else "")
						return true
					}
				}

				val randomValue = Loritta.RANDOM.nextInt(0, 100)

				if (randomValue == 0) {
					context.reply(
							LoriReply(
									locale["LORITTA_PleaseUpvote", "<https://discordbots.org/bot/loritta/vote>"],
									"\uD83D\uDE0A"
							)
					)
				} else if ((randomValue == 1 || randomValue == 2 || randomValue == 3) && !profile.isActiveDonator()) {
					context.reply(
							LoriReply(
									locale["LORITTA_PleaseDonate", "<${Loritta.config.websiteUrl}donate>"],
									"<:lori_owo:432530033316462593>"
							)
					)
				}

				if (!context.isPrivateChannel) {
					val nickname = context.guild.selfMember.nickname

					if (nickname != null) {
						// #LoritaTambémTemSentimentos
						val hasBadNickname = MiscUtils.hasInappropriateWords(nickname)

						if (hasBadNickname) {
							context.reply(
									LoriReply(
											locale["LORITTA_BadNickname"],
											"<:lori_triste:370344565967814659>"
									)
							)
							if (context.guild.selfMember.hasPermission(Permission.NICKNAME_CHANGE)) {
								context.guild.controller.setNickname(context.guild.selfMember, null).queue()
							} else {
								return true
							}
						}
					}
				}

				command.run(context, context.legacyLocale)

				val cmdOpti = context.config.getCommandOptionsFor(command)
				if (!isPrivateChannel && ev.guild != null) {
					if (ev.guild.selfMember.hasPermission(ev.textChannel, Permission.MESSAGE_MANAGE) && (conf.deleteMessageAfterCommand || (cmdOpti.override && cmdOpti.deleteMessageAfterCommand))) {
						ev.message.textChannel.getMessageById(ev.messageId).queue {
							// Nós iremos pegar a mensagem novamente, já que talvez ela tenha sido deletada
							it.delete().queue()
						}
					}
				}

				loritta.userCooldown[ev.author.idLong] = System.currentTimeMillis()

				val end = System.currentTimeMillis()
				if (ev.message.isFromType(ChannelType.TEXT)) {
					AbstractCommand.logger.info("(${ev.message.guild.name} -> ${ev.message.channel.name}) ${ev.author.name}#${ev.author.discriminator} (${ev.author.id}): ${ev.message.contentDisplay} - OK! Processado em ${end - start}ms")
				} else {
					AbstractCommand.logger.info("(Direct Message) ${ev.author.name}#${ev.author.discriminator} (${ev.author.id}): ${ev.message.contentDisplay} - OK! Processado em ${end - start}ms")
				}
				return true
			} catch (e: Exception) {
				if (e is ErrorResponseException) {
					if (e.errorCode == 40005) { // Request entity too large
						if (ev.isFromType(ChannelType.PRIVATE) || (ev.isFromType(ChannelType.TEXT) && ev.textChannel != null && ev.textChannel.canTalk()))
							context.reply(
									LoriReply(
											context.legacyLocale.format("8MB", Emotes.LORI_TEMMIE) { commands.imageTooLarge },
											"\uD83E\uDD37"
									)
							)
						return true
					}
				}

				AbstractCommand.logger.error("Exception ao executar comando ${command.javaClass.simpleName}", e)
				LorittaUtilsKotlin.sendStackTrace(ev.message, e)

				// Avisar ao usuário que algo deu muito errado
				val mention = if (conf.mentionOnCommandOutput) "${ev.author.asMention} " else ""
				val reply = "\uD83E\uDD37 **|** " + mention + locale["ERROR_WHILE_EXECUTING_COMMAND"]

				if (!e.message.isNullOrEmpty())
					reply + " ${e.message!!.escapeMentions()}"

				if (ev.isFromType(ChannelType.PRIVATE) || (ev.isFromType(ChannelType.TEXT) && ev.textChannel != null && ev.textChannel.canTalk()))
					ev.channel.sendMessage(reply).queue()
				return true
			}
		}
		return false
	}
}
