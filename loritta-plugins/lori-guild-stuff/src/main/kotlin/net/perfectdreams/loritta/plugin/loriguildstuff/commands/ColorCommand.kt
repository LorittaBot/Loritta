package net.perfectdreams.loritta.plugin.loriguildstuff.commands

import com.mrpowergamerbr.loritta.utils.extensions.await
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.platform.discord.commands.discordCommand
import net.perfectdreams.loritta.api.messages.LorittaReply

object ColorCommand {

    private val colors = hashMapOf(
            "azul claro" to 373539846620315648L,
            "vermelho escuro" to 373540076095012874L,
            "verde escuro" to 374613624536170500L,
            "rosa choque" to 411235044842012674L,
            "rosa escuro" to 374614002707333120L,
            "azul escuro" to 373539894259351553L,
            "rosa claro" to 374613958608551936L,
            "vermelho" to 373540030053875713L,
            "amarelo" to 373539918863007745L,
            "dourado" to 373539973984550912L,
            "verde" to 374613592185634816L
    )

    fun create(loritta: LorittaDiscord) = discordCommand(loritta, listOf("cor", "color"), CommandCategory.MISC) {
        this.hideInHelp = true
        this.commandCheckFilter { lorittaMessageEvent, _, _, _, _ ->
            lorittaMessageEvent.guild?.idLong == 297732013006389252L
        }

        executesDiscord {

            val donatorRole = guild.getRoleById(364201981016801281L)!! //364201981016801281L - role original
            val member = this.member!!

            if (!member.roles.contains(donatorRole)) {
                reply(
                        LorittaReply(
                                "Este comando é apenas para doadores, se você quer me ajudar a comprar um :custard:, então vire um doador! https://loritta.website/donate",
                                "<:error:412585701054611458>"
                        )
                )
            } else {
                val selection = args.joinToString(" ")

                val c = colors[selection]

                if (colors[selection] != null) {

                    if (c != null) {
                        val role = guild.getRoleById(c)!!

                        if (member.roles.contains(role)) {
                            guild.removeRoleFromMember(member, role).await()
                            reply (
                                    LorittaReply(
                                            "Cor removida!",
                                            "\uD83C\uDFA8"
                                    )
                            )
                        } else {
                            guild.addRoleToMember(member, role).await()
                            reply (
                                    LorittaReply(
                                            "Cor adicionada!",
                                            "\uD83C\uDFA8"
                                    )
                            )
                        }
                    }
                }

                var list = colors.keys.joinToString(", ")

                if (args.isEmpty()) {
                    reply (
                            LorittaReply(
                                    "Cores disponíveis: `$list`",
                                    "\uD83C\uDFA8"
                            )
                    )
                }
            }
        }
    }
}