package net.perfectdreams.loritta.commands.actions

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.LorittaLauncher
import com.mrpowergamerbr.loritta.dao.Profile
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.LoriReply
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.locale.Gender
import com.mrpowergamerbr.loritta.utils.onReactionAdd
import com.mrpowergamerbr.loritta.utils.removeAllFunctions
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.User
import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.loritta.api.commands.*
import net.perfectdreams.loritta.platform.discord.entities.DiscordCommandContext
import org.jetbrains.exposed.sql.transactions.transaction
import java.awt.Color
import java.io.File

abstract class ActionCommand(labels: Array<String>) : LorittaCommand(labels, CommandCategory.ACTION) {
    override val needsToUploadFiles = true

    abstract fun getResponse(locale: BaseLocale, first: User, second: User): String
    abstract fun getFolderName(): String
    abstract fun getEmoji(): String
    abstract fun getEmbedColor(): Color

    val cachedGifs by lazy {
        val list = mutableListOf<CachedGif>()

        val folder = File(Loritta.ASSETS, "actions/${getFolderName()}")
        val folderNames = listOf(
                Constants.ACTION_GENERIC,
                Constants.ACTION_MALE_AND_FEMALE,
                Constants.ACTION_FEMALE_AND_MALE,
                Constants.ACTION_MALE_AND_MALE,
                Constants.ACTION_FEMALE_AND_FEMALE,
                Constants.ACTION_BOTH
        )

        folderNames.forEach {
            val gifs = File(folder, it).listFiles().filter { it.extension == "gif" || it.extension == "png" }

            gifs.forEach { file ->
                list.add(
                        CachedGif(
                                it,
                                file.name
                        )
                )
            }
        }

        list
    }

    override fun getUsage(locale: BaseLocale): CommandArguments {
        return arguments {
            argument(ArgumentType.USER) {
                optional = false
            }
        }
    }

    override fun getExamples(locale: BaseLocale): List<String> {
        return locale.getList("commands.actions.examples")
    }

    private fun getGifsFor(userGender: Gender, receiverGender: Gender): List<CachedGif> {
        val folderNames = userGender.getValidActionFolderNames(receiverGender).toMutableList()
        if (folderNames.size != 1 && Loritta.RANDOM.nextBoolean()) // Remover "generic", para evitar muitas gifs repetidas
            folderNames.remove("generic")

        return cachedGifs.filter { folderNames.any { folderName -> folderName == it.folderName } }
    }

    suspend fun runAction(context: DiscordCommandContext, user: User, userProfile: Profile?, receiver: User, receiverProfile: Profile?) {
        val response: String
        val locale = context.locale
        val senderProfile = userProfile ?: LorittaLauncher.loritta.getLorittaProfile(user.id)
        val recProfile = receiverProfile ?: LorittaLauncher.loritta.getLorittaProfile(receiver.id)

        // Anti-gente idiota
        if (this is KissCommand && receiver.id == LorittaLauncher.loritta.discordConfig.discord.clientId) {
            context.reply(
                    LoriReply(
                            locale["commands.actions.kiss.responseAntiIdiot"],
                            "\uD83D\uDE45"
                    )
            )
            return
        }

        // R U a boy or girl?
        val userGender = transaction(Databases.loritta) { senderProfile?.settings?.gender ?: Gender.UNKNOWN }
        val receiverGender = transaction(Databases.loritta) { recProfile?.settings?.gender ?: Gender.UNKNOWN  }

        response = getResponse(locale, user, receiver)

        // Quem tentar estapear a Loritta, vai ser estapeado
        var files = if ((this is SlapCommand || this is AttackCommand || this is KissCommand) && receiver.id == LorittaLauncher.loritta.discordConfig.discord.clientId) {
            getGifsFor(receiverGender, userGender)
        } else {
            getGifsFor(userGender, receiverGender)
        }

        while (files.isEmpty()) {
            // Caso não tenha nenhuma GIF disponível, vamos abrir o nosso "leque" de GIFs, para evitar que dê erro
            files = getGifsFor(Gender.UNKNOWN, Gender.UNKNOWN)
        }

        val randomImage = files.random()

        val message = context.sendMessage(
                user.asMention,
                EmbedBuilder()
                        .setDescription("${getEmoji()} $response")
                        .setColor(getEmbedColor())
                        .setImage(loritta.instanceConfig.loritta.website.url + "assets/img/actions/${getFolderName()}/${randomImage.folderName}/${randomImage.fileName}")
                        .setFooter(locale["commands.actions.clickToRetribute", "\uD83D\uDD01"], null)
                        .build()
        ).handle

        if (user != receiver) {
            val userThatSentTheHug = user

            // Para evitar floods de actions, nós apenas iremos adicionar a reação *caso* o usuário tenha usado o comando em outra pessoa
            // Se fosse na mesma, o usuário pode ficar clicando na reação para "spammar" mensagens
            message.addReaction("\uD83D\uDD01").queue()

            message.onReactionAdd(context) {
                val user = it.user ?: return@onReactionAdd

                if (it.reactionEmote.name == "\uD83D\uDD01" && user.id == receiver.id) {
                    message.removeAllFunctions()

                    runAction(context, receiver, null, userThatSentTheHug, context.lorittaUser.profile)
                }
            }
        }
    }

    @Subcommand
    suspend fun root(context: DiscordCommandContext, locale: BaseLocale) {
        if (context.args.isNotEmpty()) {
            val user = context.getUserAt(0)

            if (user == null) {
                context.reply(
                        LoriReply(
                                locale["commands.userDoesNotExist", "`${context.args[0]}`"],
                                Constants.ERROR
                        )
                )
                return
            }

            runAction(context, context.userHandle, context.lorittaUser.profile, user, null)
        } else {
            context.explain()
        }
    }

    data class CachedGif(
            val folderName: String,
            val fileName: String
    )
}