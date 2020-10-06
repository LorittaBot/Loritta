package com.mrpowergamerbr.loritta.commands.vanilla.social

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.tables.GuildProfiles
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import com.mrpowergamerbr.loritta.utils.loritta
import net.dv8tion.jda.api.EmbedBuilder
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.utils.Emotes
import net.perfectdreams.loritta.utils.ExperienceUtils
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.awt.Color

class LevelCommand : AbstractCommand("level", category = CommandCategory.SOCIAL) {

    override fun canUseInPrivateChannel(): Boolean {
        return false
    }
    override fun getDescription(locale: LegacyBaseLocale): String {
        return "Veja seu XP!"
    }
    override fun getExamples(locale: LegacyBaseLocale): List<String> {
        return listOf(
                "@Loritta"
        )
    }

    override fun getUsage(): String {
        return "<usuário>"
    }
    override suspend fun run(context: CommandContext, locale: LegacyBaseLocale) {

        val user = context.getUserAt(0) ?: context.userHandle
        val userProfile = loritta.getLorittaProfile(user.idLong)!!
        val userRanking = newSuspendedTransaction {
            GuildProfiles.select {
                GuildProfiles.guildId eq context.guild.idLong and
                        (GuildProfiles.xp greaterEq userProfile.xp)
            }.count()
        }

        val userLevel = userProfile.getCurrentLevel().currentLevel
        val userNextLevel = userLevel.plus(1)
        val userNextLevelRequiredXp = ExperienceUtils.getHowMuchExperienceIsLeftToLevelUp(userProfile.xp, userNextLevel)

        try {
            val embed = EmbedBuilder()
                    .setAuthor(context.userHandle.asTag, context.userHandle.effectiveAvatarUrl, context.userHandle.effectiveAvatarUrl)
                    .setTitle("${Emotes.LORI_KAMEHAMEHA} **| Profile Card de `${user.asTag}`**")
                    .setDescription("\n${Emotes.LORI_BARF} **| Nível atual: `${userLevel}`**\n${Emotes.LORI_WATER} **| XP Atual:** `${userProfile.xp}`\n${Emotes.LORI_POINT} **| Colocação:** `#${userRanking}`\n${Emotes.LORI_SOB} **| XP necessário para o próximo nível (${userNextLevel}):** `${userNextLevelRequiredXp}`\n> ${Emotes.LORI_NICE} • **Dica da Lorota Jubinha:** continue conversando para passar de nível. Eu sei que você vai conseguir!\n")
                    .setColor(Color.CYAN)
                    .setThumbnail(user.effectiveAvatarUrl)
            context.sendMessage(context.userHandle.asMention, embed.build())
        } catch (e: Exception) {
            context.sendMessage("`$e`")
        }
    }
}