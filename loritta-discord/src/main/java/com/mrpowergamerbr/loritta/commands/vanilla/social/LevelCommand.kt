package com.mrpowergamerbr.loritta.commands.vanilla.social

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.tables.GuildProfiles
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import com.mrpowergamerbr.loritta.utils.loritta
import net.perfectdreams.loritta.api.commands.CommandArguments
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.api.commands.arguments
import net.perfectdreams.loritta.api.messages.LorittaReply
import net.perfectdreams.loritta.utils.Emotes
import net.perfectdreams.loritta.utils.ExperienceUtils
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import net.dv8tion.jda.api.EmbedBuilder
import java.awt.Color

class LevelCommand : AbstractCommand("level", category = CommandCategory.SOCIAL) {

    override fun canUseInPrivateChannel(): Boolean {
        return false
    }
    override fun getDescription(locale: LegacyBaseLocale): String {
        return locale.toNewLocale()["commands.social.level.description"]
    }
    override fun getExamples(locale: LegacyBaseLocale): List<String> {
        return listOf(
                "@MrPowerGamerBR"
        )
    }

    override fun getUsage(locale: LegacyBaseLocale): CommandArguments {
        return arguments {
            locale.toNewLocale()["commands.social.level.usage"]
        }
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

        if (user.isBot) {
            context.reply(
                    LorittaReply(
                            locale.toNewLocale()["commands.social.level.thisCommandDoesNotWorkOnBots"],
                            Emotes.LORI_HMPF
                    )
            )
            return
        }

        try {
            val embed = EmbedBuilder()
                    .setAuthor(context.userHandle.asTag, context.userHandle.effectiveAvatarUrl, context.userHandle.effectiveAvatarUrl)
                    .setTitle(locale.toNewLocale()["commands.social.level.title", Emotes.LORI_KAMEHAMEHA, user.asTag])
                    .setDescription(
                                    "\n${locale.toNewLocale()["commands.social.level.currentLevel", Emotes.LORI_BARF, userLevel]}\n" +
                                    "\n${locale.toNewLocale()["commands.social.level.currentXP", Emotes.LORI_WATER, userProfile.xp]}\n" +
                                    "\n${locale.toNewLocale()["commands.social.level.ranking", Emotes.LORI_POINT, userRanking]}\n" +
                                    "\n${locale.toNewLocale()["commands.social.level.userNextLevelRequiredXp", Emotes.LORI_SOB, userNextLevel, userNextLevelRequiredXp]}\n" +
                                    "\n${locale.toNewLocale()["commands.social.level.lorotasHint", Emotes.LORI_NICE]}\n")
                    .setColor(Color.CYAN)
                    .setThumbnail(user.effectiveAvatarUrl)
            context.sendMessage(context.userHandle.asMention, embed.build())
        } catch (e: Exception) {
            context.reply(
                    LorittaReply(
                            "Ocorreu um erro ao executar este comando: `$e`",
                            Constants.ERROR
                    )
            )
        }
    }
}