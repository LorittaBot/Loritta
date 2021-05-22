package net.perfectdreams.loritta.commands.actions.modern

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.LorittaLauncher
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.utils.Constants
import net.perfectdreams.loritta.common.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.locale.Gender
import com.mrpowergamerbr.loritta.utils.onReactionAdd
import com.mrpowergamerbr.loritta.utils.removeAllFunctions
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.User
import net.perfectdreams.loritta.api.commands.ArgumentType
import net.perfectdreams.loritta.api.commands.Command
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.api.commands.CommandContext
import net.perfectdreams.loritta.api.messages.LorittaReply
import net.perfectdreams.loritta.commands.actions.modern.impl.AttackCommand
import net.perfectdreams.loritta.commands.actions.modern.impl.KissCommand
import net.perfectdreams.loritta.commands.actions.modern.impl.SlapCommand
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.platform.discord.legacy.commands.DiscordAbstractCommandBase
import net.perfectdreams.loritta.platform.discord.legacy.commands.DiscordCommandContext
import net.perfectdreams.loritta.utils.extensions.toJDA
import org.jetbrains.exposed.sql.transactions.transaction
import java.awt.Color
import java.io.File

typealias ActionCommandScope = ActionCommandDSL.() -> Unit
typealias ActionCommandContext = (BaseLocale, User, User) -> String

abstract class ActionCommand(loritta: LorittaDiscord, labels: List<String>): DiscordAbstractCommandBase(loritta, labels, CommandCategory.ACTION) {

    abstract fun create(): ActionCommandDSL

    override fun command(): Command<CommandContext> = create {
        create().also {
            localizedDescription(it.description)
            localizedExamples("commands.category.action.examples")

            usage {
                argument(ArgumentType.USER) {
                    optional = false
                }
            }

            executesDiscord {
                if (args.isEmpty()) return@executesDiscord explain()

                it.handle(this, user, userOrFail(0).toJDA())
            }
        }
    }
}

class ActionCommandDSL(val command: ActionCommand) {

    var description: String = "commands.command.${command.labels.first()}.description"
    var folderName: String? = command.labels.first()

    lateinit var color: Color
    lateinit var emoji: String
    lateinit var response: ActionCommandContext

    fun response(block: ActionCommandContext) {
        this.response = block
    }

}

fun ActionCommand.action(block: ActionCommandScope) =
        ActionCommandDSL(this).apply(block)

data class CachedGif(
        val folderName: String,
        val fileName: String
)

private fun getCachedGifs(dsl: ActionCommandDSL): MutableList<CachedGif> {
    val list = mutableListOf<CachedGif>()

    val folder = File(Loritta.ASSETS, "actions/${dsl.folderName}")
    val folderNames = listOf(
            Constants.ACTION_GENERIC,
            Constants.ACTION_MALE_AND_FEMALE,
            Constants.ACTION_FEMALE_AND_MALE,
            Constants.ACTION_MALE_AND_MALE,
            Constants.ACTION_FEMALE_AND_FEMALE,
            Constants.ACTION_BOTH
    )

    folderNames.forEach {
        File(folder, it).listFiles()?.filter { it.extension == "gif" || it.extension == "png" }?.forEach { file ->
            list.add(CachedGif(
                    it,
                    file.name
            ))
        }
    }

    return list
}

private fun ActionCommandDSL.selectGifsByGender(userGender: Gender, receiverGender: Gender): List<CachedGif> {
    val folderNames = userGender.getValidActionFolderNames(receiverGender).toMutableList()
    if (folderNames.size != 1 && Loritta.RANDOM.nextBoolean()) // Remover "generic", para evitar muitas gifs repetidas
        folderNames.remove("generic")

    return getCachedGifs(this).filter { folderNames.any { folderName -> folderName == it.folderName } }
}

private suspend fun ActionCommandDSL.handle(context: DiscordCommandContext, sender: User, receiver: User, repeat: Boolean = false) {
    val senderProfile = context.loritta.getLorittaProfile(sender.idLong)
    val receiverProfile = context.loritta.getLorittaProfile(receiver.idLong)

    // Anti-idiot people
    if (command is KissCommand && receiver.id == LorittaLauncher.loritta.discordConfig.discord.clientId) {
        context.addIdiotReply()
        return
    }

    // Searching for receiver's and sender's genders
    val userGender = transaction(Databases.loritta) { senderProfile?.settings?.gender ?: Gender.UNKNOWN }
    val receiverGender = transaction(Databases.loritta) { receiverProfile?.settings?.gender ?: Gender.UNKNOWN  }

    val response: String

    // If the sender tried to slap Lori, Lori'll slap him!
    var files = if ((command is SlapCommand || command is AttackCommand) && receiver.id == LorittaLauncher.loritta.discordConfig.discord.clientId) {
        response = response(context.locale, receiver, sender)
        selectGifsByGender(receiverGender, userGender)
    } else {
        response = response(context.locale, sender, receiver)
        selectGifsByGender(userGender, receiverGender)
    }

    // If there're no GIFs available, we'll try to avoid errors by searching for all gifs
    while (files.isEmpty()) {
        files = selectGifsByGender(Gender.UNKNOWN, Gender.UNKNOWN)
    }

    val randomImage = files.random()

    val message = context.sendMessage(sender.asMention,
            EmbedBuilder()
                    .setDescription("$emoji $response")
                    .setColor(color)
                    .setImage(context.loritta.instanceConfig.loritta.website.url + "assets/img/actions/$folderName/${randomImage.folderName}/${randomImage.fileName}")
                    .also {
                        if (sender != receiver && !repeat) {
                            it.setFooter(context.locale["commands.category.action.clickToRetribute", "\uD83D\uDD01"], null)
                        }
                    }
                    .build()
    )

    // To avoid actions flood, we'll only add the reaction if the receiver is another person or the action is already a retribution.
    if (sender != receiver && !repeat) {
        context.addReactionButton(this, message, sender, receiver)
    }
}

// Adding the "retribute" button
private fun DiscordCommandContext.addReactionButton(dsl: ActionCommandDSL, message: Message, sender: User, receiver: User) {
    message.addReaction("\uD83D\uDD01").queue()

    message.onReactionAdd(this) {
        val user = it.user ?: return@onReactionAdd

        if (it.reactionEmote.name == "\uD83D\uDD01" && user.id == receiver.id) { message.removeAllFunctions()
            message.removeAllFunctions()
            dsl.handle(this, receiver, sender,true)
        }
    }
}

private suspend fun DiscordCommandContext.addIdiotReply() = reply(
        LorittaReply(
                locale["commands.command.kiss.responseAntiIdiot"],
                "\uD83D\uDE45"
        )
)


