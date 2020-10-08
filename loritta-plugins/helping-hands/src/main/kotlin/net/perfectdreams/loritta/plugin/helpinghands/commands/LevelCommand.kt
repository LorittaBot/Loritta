package net.perfectdreams.loritta.plugin.helpinghands.commands

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.tables.GuildProfiles
import com.mrpowergamerbr.loritta.utils.Constants
import kotlinx.coroutines.sync.Mutex
import net.dv8tion.jda.api.EmbedBuilder
import net.perfectdreams.loritta.api.LorittaBot
import net.perfectdreams.loritta.api.commands.ArgumentType
import net.perfectdreams.loritta.api.commands.Command
import net.perfectdreams.loritta.api.commands.CommandContext
import net.perfectdreams.loritta.api.commands.arguments
import net.perfectdreams.loritta.api.messages.LorittaReply
import net.perfectdreams.loritta.platform.discord.commands.DiscordCommandContext
import net.perfectdreams.loritta.plugin.helpinghands.HelpingHandsPlugin
import net.perfectdreams.loritta.plugin.helpinghands.commands.base.DSLCommandBase
import net.perfectdreams.loritta.utils.Emotes
import net.perfectdreams.loritta.utils.ExperienceUtils
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

object LevelCommand : DSLCommandBase {
    val mutex = Mutex()

    override fun command(plugin: HelpingHandsPlugin, loritta: LorittaBot): Command<CommandContext> = create(loritta, listOf("level")) {
        description {
            it["commands.social.level.description"]
        }

        examples {
            listOf(
                    "@MrPowerGamerBR"
            )
        }

        usage {
            arguments {
                argument(ArgumentType.USER) {}
            }
        }

        this.canUseInPrivateChannel = false

        executes {
            loritta as Loritta

            val context = checkType<DiscordCommandContext>(this)

            var userProfile = context.lorittaUser.profile

            val contextUser = context.user(0)?.handle
            val user = contextUser ?: context.user

            if (contextUser != null) {
                userProfile = loritta.getOrCreateLorittaProfile(contextUser.idLong)
            }

            val userRanking = newSuspendedTransaction {
                GuildProfiles.select {
                    GuildProfiles.guildId eq context.guild.idLong and
                            (GuildProfiles.xp greaterEq userProfile.xp)
                }.count()
            }

            val userLevel = userProfile.getCurrentLevel().currentLevel
            val userNextLevel = userLevel + 1
            val userNextLevelRequiredXp = ExperienceUtils.getHowMuchExperienceIsLeftToLevelUp(userProfile.xp, userNextLevel)

            if (user.isBot) {
                context.reply(
                        LorittaReply(
                                locale["commands.social.level.thisCommandDoesNotWorkOnBots"],
                                Emotes.LORI_HMPF
                        )
                )
                return@executes
            }

            val embed = EmbedBuilder()
                    .setTitle(locale["commands.social.level.title", Emotes.LORI_KAMEHAMEHA, user.asTag])
                    .setDescription(
                            "\n${locale["commands.social.level.currentLevel", Emotes.LORI_BARF, userLevel]}\n" +
                                    "\n${locale["commands.social.level.currentXP", Emotes.LORI_WATER, userProfile.xp]}\n" +
                                    "\n${locale["commands.social.level.ranking", Emotes.LORI_POINT, userRanking]}\n" +
                                    "\n${locale["commands.social.level.userNextLevelRequiredXp", Emotes.LORI_SOB, userNextLevel, userNextLevelRequiredXp]}\n" +
                                    "\n${locale["commands.social.level.lorotasHint", Emotes.LORI_NICE]}\n")
                    .setColor(Constants.DISCORD_BLURPLE)
                    .setThumbnail(user.effectiveAvatarUrl)
            context.sendMessage(user.asMention, embed.build())
        }
    }
}