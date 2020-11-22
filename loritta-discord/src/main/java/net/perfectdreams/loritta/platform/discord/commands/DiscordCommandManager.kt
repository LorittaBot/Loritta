package net.perfectdreams.loritta.platform.discord.commands

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.vanilla.discord.ChannelInfoCommand
import com.mrpowergamerbr.loritta.commands.vanilla.magic.PluginsCommand
import com.mrpowergamerbr.loritta.dao.ServerConfig
import com.mrpowergamerbr.loritta.events.LorittaMessageEvent
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.DateUtils
import com.mrpowergamerbr.loritta.utils.extensions.await
import com.mrpowergamerbr.loritta.utils.extensions.localized
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.ChannelType
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.exceptions.ErrorResponseException
import net.dv8tion.jda.api.utils.MarkdownSanitizer
import net.perfectdreams.loritta.api.commands.LorittaCommand
import net.perfectdreams.loritta.api.commands.LorittaCommandManager
import net.perfectdreams.loritta.api.entities.User
import net.perfectdreams.loritta.api.messages.LorittaReply
import net.perfectdreams.loritta.commands.vanilla.`fun`.*
import net.perfectdreams.loritta.platform.discord.entities.DiscordCommandContext
import net.perfectdreams.loritta.platform.discord.entities.jda.JDAUser
import net.perfectdreams.loritta.tables.ExecutedCommandsLog
import net.perfectdreams.loritta.utils.*
import net.perfectdreams.loritta.utils.metrics.Prometheus
import org.jetbrains.exposed.sql.insert
import java.util.*
import java.util.concurrent.CancellationException
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

class DiscordCommandManager(val discordLoritta: Loritta) : LorittaCommandManager(discordLoritta) {
    init {
        registerCommand(PluginsCommand())

        registerCommand(ChannelInfoCommand())
        registerCommand(GiveawayEndCommand())
        registerCommand(GiveawayRerollCommand())
        registerCommand(GiveawaySetupCommand())
        registerCommand(GiveawayCommand())
        registerCommand(FanArtsCommand())

        contextManager.registerContext<User>(
                { clazz: KClass<*> -> clazz.isSubclassOf(User::class) || clazz == User::class },
                { sender, clazz, stack ->
                    val link = stack.pop() // Ok, será que isto é uma URL?

                    if (sender is DiscordCommandContext) {
                        val message = sender.discordMessage

                        // Vamos verificar por menções, uma menção do Discord é + ou - assim: <@123170274651668480>
                        for (user in message.mentionedUsers) {
                            if (user.asMention == link.replace("!", "")) { // O replace é necessário já que usuários com nick tem ! no mention (?)
                                // Diferente de null? Então vamos usar o avatar do usuário!
                                return@registerContext JDAUser(user)
                            }
                        }

                        // Vamos tentar procurar pelo username + discriminator
                        if (!sender.isPrivateChannel && !link.isEmpty() && sender.discordGuild != null) {
                            val split = link.split("#").dropLastWhile { it.isEmpty() }.toTypedArray()

                            if (split.size == 2 && split[0].isNotEmpty()) {
                                val matchedMember = sender.discordGuild.getMembersByName(split[0], false).stream().filter { it -> it.user.discriminator == split[1] }.findFirst()

                                if (matchedMember.isPresent) {
                                    return@registerContext JDAUser(matchedMember.get().user)
                                }
                            }
                        }

                        // Ok então... se não é link e nem menção... Que tal então verificar por nome?
                        if (!sender.isPrivateChannel && !link.isEmpty() && sender.discordGuild != null) {
                            val matchedMembers = sender.discordGuild.getMembersByEffectiveName(link, true)

                            if (!matchedMembers.isEmpty()) {
                                return@registerContext JDAUser(matchedMembers[0].user)
                            }
                        }

                        // Se não, vamos procurar só pelo username mesmo
                        if (!sender.isPrivateChannel && !link.isEmpty() && sender.discordGuild != null) {
                            val matchedMembers = sender.discordGuild.getMembersByName(link, true)

                            if (!matchedMembers.isEmpty()) {
                                return@registerContext JDAUser(matchedMembers[0].user)
                            }
                        }

                        // Ok, então só pode ser um ID do Discord!
                        try {
                            val user = discordLoritta.lorittaShards.retrieveUserById(link)

                            if (user != null) { // Pelo visto é!
                                return@registerContext JDAUser(user)
                            }
                        } catch (e: Exception) {
                        }
                    }

                    return@registerContext null
                }
        )

        contextManager.registerContext<TextChannel>(
                { clazz: KClass<*> -> clazz.isSubclassOf(TextChannel::class) || clazz == TextChannel::class },
                { context, clazz, stack ->
                    val pop = stack.pop()

                    val guild = (context as DiscordCommandContext).discordGuild!!

                    val channels = guild.getTextChannelsByName(pop, false)
                    if (channels.isNotEmpty()) {
                        return@registerContext channels[0]
                    }

                    val id = pop
                            .replace("<", "")
                            .replace("#", "")
                            .replace(">", "")

                    if (!id.isValidSnowflake())
                        return@registerContext null

                    val channel = discordLoritta.lorittaShards.shardManager.getTextChannelById(id)
                    if (channel != null) {
                        return@registerContext channel
                    }

                    return@registerContext null
                }
        )
    }

