package net.perfectdreams.loritta.morenitta.interactions.vanilla.`fun`

import kotlinx.coroutines.sync.Mutex
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.commands.*
import net.perfectdreams.loritta.morenitta.interactions.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.morenitta.interactions.commands.options.OptionReference
import net.perfectdreams.loritta.morenitta.utils.extensions.getLocalizedName
import net.perfectdreams.loritta.morenitta.utils.musicalchairs.MusicalChairsManager
import org.apache.commons.text.similarity.LevenshteinDistance
import java.util.*

class MusicalChairsCommand(val loritta: LorittaBot) : SlashCommandDeclarationWrapper {
    override fun command() = slashCommand(MusicalChairsManager.I18N_PREFIX.Label, MusicalChairsManager.I18N_PREFIX.Description, CommandCategory.FUN, UUID.fromString("c7ba9746-75ab-4a39-977b-3755fdbe21f2")) {
        enableLegacyMessageSupport = true

        defaultMemberPermissions = DefaultMemberPermissions.enabledFor(Permission.VOICE_MOVE_OTHERS)
        isGuildOnly = true

        executor = MusicalChairsExecutor()
    }

    inner class MusicalChairsExecutor : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        inner class Options : ApplicationCommandOptions() {
            val song = optionalString("song", MusicalChairsManager.I18N_PREFIX.Options.Song.Text) {
                for (song in loritta.musicalChairsManager.songs) {
                    choice(song.name, song.name)
                }
            }
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            context.deferChannelMessage(false)

            val songId = args[options.song]

            val guild = context.guild

            val voiceState = context.member.voiceState
            val voiceChannel = voiceState?.channel as? AudioChannel
            if (voiceState == null || voiceChannel == null) {
                context.reply(false) {
                    styled(
                        content = context.i18nContext.get(MusicalChairsManager.I18N_PREFIX.YouNeedToBeConnectedToAVoiceChannelToStart),
                        prefix = Emotes.Error
                    )
                }
                return
            }

            val validParticipants = voiceChannel.members.filter { !it.user.isBot }
            if (1 >= validParticipants.size) {
                context.reply(false) {
                    styled(
                        content = context.i18nContext.get(MusicalChairsManager.I18N_PREFIX.YouNeedAtLeastTwoPeopleToStart),
                        prefix = Emotes.Error
                    )
                }
                return
            }

            if (loritta.musicalChairsManager.musicalChairsSessions.contains(guild.idLong)) {
                context.reply(false) {
                    styled(
                        content = context.i18nContext.get(MusicalChairsManager.I18N_PREFIX.YouNeedToWaitTheGameToFinishToStartANewGame),
                        prefix = Emotes.Error
                    )
                }
                return
            }

            if (!guild.selfMember.hasPermission(voiceChannel, Permission.VOICE_CONNECT, Permission.VOICE_SPEAK)) {
                context.reply(false) {
                    styled(
                        content = context.i18nContext.get(
                            I18nKeysData.Commands.LoriDoesntHavePermissionDiscord(listOf(Permission.VOICE_CONNECT, Permission.VOICE_SPEAK).joinToString(", ") { "`${it.getLocalizedName(context.i18nContext)}`" }
                            )
                        ),
                        prefix = Emotes.Error
                    )
                }
                return
            }

            loritta.musicalChairsManager.startMusicalChairs(
                MusicalChairsManager.MusicalChairsContext.MusicalUnleashedContext(context),
                context.i18nContext,
                guild,
                voiceChannel,
                voiceChannel.members.filter { !it.user.isBot },
                true,
                0,
                if (songId != null) { loritta.musicalChairsManager.songs.first { it.name == songId } } else loritta.musicalChairsManager.songs.random(),
                Mutex(),
                1
            )
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?> {
            if (args.isEmpty())
                return emptyMap()

            val songNameInput = args.joinToString(" ")

            val songResult = loritta.musicalChairsManager.songs.sortedBy {
                LevenshteinDistance.getDefaultInstance().apply(it.name, songNameInput)
            }.first()

            return mapOf(
                options.song to songResult.name
            )
        }
    }
}