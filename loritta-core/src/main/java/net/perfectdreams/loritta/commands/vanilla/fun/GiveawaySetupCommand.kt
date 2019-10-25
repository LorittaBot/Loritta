package net.perfectdreams.loritta.commands.vanilla.`fun`

import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.extensions.await
import com.mrpowergamerbr.loritta.utils.extensions.isEmote
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.Role
import net.dv8tion.jda.api.entities.TextChannel
import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.platform.discord.commands.LorittaDiscordCommand
import net.perfectdreams.loritta.platform.discord.entities.DiscordCommandContext
import net.perfectdreams.loritta.platform.discord.entities.DiscordMessage
import net.perfectdreams.loritta.utils.Emotes
import net.perfectdreams.loritta.utils.giveaway.GiveawayManager

class GiveawaySetupCommand : LorittaDiscordCommand(arrayOf("giveaway setup", "sorteio setup", "giveaway criar", "sorteio criar", "giveaway create", "sorteio create"), CommandCategory.FUN) {
    override val discordPermissions = listOf(
            Permission.MESSAGE_MANAGE
    )

    override val canUseInPrivateChannel = false

    override fun getDescription(locale: BaseLocale): String? {
        return locale["commands.fun.giveaway.description"]
    }

    @Subcommand
    suspend fun root(context: DiscordCommandContext, locale: BaseLocale, args: Array<String>) {
        var customGiveawayMessage: String? = null

        if (args.isNotEmpty()) {
            val customMessage = args.joinToString(" ")

            val message = MessageUtils.generateMessage(args.joinToString(" "), listOf(), context.discordGuild, mapOf(), true)

            if (message != null) {
                context.reply(
                        LoriReply(
                                message = locale["commands.fun.giveaway.giveawayValidCustomMessage"],
                                prefix = Emotes.LORI_TEMMIE
                        )
                )

                val giveawayMessage = GiveawayManager.createGiveawayMessage(
                        context.locale,
                        "Exemplo de Giveaway",
                        "Apenas um exemplo!",
                        "\uD83C\uDF89",
                        System.currentTimeMillis() + 120_000,
                        context.discordGuild!!,
                        customMessage
                )

                context.sendMessage(giveawayMessage)
                customGiveawayMessage = customMessage
            }
        }


        getGiveawayName(context, locale, GiveawayBuilder().apply { this.customGiveawayMessage = customGiveawayMessage })
    }

    suspend fun getGiveawayName(context: DiscordCommandContext, locale: BaseLocale, builder: GiveawayBuilder) {
        val message = context.reply(
                LoriReply(
                        message = locale["commands.fun.giveaway.giveawayName"],
                        prefix = "\uD83E\uDD14"
                )
        )

        addCancelOption(context, message)

        message.onResponseByAuthor(context) {
            builder.name = it.message.contentRaw
            message.delete()
            getGiveawayDescription(context, locale, builder)
        }
    }

    suspend fun getGiveawayDescription(context: DiscordCommandContext, locale: BaseLocale, builder: GiveawayBuilder) {
        val message = context.reply(
                LoriReply(
                        message = locale["commands.fun.giveaway.giveawayDescription"],
                        prefix = "\uD83E\uDD14"
                )
        )

        addCancelOption(context, message)

        message.onResponseByAuthor(context) {
            builder.description = it.message.contentRaw
            message.delete()
            getGiveawayDuration(context, locale, builder)
        }
    }

    suspend fun getGiveawayDuration(context: DiscordCommandContext, locale: BaseLocale, builder: GiveawayBuilder) {
        val message = context.reply(
                LoriReply(
                        message = locale["commands.fun.giveaway.giveawayDuration"],
                        prefix = "\uD83E\uDD14"
                )
        )

        addCancelOption(context, message)

        message.onResponseByAuthor(context) {
            builder.duration = it.message.contentRaw
            message.delete()
            getGiveawayReaction(context, locale, builder)
        }
    }

    suspend fun getGiveawayReaction(context: DiscordCommandContext, locale: BaseLocale, builder: GiveawayBuilder) {
        val message = context.reply(
                LoriReply(
                        message = locale["commands.fun.giveaway.giveawayReaction"],
                        prefix = "\uD83E\uDD14"
                )
        )

        addCancelOption(context, message)

        message.onResponseByAuthor(context) {
            builder.reaction = it.message.contentRaw
            message.delete()
            getGiveawayChannel(context, locale, builder)
        }
    }

