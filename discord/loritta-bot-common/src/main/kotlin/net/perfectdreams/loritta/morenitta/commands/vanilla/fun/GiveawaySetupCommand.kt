package net.perfectdreams.loritta.morenitta.commands.vanilla.`fun`

import net.perfectdreams.loritta.morenitta.utils.*
import net.perfectdreams.loritta.common.locale.BaseLocale
import mu.KotlinLogging
import dev.kord.common.entity.Permission
import net.perfectdreams.loritta.deviousfun.entities.Role
import net.perfectdreams.loritta.common.entities.LorittaEmote
import net.perfectdreams.loritta.common.entities.UnicodeEmote
import net.perfectdreams.loritta.morenitta.messages.LorittaReply
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.platform.discord.legacy.commands.DiscordAbstractCommandBase
import net.perfectdreams.loritta.morenitta.platform.discord.legacy.commands.DiscordCommandContext
import net.perfectdreams.loritta.morenitta.platform.discord.legacy.entities.DiscordEmote
import net.perfectdreams.loritta.common.utils.Emotes
import net.perfectdreams.loritta.deviousfun.entities.Channel

class GiveawaySetupCommand(loritta: LorittaBot) : DiscordAbstractCommandBase(
    loritta,
    listOf("giveaway setup", "sorteio setup", "giveaway criar", "sorteio criar", "giveaway create", "sorteio create"),
    net.perfectdreams.loritta.common.commands.CommandCategory.FUN
) {
    companion object {
        private const val LOCALE_PREFIX = "commands.command"
        val logger = KotlinLogging.logger { }
    }

    override fun command() = create {
        userRequiredPermissions = listOf(Permission.ManageMessages)

        canUseInPrivateChannel = false

        localizedDescription("$LOCALE_PREFIX.giveaway.description")

        executesDiscord {
            val context = this

            var customGiveawayMessage: String? = null

            if (args.isNotEmpty()) {
                val customMessage = args.joinToString(" ")

                val watermarkedMessage = MessageUtils.watermarkSayMessage(
                    customMessage,
                    context.user,
                    context.locale["$LOCALE_PREFIX.giveaway.giveawayCreatedBy"]
                )

                val message = MessageUtils.generateMessage(watermarkedMessage, listOf(), context.guild, mapOf(), true)

                if (message != null) {
                    context.reply(
                        LorittaReply(
                            message = locale["commands.command.giveaway.giveawayValidCustomMessage"],
                            prefix = Emotes.LORI_TEMMIE
                        )
                    )

                    val giveawayMessage = loritta.giveawayManager.createGiveawayMessage(
                        context.locale,
                        "Exemplo de Giveaway",
                        "Apenas um exemplo!",
                        "\uD83C\uDF89",
                        System.currentTimeMillis() + 120_000,
                        context.guild,
                        watermarkedMessage
                    )

                    context.sendMessage(giveawayMessage)
                    customGiveawayMessage = watermarkedMessage
                }
            }


            getGiveawayName(
                context,
                locale,
                GiveawayBuilder().apply { this.customGiveawayMessage = customGiveawayMessage })
        }
    }

    suspend fun getGiveawayName(context: DiscordCommandContext, locale: BaseLocale, builder: GiveawayBuilder) {
        val message = context.discordMessage.channel.sendMessage(
            LorittaReply(
                message = locale["$LOCALE_PREFIX.giveaway.giveawayName"],
                prefix = "\uD83E\uDD14"
            ).build(context.getUserMention(true))
        )

        addCancelOption(context, message)

        message.onResponseByAuthor(context) {
            builder.name = it.message.contentRaw
            message.delete()
            getGiveawayDescription(context, locale, builder)
        }
    }

    suspend fun getGiveawayDescription(context: DiscordCommandContext, locale: BaseLocale, builder: GiveawayBuilder) {
        val message = context.discordMessage.channel.sendMessage(
            LorittaReply(
                message = locale["$LOCALE_PREFIX.giveaway.giveawayDescription"],
                prefix = "\uD83E\uDD14"
            ).build(context.getUserMention(true))
        )

        addCancelOption(context, message)

        message.onResponseByAuthor(context) {
            builder.description = it.message.contentRaw
            message.delete()
            getGiveawayDuration(context, locale, builder)
        }
    }

    suspend fun getGiveawayDuration(context: DiscordCommandContext, locale: BaseLocale, builder: GiveawayBuilder) {
        val message = context.discordMessage.channel.sendMessage(
            LorittaReply(
                message = locale["commands.command.giveaway.giveawayDuration"],
                prefix = "\uD83E\uDD14"
            ).build(context.getUserMention(true))
        )

        addCancelOption(context, message)

        message.onResponseByAuthor(context) {
            builder.duration = it.message.contentRaw
            message.delete()
            getGiveawayReaction(context, locale, builder)
        }
    }

    suspend fun getGiveawayReaction(context: DiscordCommandContext, locale: BaseLocale, builder: GiveawayBuilder) {
        val message = context.discordMessage.channel.sendMessage(
            LorittaReply(
                message = locale["$LOCALE_PREFIX.giveaway.giveawayReaction"],
                prefix = "\uD83E\uDD14"
            ).build(context.getUserMention(true))
        )

        addCancelOption(context, message)

        message.onResponseByAuthor(context) {
            // If the message contains a emote, we are going to use it on the giveaway
            // This way we can use any emote as long as the user has Nitro and Loritta shares a server.
            //
            // Before we were extracting using a RegEx pattern,
            val emoteInTheMessage = it.message.emotes.firstOrNull()

            builder.reaction = if (emoteInTheMessage != null)
                DiscordEmote.DiscordEmoteBackedByJdaEmote(emoteInTheMessage)
            else
                UnicodeEmote(it.message.contentRaw)

            message.delete()
            getGiveawayChannel(context, locale, builder)
        }
    }

    suspend fun getGiveawayChannel(context: DiscordCommandContext, locale: BaseLocale, builder: GiveawayBuilder) {
        val message = context.discordMessage.channel.sendMessage(
            LorittaReply(
                message = locale["$LOCALE_PREFIX.giveaway.giveawayChannel"],
                prefix = "\uD83E\uDD14"
            ).build(context.getUserMention(true))
        )

        addCancelOption(context, message)

        message.onResponseByAuthor(context) {
            val pop = it.message.contentRaw
            var channel: Channel? = null

            val channels = context.guild.getTextChannelsByName(pop, true)

            if (channels.isNotEmpty()) {
                channel = channels[0]
            } else {
                val id = pop
                    .replace("<", "")
                    .replace("#", "")
                    .replace(">", "")

                if (id.isValidSnowflake()) {
                    channel = context.guild.getTextChannelById(id)
                }
            }

            if (channel == null) {
                context.reply(
                    LorittaReply(
                        "Canal inválido!",
                        Constants.ERROR
                    )
                )
                return@onResponseByAuthor
            }

            if (!channel.canTalk()) {
                context.reply(
                    LorittaReply(
                        "Eu não posso falar no canal de texto!",
                        Constants.ERROR
                    )
                )
                return@onResponseByAuthor
            }

            if (!channel.canTalk(context.member!!)) {
                context.reply(
                    LorittaReply(
                        "Você não pode falar no canal de texto!",
                        Constants.ERROR
                    )
                )
                return@onResponseByAuthor
            }

            val lorittaAsMember = context.guild.retrieveSelfMember()

            if (!lorittaAsMember.hasPermission(channel, Permission.AddReactions)) {
                context.reply(
                    LorittaReply(
                        "Não tenho permissão para reagir nesse canal!",
                        Constants.ERROR
                    )
                )
                return@onResponseByAuthor
            }

            if (!lorittaAsMember.hasPermission(channel, Permission.EmbedLinks)) {
                context.reply(
                    LorittaReply(
                        "Não tenho permissão para enviar embeds nesse canal!",
                        Constants.ERROR
                    )
                )
                return@onResponseByAuthor
            }

            if (!lorittaAsMember.hasPermission(channel, Permission.ReadMessageHistory)) {
                context.reply(
                    LorittaReply(
                        "Não tenho permissão para ver o histórico desse canal!",
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
        val message = context.discordMessage.channel.sendMessage(
            LorittaReply(
                message = locale["$LOCALE_PREFIX.giveaway.giveawayDoYouWantAutomaticRole"],
                prefix = "\uD83E\uDD14"
            ).build(context.getUserMention(true))
        )

        runCatching { message.addReaction("✅") }
        runCatching { message.addReaction("\uD83D\uDE45") }

        message.onReactionAddByAuthor(context) {
            message.delete()

            if (it.reactionEmote.name == "✅") {
                val message = context.discordMessage.channel.sendMessage(
                    LorittaReply(
                        message = locale["$LOCALE_PREFIX.giveaway.giveawayMentionRoles"],
                        prefix = "\uD83E\uDD14"
                    ).build(context.getUserMention(true))
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

                    val role = context.guild.getRolesByName(roleName, true).firstOrNull()

                    if (role != null)
                        roles.add(role)

                    if (roles.isEmpty()) {
                        context.reply(
                            LorittaReply(
                                locale["commands.command.giveaway.giveawayNoValidRoles"],
                                Constants.ERROR
                            )
                        )
                        return@onResponseByAuthor
                    }

                    val selfMember = context.guild.retrieveSelfMember()
                    for (role in roles) {
                        if (!selfMember.canInteract(role) || role.isManaged) {
                            context.reply(
                                LorittaReply(
                                    locale["commands.command.giveaway.giveawayCantInteractWithRole", "`${role.name}`"],
                                    Constants.ERROR
                                )
                            )
                            return@onResponseByAuthor
                        }

                        if (context.discordMessage.member?.canInteract(role) == false) {
                            context.reply(
                                LorittaReply(
                                    locale["commands.command.giveaway.giveawayCantYouInteractWithRole", "`${role.name}`"],
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
        val message = context.discordMessage.channel.sendMessage(
            LorittaReply(
                message = locale["commands.command.giveaway.giveawayWinnerCount"],
                prefix = "\uD83E\uDD14"
            ).build(context.getUserMention(true))
        )

        addCancelOption(context, message)

        message.onResponseByAuthor(context) {
            val numberOfWinners = it.message.contentRaw.toIntOrNull()

            if (numberOfWinners == null) {
                context.reply(
                    LorittaReply(
                        "Eu não sei o que você colocou aí, mas tenho certeza que não é um número.",
                        Constants.ERROR
                    )
                )
                return@onResponseByAuthor
            }

            if (numberOfWinners !in 1..100) {
                context.reply(
                    LorittaReply(
                        locale["commands.command.giveaway.giveawayWinnerCountNotInRange"],
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

    suspend fun buildGiveaway(
        message: net.perfectdreams.loritta.deviousfun.entities.Message,
        context: DiscordCommandContext,
        locale: BaseLocale,
        builder: GiveawayBuilder
    ) {
        val (reason, description, time, reaction, channel, numberOfWinners, roleIds) = builder

        val epoch = TimeUtils.convertToMillisRelativeToNow(time)

        builder.numberOfWinners = numberOfWinners

        message.delete()

        loritta.giveawayManager.spawnGiveaway(
            loritta.localeManager.getLocaleById(context.serverConfig.localeId),
            channel,
            reason,
            description,
            reaction.code,
            epoch,
            numberOfWinners,
            builder.customGiveawayMessage,
            roleIds
        )
    }

    suspend fun addCancelOption(
        context: DiscordCommandContext,
        message: net.perfectdreams.loritta.deviousfun.entities.Message
    ) {
        message.onReactionAddByAuthor(context) {
            if (it.reactionEmote.idLong == 412585701054611458L) {
                message.delete()
                context.reply(
                    LorittaReply(
                        context.locale["$LOCALE_PREFIX.giveaway.giveawaySetupCancelled"]
                    )
                )
            }
        }

        runCatching { message.addReaction("error:412585701054611458") }
    }

    class GiveawayBuilder {
        var customGiveawayMessage: String? = null
        var name: String? = null
        var description: String? = null
        var duration: String? = null
        var reaction: LorittaEmote? = null
        var channel: Channel? = null
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

        operator fun component4(): LorittaEmote {
            return reaction!!
        }

        operator fun component5(): Channel {
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
