package net.perfectdreams.loritta.plugin.loriguildstuff.commands

import com.mrpowergamerbr.loritta.utils.Constants
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.platform.discord.legacy.commands.discordCommand

object ColorCommand {

    // All available roles and its id's.
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
            "verde" to 374613592185634816L,
            "violeta" to 738880144403464322L,
            "laranja" to 738914237598007376L,
            "violeta claro" to 750738232735432817L,
            "preto" to 751256879534964796L,
            "verde neon" to 760681173608431647L
    )

    fun create(loritta: LorittaDiscord) = discordCommand(loritta, listOf("cor", "color"), CommandCategory.MISC) {
        hideInHelp = true

        commandCheckFilter { lorittaMessageEvent, _, _, _, _ ->
            lorittaMessageEvent.guild?.idLong == 297732013006389252L
        }

        executesDiscord {

            // Role for Loritta's donators
            val donatorRole = guild.getRoleById(364201981016801281L)!!
            val member = this.member!!

            // Checking if user is a donator
            if (!member.roles.contains(donatorRole))
                fail("Este comando é apenas para doadores, se você quer me ajudar a comprar um :custard:, então vire um doador! https://loritta.website/donate", Constants.ERROR)

            // Input role
            val selection = args.joinToString(" ").toLowerCase()
            // Selected color's role id
            val colorId = colors[selection] ?: fail("Hmm, estranho! Não encontrei a cor que você selecionou! Essas são todas as cores disponíveis: `${colors.keys.joinToString(", ")}`", "<:lori_what:626942886361038868>")

            val role = guild.getRoleById(colorId) ?: error("Role with id $colorId couldn't be found on guild ${guild.idLong}")

            // Checking if user already has the selected role
            if (!member.roles.contains(role)) {
                // Adding selected role to member
                guild.addRoleToMember(member, role).queue()

                // Removing legacy roles
                for (colorRole in member.roles) {
                    if (colors.containsValue(colorRole.idLong)) {
                        guild.removeRoleFromMember(member, colorRole).queue()
                    }
                }

                fail("Você definiu sua cor para `$selection` com sucesso!", "<:lori_wow:626942886432473098>")
            } else {
                // Removing selected role from member
                guild.removeRoleFromMember(member, role).queue()

                fail("Você removeu sua cor `$selection` com sucesso!", "<:lori_wow:626942886432473098>")
            }
        }
    }
}