    suspend fun getGiveawayChannel(context: DiscordCommandContext, locale: BaseLocale, builder: GiveawayBuilder) {
        val message = context.reply(
                LoriReply(
                        message = locale["commands.fun.giveaway.giveawayChannel"],
                        prefix = "\uD83E\uDD14"
                )
        )

        addCancelOption(context, message)

        message.onResponseByAuthor(context) {
            val pop = it.message.contentRaw
            var channel: TextChannel? = null

            val channels = context.discordGuild!!.getTextChannelsByName(pop, true)

            if (channels.isNotEmpty()) {
                channel = channels[0]
            } else {
                val id = pop
                        .replace("<", "")
                        .replace("#", "")
                        .replace(">", "")

                if (id.isValidSnowflake()) {
                    channel = context.discordGuild.getTextChannelById(id)
                }
            }

            if (channel == null) {
                context.reply(
                        LoriReply(
                                "Canal inválido!",
                                Constants.ERROR
                        )
                )
                return@onResponseByAuthor
            }

            if (!channel.canTalk()) {
                context.reply(
                        LoriReply(
                                "Eu não posso falar no canal de texto!",
                                Constants.ERROR
                        )
                )
                return@onResponseByAuthor
            }

            if (!channel.canTalk(context.handle)) {
                context.reply(
                        LoriReply(
                                "Você não pode falar no canal de texto!",
                                Constants.ERROR
                        )
                )
                return@onResponseByAuthor
            }

            builder.channel = channel

            message.delete()
            getGiveawayWinningRoles(context, locale, builder)
        }
    }

    suspend fun getGiveawayWinningRoles(context: DiscordCommandContext, locale: BaseLocale, builder: GiveawayBuilder) {
        val message = context.reply(
                LoriReply(
                        message = locale["commands.fun.giveaway.giveawayDoYouWantAutomaticRole"],
                        prefix = "\uD83E\uDD14"
                )
        )

        message.handle.addReaction("✅").queue()
        message.handle.addReaction("\uD83D\uDE45").queue()

        message.handle.onReactionAddByAuthor(context) {
            message.delete()

            if (it.reactionEmote.isEmote("✅")) {
                val message = context.reply(
                        LoriReply(
                                message = locale["commands.fun.giveaway.giveawayMentionRoles"],
                                prefix = "\uD83E\uDD14"
                        )
                )

                addCancelOption(context, message)

                message.onResponseByAuthor(context) {
                    val roles = mutableSetOf<Role>()

                    it.message.mentionedRoles.forEach {
                        roles.add(it) // Vamos adicionar os cargos marcados, quick and easy
                    }

                    // Tentar pegar pelo nome
                    var roleName = it.message.contentRaw
                    if (it.message.contentRaw.startsWith("@")) {
                        roleName = it.message.contentRaw.substring(1)
                    }

                    val role = context.discordGuild!!.getRolesByName(roleName, true).firstOrNull()

                    if (role != null)
                        roles.add(role)

                    if (roles.isEmpty()) {
                        context.reply(
                                LoriReply(
                                        locale["commands.fun.giveaway.giveawayNoValidRoles"],
                                        Constants.ERROR
                                )
                        )
                        return@onResponseByAuthor
                    }

                    for (role in roles) {
                        if (!context.discordGuild.selfMember.canInteract(role) || role.isManaged) {
                            context.reply(
                                    LoriReply(
                                            locale["commands.fun.giveaway.giveawayCantInteractWithRole", "`${role.name}`"],
                                            Constants.ERROR
                                    )
                            )
                            return@onResponseByAuthor
                        }

                        if (context.discordMessage.member?.canInteract(role) == false) {
                            context.reply(
                                    LoriReply(
                                            locale["commands.fun.giveaway.giveawayCantYouInteractWithRole", "`${role.name}`"],
                                            Constants.ERROR
                                    )
                            )
                            return@onResponseByAuthor
                        }
                    }

                    message.delete()

                    builder.roleIds = roles.map { it.id }

                    getGiveawayWinnerCount(context, locale, builder)
                }
            } else {
                getGiveawayWinnerCount(context, locale, builder)
            }
        }
    }

