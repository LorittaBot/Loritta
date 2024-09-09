package net.perfectdreams.loritta.morenitta.interactions.commands

import dev.minn.jda.ktx.messages.InlineMessage
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.utils.messages.MessageCreateData
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.utils.Emotes
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.dao.ServerConfig
import net.perfectdreams.loritta.morenitta.interactions.InteractionMessage
import net.perfectdreams.loritta.morenitta.interactions.commands.options.*
import net.perfectdreams.loritta.morenitta.utils.Constants
import net.perfectdreams.loritta.morenitta.utils.extensions.getLocalizedName
import java.time.Instant

/**
 * A context related to commands
 */
interface CommandContext {
    val jda: JDA
    val loritta: LorittaBot
    val user: User
    val locale: BaseLocale
    val i18nContext: I18nContext
    val config: ServerConfig
    val rootDeclaration: SlashCommandDeclaration
    val commandDeclaration: SlashCommandDeclaration

    suspend fun reply(ephemeral: Boolean, builder: suspend InlineMessage<MessageCreateData>.() -> Unit): InteractionMessage

    /**
     * Sends an embed explaining what the command does
     *
     * @param context the context of the command
     */
    suspend fun explain() {
        val commandDescription = i18nContext.get(commandDeclaration.description)
        val commandLabel = i18nContext.get(commandDeclaration.name)
        val commandPrefix = config.commandPrefix

        // I think that this should NEVER be null... well, I hope so
        val declarationPath = loritta.interactionsListener.manager.findDeclarationPath(commandDeclaration)

        val fullLabel = buildString {
            declarationPath.forEach {
                when (it) {
                    is SlashCommandDeclaration -> append(i18nContext.get(it.name))
                    is SlashCommandGroupDeclaration -> append(i18nContext.get(it.name))
                }
                this.append(" ")
            }
        }.trim()

        val commandLabelWithPrefix = "$commandPrefix$fullLabel"

        val embed = EmbedBuilder()
            .setColor(Constants.LORITTA_AQUA)
            .setAuthor(locale["commands.explain.clickHereToSeeAllMyCommands"], "${loritta.config.loritta.website.url}commands", jda.selfUser.effectiveAvatarUrl)
            .setTitle("${Emotes.LORI_HM} `$commandPrefix$fullLabel`")
            .setFooter("${user.name} • ${commandDeclaration.category.getLocalizedName(i18nContext)}", user.effectiveAvatarUrl)
            .setTimestamp(Instant.now())

        val description = buildString {
            // Builds the "How to Use" string
            this.append(commandDescription)
            this.append('\n')
            this.append('\n')
            this.append("${Emotes.LORI_SMILE} **${locale["commands.explain.howToUse"]}**")
            this.append(" `")
            this.append(commandLabelWithPrefix)
            this.append('`')

            // I don't think that the executor should ever be null at this point
            val executor = commandDeclaration.executor
            if (executor != null) {
                // Only add the arguments if the list is not empty (to avoid adding a empty "` `")
                val options = executor.options.registeredOptions
                if (options.isNotEmpty()) {
                    this.append("**")
                    this.append('`')
                    this.append(' ')
                    for ((index, option) in options.withIndex()) {
                        if (option is DiscordOptionReference) {
                            if (option.required)
                                append("<")
                            else
                                append("[")
                        } else append("<")

                        append(option.name)
                        append(":")
                        append(" ")

                        when (option) {
                            is LongDiscordOptionReference -> append("número inteiro")
                            is BooleanDiscordOptionReference -> append("booleano")
                            is NumberDiscordOptionReference -> append("número")
                            is StringDiscordOptionReference -> append("texto")
                            is ChannelDiscordOptionReference -> append("canal")
                            is UserDiscordOptionReference -> append("usuário")
                            is RoleDiscordOptionReference -> append("cargo")
                            is AttachmentDiscordOptionReference -> append("arquivo")
                            is ImageReferenceDiscordOptionReference -> append("referência de imagem")
                            is ImageReferenceOrAttachmentDiscordOptionReference -> append("referência de imagem ou arquivo")
                        }

                        if (option is DiscordOptionReference) {
                            if (option.required)
                                append(">")
                            else
                                append("]")
                        } else append(">")

                        if (index != options.size - 1)
                            this.append(' ')
                    }
                    this.append('`')
                    this.append("**")
                }
            }
        }

        embed.setDescription(description)

        // Create example list
        val examplesKey = commandDeclaration.examples
        val examples = ArrayList<String>()

        if (examplesKey != null) {
            val examplesAsString = i18nContext.get(examplesKey)

            for (example in examplesAsString) {
                val split = example.split("|-|")
                    .map { it.trim() }

                if (split.size == 2) {
                    // If the command has a extended description
                    // "12 |-| Gira um dado de 12 lados"
                    // A extended description can also contain "nothing", but contains a extended description
                    // "|-| Gira um dado de 6 lados"
                    val (commandExample, explanation) = split

                    examples.add("\uD83D\uDD39 **$explanation**")
                    examples.add("`" + commandLabelWithPrefix + "`" + (if (commandExample.isEmpty()) "" else "**` $commandExample`**"))
                } else {
                    val commandExample = split[0]

                    examples.add("`" + commandLabelWithPrefix + "`" + if (commandExample.isEmpty()) "" else "**` $commandExample`**")
                }
            }
        }

        if (examples.isNotEmpty()) {
            embed.addField(
                "\uD83D\uDCD6 ${locale["commands.explain.examples"]}",
                examples.joinToString("\n", transform = { it }),
                false
            )
        }

        val userRequiredPermissionsRaw = rootDeclaration.defaultMemberPermissions?.permissionsRaw
        if (userRequiredPermissionsRaw != null) {
            val userRequiredPermissions = Permission.getPermissions(userRequiredPermissionsRaw)

            var field = ""
            if (userRequiredPermissions.isNotEmpty()) {
                field += "\uD83D\uDC81 ${locale["commands.explain.youNeedToHavePermission", userRequiredPermissions.joinToString(", ", transform = { "`${it.getLocalizedName(i18nContext)}`" })]}\n"
            }
            // if (command.botRequiredPermissions.isNotEmpty()) {
            //     field += "<:loritta:331179879582269451> ${locale["commands.explain.loriNeedToHavePermission", command.botRequiredPermissions.joinToString(", ", transform = { "`${it.getLocalizedName(i18nContext)}`" })]}\n"
            // }
            embed.addField(
                "\uD83D\uDCDB ${locale["commands.explain.permissions"]}",
                field,
                false
            )
        }

        val otherAlternatives = mutableListOf(
            buildString {
                this.append("/")
                declarationPath.forEach {
                    when (it) {
                        is SlashCommandDeclaration -> append(i18nContext.get(it.name))
                        is SlashCommandGroupDeclaration -> append(i18nContext.get(it.name))
                    }
                    this.append(" ")
                }
            }.trim(),
            buildString {
                this.append(commandPrefix)
                declarationPath.forEach {
                    when (it) {
                        is SlashCommandDeclaration -> append(i18nContext.get(it.name))
                        is SlashCommandGroupDeclaration -> append(i18nContext.get(it.name))
                    }
                    this.append(" ")
                }
            }.trim()
        )

        for (alternativeLabel in commandDeclaration.alternativeLegacyLabels) {
            otherAlternatives.add(
                buildString {
                    append(commandPrefix)
                    declarationPath.dropLast(1).forEach {
                        when (it) {
                            is SlashCommandDeclaration -> append(i18nContext.get(it.name))
                            is SlashCommandGroupDeclaration -> append(i18nContext.get(it.name))
                        }
                        this.append(" ")
                    }
                    append(alternativeLabel)
                }
            )
        }

        for (absolutePath in commandDeclaration.alternativeLegacyAbsoluteCommandPaths) {
            otherAlternatives.add("${commandPrefix}$absolutePath")
        }

        if (otherAlternatives.isNotEmpty()) {
            embed.addField(
                "\uD83D\uDD00 ${locale["commands.explain.aliases"]}",
                otherAlternatives.joinToString(transform = { "`$it`" }),
                false
            )
        }

        /* val similarCommands = loritta.commandMap.commands.filter {
            it.commandName != command.commandName && it.commandName in command.similarCommands
        }

        if (similarCommands.isNotEmpty()) {
            embed.addField(
                "${Emotes.LORI_WOW} ${locale["commands.explain.relatedCommands"]}",
                similarCommands.joinToString(transform = { "`${serverConfig.commandPrefix}${it.labels.first()}`" }),
                false
            )
        } */

        reply(true) {
            mentions {}

            embeds += embed.build()
        }
    }
}