package net.perfectdreams.loritta.commands.actions.modern

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.LorittaLauncher
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.locale.Gender
import com.mrpowergamerbr.loritta.utils.onReactionAdd
import com.mrpowergamerbr.loritta.utils.removeAllFunctions
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.User
import net.perfectdreams.loritta.api.commands.ArgumentType
import net.perfectdreams.loritta.api.commands.Command
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.api.commands.CommandContext
import net.perfectdreams.loritta.api.messages.LorittaReply
import net.perfectdreams.loritta.commands.actions.modern.impl.AttackCommand
import net.perfectdreams.loritta.commands.actions.modern.impl.KissCommand
import net.perfectdreams.loritta.commands.actions.modern.impl.SlapCommand
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.platform.discord.commands.DiscordAbstractCommandBase
import net.perfectdreams.loritta.platform.discord.commands.DiscordCommandContext
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
            localizedExamples("commands.actions.examples")

            usage {
                argument(ArgumentType.USER) {
                    optional = false
                }
            }

            executesDiscord {
                if (args.isEmpty()) return@executesDiscord explain()

                handle(it, userOrFail(0).toJDA())
            }
        }
    }
}

class ActionCommandDSL(val command: ActionCommand) {

    var description: String = "commands.actions.${command.labels.first()}.description"
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

private suspend fun DiscordCommandContext.handle(dsl: ActionCommandDSL, receiver: User, repeat: Boolean = false) {
    val senderProfile = lorittaUser.profile
    val receiverProfile = LorittaLauncher.loritta.getLorittaProfile(receiver.id)

    // Anti-gente idiota
    if (dsl.command is KissCommand && receiver.id == LorittaLauncher.loritta.discordConfig.discord.clientId) {
        addIdiotReply()
        return
    }

    // R U a boy or girl?
    val userGender = transaction(Databases.loritta) { senderProfile.settings.gender }
    val receiverGender = transaction(Databases.loritta) { receiverProfile?.settings?.gender ?: Gender.UNKNOWN  }

    val response: String = dsl.response(locale, user, receiver)

    // Quem tentar estapear a Loritta, vai ser estapeado
    var files = if ((dsl.command is SlapCommand || dsl.command is AttackCommand) && receiver.id == LorittaLauncher.loritta.discordConfig.discord.clientId) {
        dsl.selectGifsByGender(receiverGender, userGender)
    } else {
        dsl.selectGifsByGender(userGender, receiverGender)
    }

    // Caso não tenha nenhuma GIF disponível, vamos abrir o nosso "leque" de GIFs, para evitar que dê erro
    while (files.isEmpty()) {
        files = dsl.selectGifsByGender(Gender.UNKNOWN, Gender.UNKNOWN)
    }

    val randomImage = files.random()

    val message = sendMessage(user.asMention,
            EmbedBuilder()
                    .setDescription("${dsl.emoji} $response")
                    .setColor(dsl.color)
                    .setImage(loritta.instanceConfig.loritta.website.url + "assets/img/actions/${dsl.folderName}/${randomImage.folderName}/${randomImage.fileName}")
                    .also {
                        if (user != receiver && !repeat) {
                            it.setFooter(locale["commands.actions.clickToRetribute", "\uD83D\uDD01"], null)
                        }
                    }
                    .build()
    )

    // Para evitar floods de actions, nós apenas iremos adicionar a reação *caso* o usuário tenha usado o comando em outra pessoa
    if (user != receiver && !repeat) {
        addReactionButton(dsl, message, receiver)
    }
}

private fun DiscordCommandContext.addReactionButton(dsl: ActionCommandDSL, message: Message, receiver: User) {
    message.addReaction("\uD83D\uDD01").queue()

    message.onReactionAdd(this) {
        val user = it.user ?: return@onReactionAdd

        if (it.reactionEmote.name == "\uD83D\uDD01" && user.id == receiver.id) { message.removeAllFunctions()
            handle(dsl, receiver, true)
        }
    }
}

private suspend fun DiscordCommandContext.addIdiotReply() = reply(
        LorittaReply(
                locale["commands.actions.kiss.responseAntiIdiot"],
                "\uD83D\uDE45"
        )
)