    suspend fun getGiveawayWinnerCount(context: DiscordCommandContext, locale: BaseLocale, builder: GiveawayBuilder) {
        val message = context.reply(
                LoriReply(
                        message = locale["commands.fun.giveaway.giveawayWinnerCount"],
                        prefix = "\uD83E\uDD14"
                )
        )

        addCancelOption(context, message)

        message.onResponseByAuthor(context) {
            val numberOfWinners = it.message.contentRaw.toIntOrNull()

            if (numberOfWinners == null) {
                context.reply(
                        LoriReply(
                                "Eu não sei o que você colocou aí, mas tenho certeza que não é um número.",
                                Constants.ERROR
                        )
                )
                return@onResponseByAuthor
            }

            if (numberOfWinners !in 1..20) {
                context.reply(
                        LoriReply(
                                "Precisa ter, no mínimo, um ganhador e, no máximo, vinte ganhadores!",
                                Constants.ERROR
                        )
                )
                return@onResponseByAuthor
            }

            message.delete()

            builder.numberOfWinners = numberOfWinners

            buildGiveaway(it.message, context, locale, builder)
        }
    }

    suspend fun buildGiveaway(message: Message, context: DiscordCommandContext, locale: BaseLocale, builder: GiveawayBuilder) {
        val (reason, description, time, _reaction, channel, numberOfWinners, roleIds) = builder
        var reaction = _reaction

        val epoch = time.convertToEpochMillisRelativeToNow()

        try {
            // Testar se é possível usar o emoticon atual
            val emoteMatcher = Constants.DISCORD_EMOTE_PATTERN.matcher(reaction)

            if (emoteMatcher.find()) {
                val emoteId = emoteMatcher.group(2).toLongOrNull()

                if (emoteId != null) {
                    val emote = lorittaShards.getEmoteById(emoteId.toString())

                    // TODO: Isso está feio e confuso, dá para ser melhor.
                    reaction = if (emote == null) { // Emoji NÃO existe
                        "\uD83C\uDF89"
                    } else {
                        val emoteGuild = emote.guild
                        if (emoteGuild == null) { // Guild do emote NÃO existe (Então a Lori não conhece o emoji)
                            "\uD83C\uDF89"
                        } else {
                            if (!emote.canInteract(emoteGuild.selfMember)) { // Lori não consegue interagir com o emoji
                                "\uD83C\uDF89"
                            } else {
                                message.addReaction(emote).await()
                                emote.id
                            }
                        }
                    }
                }
            } else {
                message.addReaction(reaction).await()
            }
        } catch (e: Exception) {
            logger.trace(e) { "Exception while adding $reaction to $message, resetting emote to default..."}
            reaction = "\uD83C\uDF89"
        }

        builder.numberOfWinners = numberOfWinners

        message.delete()

        GiveawayManager.spawnGiveaway(
                loritta.getLocaleById(context.config.localeId),
                channel,
                reason,
                description,
                reaction,
                epoch,
                numberOfWinners,
                builder.customGiveawayMessage,
                roleIds
        )
    }

    fun addCancelOption(context: DiscordCommandContext, message: DiscordMessage) {
        message.handle.onReactionAddByAuthor(context) {
            if (it.reactionEmote.idLong == 412585701054611458L) {
                message.delete()
                context.reply(
                        LoriReply(
                                context.locale["commands.fun.giveaway.giveawaySetupCancelled"]
                        )
                )
            }
        }

        message.handle.addReaction("error:412585701054611458").queue()
    }

    class GiveawayBuilder {
        var customGiveawayMessage: String? = null
        var name: String? = null
        var description: String? = null
        var duration: String? = null
        var reaction: String? = null
        var channel: TextChannel? = null
        var numberOfWinners: Int? = null
        var roleIds: List<String>? = null

        operator fun component1(): String {
            return name!!
        }

        operator fun component2(): String {
            return description!!
        }

        operator fun component3(): String {
            return duration!!
        }

        operator fun component4(): String {
            return reaction!!
        }

        operator fun component5(): TextChannel {
            return channel!!
        }

        operator fun component6(): Int {
            return numberOfWinners!!
        }

        operator fun component7(): List<String>? {
            return roleIds
        }
    }
}