    suspend fun dispatch(ev: LorittaMessageEvent, rawArguments: List<String>, serverConfig: ServerConfig, locale: BaseLocale, legacyLocale: LegacyBaseLocale, lorittaUser: LorittaUser): Boolean {
        for (command in getRegisteredCommands()) {
            if (verifyAndDispatch(command, rawArguments, ev, serverConfig, locale, legacyLocale, lorittaUser))
                return true
        }

        return false
    }

    suspend fun verifyAndDispatch(command: LorittaCommand, rawArguments: List<String>, ev: LorittaMessageEvent, serverConfig: ServerConfig, locale: BaseLocale, legacyLocale: LegacyBaseLocale, lorittaUser: LorittaUser): Boolean {
        for (subCommand in command.subcommands) {
            if (dispatch(subCommand as LorittaCommand, rawArguments.drop(1).toMutableList(), ev, serverConfig, locale, legacyLocale, lorittaUser, true))
                return true
        }

        if (dispatch(command, rawArguments, ev, serverConfig, locale, legacyLocale, lorittaUser, false))
            return true

        return false
    }

    suspend fun dispatch(command: LorittaCommand, rawArguments: List<String>, ev: LorittaMessageEvent, serverConfig: ServerConfig, locale: BaseLocale, legacyLocale: LegacyBaseLocale, lorittaUser: LorittaUser, isSubcommand: Boolean): Boolean {
        val message = ev.message.contentDisplay
        val member = ev.message.member

        val labels = command.labels.toMutableList()

        // Comandos com espaços na label, yeah!
        var valid = false

        val checkArguments = rawArguments.toMutableList()
        var removeArgumentCount = 0

        for (label in labels) {
            val subLabels = label.split(" ")

            removeArgumentCount = 0
            var validLabelCount = 0

            for ((index, subLabel) in subLabels.withIndex()) {
                val rawArgumentAt = checkArguments.getOrNull(index) ?: break

                if (rawArgumentAt.equals(subLabel, true)) { // ignoreCase = true ~ Permite usar "+cOmAnDo"
                    validLabelCount++
                    removeArgumentCount++
                }
            }

            if (validLabelCount == subLabels.size) {
                valid = true
                break
            }
        }

        if (valid) {
            val isPrivateChannel = ev.isFromType(ChannelType.PRIVATE)
            val start = System.currentTimeMillis()

            val rawArgs = rawArguments.joinToString(" ").stripCodeMarks()
                    .split(Constants.WHITE_SPACE_MULTIPLE_REGEX)
                    .drop(removeArgumentCount)
                    .toTypedArray()
            val args = rawArgs
            val strippedArgs: Array<String>

            if (rawArgs.isNotEmpty()) {
                strippedArgs = MarkdownSanitizer.sanitize(rawArgs.joinToString(" ")).split(" ").toTypedArray()
            } else {
                strippedArgs = rawArgs
            }

            var legacyLocale = legacyLocale

            if (!isPrivateChannel) { // TODO: Migrar isto para que seja customizável
                when (ev.channel.id) {
                    "414839559721975818" -> legacyLocale = loritta.getLegacyLocaleById("default") // português (default)
                    "404713176995987466" -> legacyLocale = loritta.getLegacyLocaleById("en-us") // inglês
                    "414847180285935622" -> legacyLocale = loritta.getLegacyLocaleById("es-es") // espanhol
                    "414847291669872661" -> legacyLocale = loritta.getLegacyLocaleById("pt-pt") // português de portugal
                    "414847379670564874" -> legacyLocale = loritta.getLegacyLocaleById("pt-funk") // português funk
                }
            }

            val context = DiscordCommandContext(serverConfig, lorittaUser, locale, legacyLocale, ev, command, rawArgs, args, strippedArgs)

            if (ev.message.isFromType(ChannelType.TEXT)) {
                logger.info("(${ev.message.guild.name} -> ${ev.message.channel.name}) ${ev.author.name}#${ev.author.discriminator} (${ev.author.id}): ${ev.message.contentDisplay}")
            } else {
                logger.info("(Direct Message) ${ev.author.name}#${ev.author.discriminator} (${ev.author.id}): ${ev.message.contentDisplay}")
            }

            try {
                if (serverConfig.blacklistedChannels.contains(ev.channel.idLong) && !lorittaUser.hasPermission(LorittaPermission.BYPASS_COMMAND_BLACKLIST)) {
                    // if (!conf.miscellaneousConfig.enableBomDiaECia || (conf.miscellaneousConfig.enableBomDiaECia && command !is LigarCommand)) {
                    if (serverConfig.warnIfBlacklisted) {
                        if (serverConfig.blacklistedChannels.isNotEmpty() && ev.guild != null && ev.member != null && ev.textChannel != null) {
                            val generatedMessage = MessageUtils.generateMessage(
                                    serverConfig.blacklistedWarning ?: "???",
                                    listOf(ev.member, ev.textChannel),
                                    ev.guild
                            )
                            if (generatedMessage != null)
                                ev.textChannel.sendMessage(generatedMessage)
                                        .reference(ev.message)
                                        .queue()
                        }
                    }
                    return true // Ignorar canais bloqueados (return true = fast break, se está bloqueado o canal no primeiro comando que for executado, os outros obviamente também estarão)
                    // }
                }

                // if (cmdOptions.override && cmdOptions.blacklistedChannels.contains(ev.channel.id))
                // 	return true // Ignorar canais bloqueados

                // Check if user is banned
                if (LorittaUtilsKotlin.handleIfBanned(context, lorittaUser.profile))
                    return true

                // Cooldown
                var commandCooldown = command.cooldown
                val donatorPaid = com.mrpowergamerbr.loritta.utils.loritta.getActiveMoneyFromDonationsAsync(ev.author.idLong)
                val guildId = ev.guild?.idLong
                val guildPaid = guildId?.let { serverConfig.getActiveDonationKeysValue() } ?: 0.0

                val plan = UserPremiumPlans.getPlanFromValue(donatorPaid)

                if (plan.lessCooldown) {
                    commandCooldown /= 2
                }

                val (cooldownStatus, cooldownTriggeredAt, cooldown) = com.mrpowergamerbr.loritta.utils.loritta.commandCooldownManager.checkCooldown(
                        ev,
                        commandCooldown
                )

                if (cooldownStatus == CommandCooldownManager.CooldownStatus.RATE_LIMITED_SEND_MESSAGE) {
                    val fancy = DateUtils.formatDateDiff(cooldown + cooldownTriggeredAt, legacyLocale)
                    context.reply(
                            LorittaReply(
                                    locale["commands.pleaseWaitCooldown", fancy, "\uD83D\uDE45"],
                                    "\uD83D\uDD25"
                            )
                    )
                    return true
                } else if (cooldownStatus == CommandCooldownManager.CooldownStatus.RATE_LIMITED_MESSAGE_ALREADY_SENT) return true

                if (command.hasCommandFeedback) {
                    // Sending typing status for every single command is costly (API limits!)
                    // To avoid sending it every time, we check if we should send the typing status
                    // (We only send it if the command takes a looong time to be executed)
                    if (command.sendTypingStatus)
                        ev.channel.sendTyping().await()
                }

                if (!isPrivateChannel && ev.guild != null && ev.member != null) {
                    // Verificar se o comando está ativado na guild atual
                    if (CommandUtils.checkIfCommandIsDisabledInGuild(serverConfig, locale, ev.channel, ev.member, command::class.simpleName!!))
                        return true
                }

                // Se estamos dentro de uma guild... (Já que mensagens privadas não possuem permissões)
                if (!isPrivateChannel && ev.guild != null && ev.member != null && ev.textChannel != null && command is LorittaDiscordCommand) {
                    // Verificar se a Loritta possui todas as permissões necessárias
                    val botPermissions = command.botPermissions.toMutableList()
                    botPermissions.add(Permission.MESSAGE_EMBED_LINKS)
                    botPermissions.add(Permission.MESSAGE_EXT_EMOJI)
                    botPermissions.add(Permission.MESSAGE_ADD_REACTION)
                    botPermissions.add(Permission.MESSAGE_HISTORY)
                    val missingPermissions = ArrayList<Permission>(botPermissions.filterNot { ev.guild.selfMember.hasPermission(ev.textChannel, it) })

                    if (missingPermissions.isNotEmpty()) {
                        // oh no
                        val required = missingPermissions.joinToString(", ", transform = { "`" + it.localized(locale) + "`" })
                        context.reply(
                                LorittaReply(
                                        locale["commands.loriDoesntHavePermissionDiscord", required, "\uD83D\uDE22", "\uD83D\uDE42"],
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
                        val required = missingPermissions.joinToString(", ", transform = { "`" + legacyLocale["LORIPERMISSION_${it.name}"] + "`"})
                        var message = legacyLocale["LORIPERMISSION_MissingPermissions", required]

                        if (ev.member.hasPermission(Permission.ADMINISTRATOR) || ev.member.hasPermission(Permission.MANAGE_SERVER)) {
                            message += " ${legacyLocale["LORIPERMISSION_MissingPermCanConfigure", loritta.instanceConfig.loritta.website.url]}"
                        }
                        ev.textChannel.sendMessage(Constants.ERROR + " **|** ${ev.member.asMention} $message")
                                .reference(ev.message)
                                .queue()
                        return true
                    }
                }

                if (args.isNotEmpty() && args[0] == "🤷") { // Usar a ajuda caso 🤷 seja usado
                    context.explain()
                    return true
                }

                if (context.command.onlyOwner && !loritta.config.isOwner(context.userHandle.id)) {
                    context.reply(
                            LorittaReply(
                                    locale["commands.commandOnlyForOwner"],
                                    Constants.ERROR
                            )
                    )
                    return true
                }

                if (!context.canUseCommand()) {
                    if (command is LorittaDiscordCommand) {
                        val requiredPermissions = command.discordPermissions.filter { !ev.message.member!!.hasPermission(ev.message.textChannel, it) }
                        val required = requiredPermissions.joinToString(", ", transform = { "`" + it.localized(locale) + "`" })
                        context.reply(
                                LorittaReply(
                                        locale["commands.userDoesntHavePermissionDiscord", required],
                                        Constants.ERROR
                                )
                        )
                    }
                    return true
                }

                if (context.isPrivateChannel && !command.canUseInPrivateChannel) {
                    context.sendMessage(Constants.ERROR + " **|** " + context.getAsMention(true) + legacyLocale["CANT_USE_IN_PRIVATE"])
                    return true
                }

                /* if (command.needsToUploadFiles()) {
                    if (!LorittaUtils.canUploadFiles(context)) {
                        return true
                    }
                } */

                // Vamos pegar uma mensagem aleatória de doação, se não for nula, iremos enviar ela :3
                DonateUtils.getRandomDonationMessage(
                        locale,
                        lorittaUser.profile,
                        donatorPaid,
                        guildPaid
                )?.let { context.reply(it) }

                if (!context.isPrivateChannel && ev.guild != null) {
                    val nickname = ev.guild.selfMember.nickname

                    if (nickname != null) {
                        // #LoritaTambémTemSentimentos
                        val hasBadNickname = MiscUtils.hasInappropriateWords(nickname)

                        if (hasBadNickname) {
                            context.reply(
                                    LorittaReply(
                                            legacyLocale["LORITTA_BadNickname"],
                                            "<:lori_triste:370344565967814659>"
                                    )
                            )
                            if (ev.guild.selfMember.hasPermission(Permission.NICKNAME_CHANGE)) {
                                ev.guild.modifyNickname(ev.guild.selfMember, null).queue()
                            } else {
                                return true
                            }
                        }
                    }
                }

                if (ev.guild != null && (LorittaUtils.isGuildOwnerBanned(lorittaUser._profile, ev.guild) || LorittaUtils.isGuildBanned(ev.guild)))
                    return true

                loritta.newSuspendedTransaction {
                    lorittaUser.profile.lastCommandSentAt = System.currentTimeMillis()

                    ExecutedCommandsLog.insert {
                        it[userId] = lorittaUser.user.idLong
                        it[ExecutedCommandsLog.guildId] = if (ev.message.isFromGuild) ev.message.guild.idLong else null
                        it[channelId] = ev.message.channel.idLong
                        it[sentAt] = System.currentTimeMillis()
                        it[ExecutedCommandsLog.command] = command::class.simpleName ?: "UnknownCommand"
                        it[ExecutedCommandsLog.message] = ev.message.contentRaw
                    }

                    val profile = serverConfig.getUserDataIfExistsNested(lorittaUser.profile.userId)

                    if (profile != null && !profile.isInGuild)
                        profile.isInGuild = true
                }

                lorittaShards.updateCachedUserData(context.userHandle)

                val result = execute(context, command, rawArgs)

                if (!isPrivateChannel && ev.guild != null) {
                    if (ev.guild.selfMember.hasPermission(ev.textChannel!!, Permission.MESSAGE_MANAGE) && (serverConfig.deleteMessageAfterCommand)) {
                        ev.message.textChannel.deleteMessageById(ev.messageId).queue({}, {
                            // We don't care if we weren't able to delete the message because it was already deleted
                        })
                    }
                }

                val end = System.currentTimeMillis()
                val commandLatency = end - start
                Prometheus.COMMAND_LATENCY.labels(command::class.simpleName).observe(commandLatency.toDouble())
                if (ev.message.isFromType(ChannelType.TEXT)) {
                    logger.info("(${ev.message.guild.name} -> ${ev.message.channel.name}) ${ev.author.name}#${ev.author.discriminator} (${ev.author.id}): ${ev.message.contentDisplay} - OK! Processed in ${commandLatency}ms")
                } else {
                    logger.info("(Direct Message) ${ev.author.name}#${ev.author.discriminator} (${ev.author.id}): ${ev.message.contentDisplay} - OK! Processed in ${commandLatency}ms")
                }
                return result
            } catch (e: Exception) {
                if (e is CancellationException) {
                    logger.error(e) { "RestAction in command ${command::class.simpleName} has been cancelled" }
                    return true
                }

                if (e is ErrorResponseException) {
                    if (e.errorCode == 40005) { // Request entity too large
                        if (ev.isFromType(ChannelType.PRIVATE) || (ev.isFromType(ChannelType.TEXT) && ev.textChannel != null && ev.textChannel.canTalk()))
                            context.reply(
                                    LorittaReply(
                                            locale["commands.imageTooLarge", "8MB", Emotes.LORI_TEMMIE],
                                            "\uD83E\uDD37"
                                    )
                            )
                        return true
                    }
                }

                logger.error("Exception ao executar comando ${command.javaClass.simpleName}", e)

                // Avisar ao usuário que algo deu muito errado
                val mention = "${ev.author.asMention} "
                var reply = "\uD83E\uDD37 **|** " + mention + locale["commands.errorWhileExecutingCommand", Emotes.LORI_RAGE, Emotes.LORI_CRYING]

                if (!e.message.isNullOrEmpty())
                    reply += " `${e.message!!.escapeMentions()}`"

                if (ev.isFromType(ChannelType.PRIVATE) || (ev.isFromType(ChannelType.TEXT) && ev.textChannel != null && ev.textChannel.canTalk()))
                    ev.channel.sendMessage(reply)
                            .reference(ev.message)
                            .queue()

                return true
            }
        }
        return false
    }
}