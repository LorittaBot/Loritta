package net.perfectdreams.loritta.morenitta.interactions.vanilla.`fun`

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import mu.KotlinLogging
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.audio.AudioSendHandler
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.channel.concrete.StageChannel
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import net.dv8tion.jda.api.managers.AudioManager
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.discord.utils.soundboard.SoundboardAudio
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.utils.LorittaColors
import net.perfectdreams.loritta.common.utils.extensions.getPathFromResources
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.commands.*
import net.perfectdreams.loritta.morenitta.interactions.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.morenitta.interactions.commands.options.OptionReference
import net.perfectdreams.loritta.morenitta.utils.extensions.await
import net.perfectdreams.loritta.morenitta.utils.extensions.getLocalizedName
import org.apache.commons.text.similarity.LevenshteinDistance
import java.nio.ByteBuffer
import java.time.Instant
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.LinkedBlockingQueue
import kotlin.io.path.readBytes

class MusicalChairsCommand(val loritta: LorittaBot) : SlashCommandDeclarationWrapper {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    private val I18N_PREFIX = I18nKeysData.Commands.Command.Musicalchairs
    private val songs = listOf(
        MusicalChairSong(
            "MC Chinelinho - O Meteoro",
            "https://youtu.be/Cn6ti1fE-Vs",
            loritta.soundboard.extractOpusFrames(
                LorittaBot::class.getPathFromResources("/musical_chairs/mc_chinelinho_o_meteoro.opus")!!.readBytes()
            )
        ),
        MusicalChairSong(
            "MC Chinelinho - Chamei os Parça",
            "https://youtu.be/0kdkI8SM2V8",
            loritta.soundboard.extractOpusFrames(
                LorittaBot::class.getPathFromResources("/musical_chairs/mc_chinelinho_chamei_os_parca.opus")!!.readBytes()
            )
        ),
        MusicalChairSong(
            "BIBI - Isolados",
            "https://youtu.be/aEk3zK2aMCs",
            loritta.soundboard.extractOpusFrames(
                LorittaBot::class.getPathFromResources("/musical_chairs/bibi_isolados.opus")!!.readBytes()
            )
        ),
        MusicalChairSong(
            "BngOficial - RAP DO MINECRAFT",
            "https://youtu.be/rQzSiiRe6YM",
            loritta.soundboard.extractOpusFrames(
                LorittaBot::class.getPathFromResources("/musical_chairs/bngoficial_rap_do_minecraft.opus")!!.readBytes()
            )
        ),
        MusicalChairSong(
            "Yudi - Funk do Yudi",
            "https://youtu.be/xBzSblZ1ZkY",
            loritta.soundboard.extractOpusFrames(
                LorittaBot::class.getPathFromResources("/musical_chairs/yudi_funk_do_yudi.opus")!!.readBytes()
            )
        ),
        MusicalChairSong(
            "Venom Extreme - Tá Ficando Apertado",
            "https://youtu.be/xdQ4Rqt8J6c",
            loritta.soundboard.extractOpusFrames(
                LorittaBot::class.getPathFromResources("/musical_chairs/venom_extreme_ta_ficando_apertado.opus")!!.readBytes()
            )
        ),
        MusicalChairSong(
            "toby fox - MEGALOVANIA",
            "https://youtu.be/c5daGZ96QGU",
            loritta.soundboard.extractOpusFrames(
                LorittaBot::class.getPathFromResources("/musical_chairs/toby_fox_megalovania.opus")!!.readBytes()
            )
        ),
        MusicalChairSong(
            "toby fox - BIG SHOT",
            "https://youtu.be/uivFFnCI8tM",
            loritta.soundboard.extractOpusFrames(
                LorittaBot::class.getPathFromResources("/musical_chairs/toby_fox_big_shot.opus")!!.readBytes()
            )
        ),
        MusicalChairSong(
            "Manoel Gomes - Caneta Azul",
            "https://youtu.be/Tw_IGPK4S_I",
            loritta.soundboard.extractOpusFrames(
                LorittaBot::class.getPathFromResources("/musical_chairs/manoel_gomes_caneta_azul.opus")!!.readBytes()
            )
        ),
        MusicalChairSong(
            "Leonz - Among Us Drip",
            "https://youtu.be/grd-K33tOSM",
            loritta.soundboard.extractOpusFrames(
                LorittaBot::class.getPathFromResources("/musical_chairs/leonz_among_us_drip.opus")!!.readBytes()
            )
        ),
        MusicalChairSong(
            "Ednaldo Pereira - Vale Nada Vale Tudo",
            "https://youtu.be/5BO7kF0zxUA",
            loritta.soundboard.extractOpusFrames(
                LorittaBot::class.getPathFromResources("/musical_chairs/ednaldo_pereira_vale_nada_vale_tudo.opus")!!.readBytes()
            )
        ),
        MusicalChairSong(
            "Pato Papão - KD FOREVER MAPA",
            "https://youtu.be/tdZ3K-eny4U",
            loritta.soundboard.extractOpusFrames(
                LorittaBot::class.getPathFromResources("/musical_chairs/pato_papao_kd_forever_mapa.opus")!!.readBytes()
            )
        ),
        MusicalChairSong(
            "Caue Moura - RAP DOS MEMES",
            "https://youtu.be/YtpATpMKDkg",
            loritta.soundboard.extractOpusFrames(
                LorittaBot::class.getPathFromResources("/musical_chairs/caue_moura_rap_dos_memes.opus")!!.readBytes()
            )
        ),
        MusicalChairSong(
            "MC Gui - O Bonde Passou",
            "https://youtu.be/IAt8--ybrKo",
            loritta.soundboard.extractOpusFrames(
                LorittaBot::class.getPathFromResources("/musical_chairs/mc_gui_o_bonde_passou.opus")!!.readBytes()
            )
        ),
        MusicalChairSong(
            "Hampton the Hampster - The HampsterDance Song",
            "https://youtu.be/H9K8-3PHZOU",
            loritta.soundboard.extractOpusFrames(
                LorittaBot::class.getPathFromResources("/musical_chairs/hampton_the_hampsterdance_song.opus")!!.readBytes()
            )
        ),
        MusicalChairSong(
            "Nomico - Bad Apple!!",
            "https://youtu.be/zPOMR2tenHE",
            loritta.soundboard.extractOpusFrames(
                LorittaBot::class.getPathFromResources("/musical_chairs/nomico_bad_apple.opus")!!.readBytes()
            )
        ),
        MusicalChairSong(
            "Popai - Funk da Winx",
            "https://youtu.be/gTguFU-iTj8",
            loritta.soundboard.extractOpusFrames(
                LorittaBot::class.getPathFromResources("/musical_chairs/popai_winx_funk.opus")!!.readBytes()
            )
        ),
        MusicalChairSong(
            "Os Leleks - Passinho do Volante",
            "https://youtu.be/YVaogUQlhI4",
            loritta.soundboard.extractOpusFrames(
                LorittaBot::class.getPathFromResources("/musical_chairs/os_leleks_passinho_do_volante.opus")!!.readBytes()
            )
        ),
        MusicalChairSong(
            "Nyan Cat",
            "https://youtu.be/QH2-TGUlwu4",
            loritta.soundboard.extractOpusFrames(
                LorittaBot::class.getPathFromResources("/musical_chairs/nyan_cat.opus")!!.readBytes()
            )
        ),
        MusicalChairSong(
            "MC Crash - Sarrada no Ar",
            "https://youtu.be/g-UjVI_nVd8",
            loritta.soundboard.extractOpusFrames(
                LorittaBot::class.getPathFromResources("/musical_chairs/mc_crash_sarrada_no_ar.opus")!!.readBytes()
            )
        ),
        MusicalChairSong(
            "DJ MP4 - The Book Is On The Table",
            "https://youtu.be/c0u-SIQw_xo",
            loritta.soundboard.extractOpusFrames(
                LorittaBot::class.getPathFromResources("/musical_chairs/dj_mp4_the_book_is_on_the_table.opus")!!.readBytes()
            )
        ),
        MusicalChairSong(
            "Funk do Pica Pau",
            "https://youtu.be/jHQis_UP4io",
            loritta.soundboard.extractOpusFrames(
                LorittaBot::class.getPathFromResources("/musical_chairs/funk_do_pica_pau.opus")!!.readBytes()
            )
        ),
        MusicalChairSong(
            "Steven Universe - Stronger Than You",
            "https://youtu.be/7GrmiTlaVgo",
            loritta.soundboard.extractOpusFrames(
                LorittaBot::class.getPathFromResources("/musical_chairs/steven_universe_stronger_than_you.opus")!!.readBytes()
            )
        ),
        MusicalChairSong(
            "Tauz - Rap do Goku",
            "https://youtu.be/FCm88wOgw7s",
            loritta.soundboard.extractOpusFrames(
                LorittaBot::class.getPathFromResources("/musical_chairs/tauz_rap_do_goku.opus")!!.readBytes()
            )
        )
    )

    private val musicalChairsIntro = loritta.soundboard.extractOpusFrames(
        LorittaBot::class.getPathFromResources("/musical_chairs/musical_chairs_intro.opus")!!.readBytes()
    )
    private val musicalChairsSessions = ConcurrentHashMap.newKeySet<Long>()

    override fun command() = slashCommand(I18N_PREFIX.Label, I18N_PREFIX.Description, CommandCategory.FUN, UUID.fromString("c7ba9746-75ab-4a39-977b-3755fdbe21f2")) {
        enableLegacyMessageSupport = true

        defaultMemberPermissions = DefaultMemberPermissions.enabledFor(Permission.VOICE_MOVE_OTHERS)
        isGuildOnly = true

        executor = MusicalChairsExecutor()
    }

    inner class MusicalChairsExecutor : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        inner class Options : ApplicationCommandOptions() {
            val song = optionalString("song", I18N_PREFIX.Options.Song.Text) {
                for (song in songs) {
                    choice(song.name, song.name)
                }
            }
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            context.deferChannelMessage(false)

            val songId = args[options.song]

            println("song id: $songId")

            val guild = context.guild

            val voiceState = context.member.voiceState
            val voiceChannel = voiceState?.channel as? AudioChannel
            if (voiceState == null || voiceChannel == null) {
                context.reply(false) {
                    styled(
                        content = context.i18nContext.get(I18N_PREFIX.YouNeedToBeConnectedToAVoiceChannelToStart),
                        prefix = Emotes.Error
                    )
                }
                return
            }

            val validParticipants = voiceChannel.members.filter { !it.user.isBot }
            if (1 >= validParticipants.size) {
                context.reply(false) {
                    styled(
                        content = context.i18nContext.get(I18N_PREFIX.YouNeedAtLeastTwoPeopleToStart),
                        prefix = Emotes.Error
                    )
                }
                return
            }

            if (musicalChairsSessions.contains(guild.idLong)) {
                context.reply(false) {
                    styled(
                        content = context.i18nContext.get(I18N_PREFIX.YouNeedToWaitTheGameToFinishToStartANewGame),
                        prefix = Emotes.Error
                    )
                }
                return
            }

            if (!guild.selfMember.hasPermission(voiceChannel, Permission.VOICE_CONNECT, Permission.VOICE_SPEAK)) {
                context.reply(false) {
                    styled(
                        content = context.i18nContext.get(
                            I18nKeysData.Commands.LoriDoesntHavePermissionDiscord(
                                listOf(Permission.VOICE_CONNECT, Permission.VOICE_SPEAK)
                                    .joinToString(", ") { "`${it.getLocalizedName(context.i18nContext)}`" }
                            )
                        ),
                        prefix = Emotes.Error
                    )
                }
                return
            }

            startMusicalChairs(
                context,
                context.i18nContext,
                guild,
                voiceChannel,
                voiceChannel.members.filter { !it.user.isBot },
                true,
                0,
                if (songId != null) { songs.first { it.name == songId } } else songs.random(),
                Mutex(),
                1
            )
        }

        private suspend fun startMusicalChairs(
            context: UnleashedContext,
            i18nContext: I18nContext,
            guild: Guild,
            audioChannel: AudioChannel,
            startingMembers: List<Member>,
            newGame: Boolean,
            timeOffset: Int,
            song: MusicalChairSong,
            mutex: Mutex,
            round: Int
        ) {
            try {
                val audioManager = guild.audioManager
                if (!newGame && (!audioManager.isConnected || audioManager.connectedChannel?.idLong != audioChannel.idLong)) {
                    musicalChairsSessions.remove(context.guildId)

                    context.reply(false) {
                        styled(
                            i18nContext.get(I18N_PREFIX.CancellingTheGameBecauseNotConnectedToChannel),
                            Emotes.LoriSob
                        )
                    }
                    return
                }

                val time = if (10 >= startingMembers.size) {
                    LorittaBot.RANDOM.nextInt(7_000, 20_000)
                } else if (15 >= startingMembers.size) {
                    LorittaBot.RANDOM.nextInt(7_000, 15_000)
                } else if (20 >= startingMembers.size) {
                    LorittaBot.RANDOM.nextInt(5_000, 15_000)
                } else {
                    LorittaBot.RANDOM.nextInt(3_000, 12_000)
                }

                val lengthOfSongSlice = time / 20
                val songFrames = song.frames
                    .drop(timeOffset / 20)
                    .take(lengthOfSongSlice)

                if (songFrames.size != lengthOfSongSlice) {
                    // If the lengthOfSongSlice is different than what we want, then it means that the song would "end" by itself, so let's switch to a new song!
                    return startMusicalChairs(
                        context,
                        i18nContext,
                        guild,
                        audioChannel,
                        startingMembers,
                        false,
                        0,
                        // Select a random song that ISN'T the current song
                        songs.toMutableList().apply {
                            this.remove(song)
                        }.random(),
                        mutex,
                        round + 1
                    )
                }

                val musicQueue = LinkedBlockingQueue(songFrames)

                val successfullyConnected = loritta.openAudioChannelAndAwaitConnection(audioManager, audioChannel)

                // Failed to connect! End the game here...
                if (!successfullyConnected) {
                    context.reply(false) {
                        styled(
                            content = context.i18nContext.get(I18N_PREFIX.FailedToConnectToTheVoiceChannel),
                            prefix = Emotes.Error
                        )
                    }
                    return
                }

                if (newGame) {
                    // Is this a stage channel?
                    if (audioChannel is StageChannel) {
                        // If yes, we will request to speak in the stage channel!
                        audioChannel.requestToSpeak().await()
                    }

                    musicalChairsSessions.add(context.guildId)

                    // New game! Let's play the intro!!
                    playSoundEffectAndWait(audioManager, audioChannel, musicalChairsIntro)
                }

                audioManager.sendingHandler = MusicalChairsAudioProvider(musicQueue)

                val participatingMembers = startingMembers
                    .associateWith { MusicalChairsState.Waiting }
                    .toMutableMap() as MutableMap<Member, MusicalChairsState>

                var restarted = false

                suspend fun handleFinish(eventTimeoutJob: Job, endedDueToTimeout: Boolean) {
                    val canRestart = participatingMembers.values.none { it is MusicalChairsState.Waiting }

                    if (canRestart && !restarted) {
                        restarted = true
                        if (!endedDueToTimeout)
                            eventTimeoutJob.cancel()

                        val membersToContinue =
                            participatingMembers.entries.filter { it.value is MusicalChairsState.Sit }.map { it.key }

                        context.chunkedReply(false) {
                            // TODO: Maybe we could figure out a way to not need those casts?
                            val sittingMembers =
                                participatingMembers.filterValues { it is MusicalChairsState.Sit } as Map<Member, MusicalChairsState.Sit>
                            val satOnLapMembers =
                                participatingMembers.filterValues { it is MusicalChairsState.SatOnLap } as Map<Member, MusicalChairsState.SatOnLap>
                            val didntWaitUntilSongStoppedMembers =
                                participatingMembers.filterValues { it is MusicalChairsState.DidntWaitUntilSongStopped } as Map<Member, MusicalChairsState.DidntWaitUntilSongStopped>
                            val tookTooLongToSit =
                                participatingMembers.filterValues { it is MusicalChairsState.TookTooLongToSit } as Map<Member, MusicalChairsState.TookTooLongToSit>

                            for ((member, _) in didntWaitUntilSongStoppedMembers) {
                                styled(
                                    i18nContext.get(I18N_PREFIX.States.DidntWaitUntilSongStopped(member.asMention)),
                                    Emotes.LoriBonk
                                )
                            }

                            val sittingMembersSortedByTime = sittingMembers.entries.sortedBy { it.value.time }
                            for ((member, _) in sittingMembersSortedByTime) {
                                val isFirst = sittingMembersSortedByTime.indexOfFirst { it.key == member } == 0

                                styled(
                                    buildString {
                                        if (isFirst)
                                            append(i18nContext.get(I18N_PREFIX.States.SitFirst(member.asMention)))
                                        else
                                            append(i18nContext.get(I18N_PREFIX.States.Sit(member.asMention)))
                                    },
                                    "\uD83E\uDE91"
                                )
                            }

                            for ((member, _) in satOnLapMembers) {
                                styled(
                                    i18nContext.get(I18N_PREFIX.States.SatOnLap(member.asMention)),
                                    Emotes.LoriFlushed
                                )
                            }

                            if (tookTooLongToSit.isNotEmpty()) {
                                val joinedMembers = tookTooLongToSit.keys.joinToString(", ") { it.asMention }
                                if (tookTooLongToSit.size == 1) {
                                    styled(
                                        i18nContext.get(I18N_PREFIX.States.TookTooLongToSit(joinedMembers)),
                                        Emotes.LoriSleeping
                                    )
                                } else {
                                    styled(
                                        i18nContext.get(I18N_PREFIX.States.TookTooLongToSitMultiple(joinedMembers)),
                                        Emotes.LoriSleeping
                                    )
                                }
                            }
                        }

                        if (membersToContinue.size == 1) {
                            context.reply(false) {
                                styled(
                                    i18nContext.get(I18N_PREFIX.UserWonTheGame(membersToContinue.first().asMention)),
                                    Emotes.LoriYay
                                )
                            }
                            playSoundEffectAndWait(audioManager, audioChannel, loritta.soundboard.getAudioClip(SoundboardAudio.ESSE_E_O_MEU_PATRAO_HEHE))

                            musicalChairsSessions.remove(context.guildId)
                            audioManager.closeAudioConnection()
                            return
                        }

                        if (membersToContinue.isEmpty()) {
                            context.reply(false) {
                                styled(
                                    i18nContext.get(I18N_PREFIX.EveryoneLostTheGame),
                                    Emotes.LoriHmpf
                                )
                            }
                            playSoundEffectAndWait(audioManager, audioChannel, loritta.soundboard.getAudioClip(SoundboardAudio.XIII))

                            musicalChairsSessions.remove(context.guildId)
                            audioManager.closeAudioConnection()
                            return
                        }

                        // We will continue!
                        if (membersToContinue.size == 2) {
                            // Only two left? Let's play dança gatinho dança!
                            playSoundEffectAndWait(audioManager, audioChannel, loritta.soundboard.getAudioClip(SoundboardAudio.DANCE_CAT_DANCE))
                        } else {
                            // If not, let's play a random sfx
                            val randomVictorySfx = listOf(
                                loritta.soundboard.getAudioClip(SoundboardAudio.IRRA),
                                loritta.soundboard.getAudioClip(SoundboardAudio.RAPAIZ),
                                loritta.soundboard.getAudioClip(SoundboardAudio.UI),
                                loritta.soundboard.getAudioClip(SoundboardAudio.UEPA),
                                loritta.soundboard.getAudioClip(SoundboardAudio.ELE_GOSTA),
                            )

                            playSoundEffectAndWait(audioManager, audioChannel, randomVictorySfx.random())
                        }

                        startMusicalChairs(
                            context,
                            i18nContext,
                            guild,
                            audioChannel,
                            membersToContinue,
                            false,
                            time + timeOffset,
                            song,
                            mutex,
                            round + 1
                        )
                    }
                }

                var eventTimeoutJob: Job? = null

                eventTimeoutJob = GlobalScope.launch {
                    delay(time + 3_000L)

                    mutex.withLock {
                        val didntSitYetMembers =
                            participatingMembers.entries.filter { it.value is MusicalChairsState.Waiting }

                        for ((didntSitYetMember, _) in didntSitYetMembers) {
                            participatingMembers[didntSitYetMember] = MusicalChairsState.TookTooLongToSit
                        }

                        handleFinish(eventTimeoutJob!!, true)
                    }
                }

                val subtractChairs = when {
                    10 >= participatingMembers.size -> 1
                    15 >= participatingMembers.size -> 2
                    20 >= participatingMembers.size -> 3
                    25 >= participatingMembers.size -> 4
                    30 >= participatingMembers.size -> 5
                    else -> 10
                }

                val availableChairs = participatingMembers.entries.size - subtractChairs

                context.reply(false) {
                    embed {
                        author(song.name, song.source)

                        title = i18nContext.get(I18N_PREFIX.Title(round))

                        description = buildString {
                            appendLine(i18nContext.get(I18N_PREFIX.PayAttentionToTheMusicTutorial(audioChannel.asMention)))
                            appendLine()
                            appendLine("**${i18nContext.get(I18N_PREFIX.Participants(participatingMembers.size))}**")
                            // We aren't going to display anything if there is more than 100 members in the voice channel, to avoid a big embed + embed size limit
                            if (100 >= participatingMembers.size) {
                                for (member in participatingMembers) {
                                    appendLine(member.key.asMention)
                                }
                            } else {
                                appendLine("*${i18nContext.get(I18N_PREFIX.TooManyParticipantesHidingList)}*")
                            }
                            appendLine()
                            append(i18nContext.get(I18N_PREFIX.AvailableChairs(availableChairs)))
                        }
                        footer(
                            i18nContext.get(I18N_PREFIX.DjArthTheRat),
                            "https://stuff.loritta.website/dj-arth.png"
                        )
                        color = LorittaColors.LorittaAqua.rgb
                    }

                    actionRow(
                        loritta.interactivityManager.button(
                            ButtonStyle.PRIMARY,
                            i18nContext.get(I18N_PREFIX.SitInTheChairWithYourButt),
                            {
                                emoji = Emoji.fromUnicode("\uD83E\uDE91")
                            }
                        ) { context ->
                            try {
                                val member = context.event.member!!

                                if (context.event.member !in participatingMembers) {
                                    context.reply(true) {
                                        styled(
                                            i18nContext.get(I18N_PREFIX.YouAreNotParticipatingInThisGame(audioChannel.asMention)),
                                            Emotes.LoriSob
                                        )
                                    }
                                    return@button
                                }

                                // We are going to defer edit but we aren't going to actually edit the message, heh
                                context.deferEdit()

                                mutex.withLock {
                                    // Don't process further buttons if the user isn't waiting
                                    if (participatingMembers[member] !is MusicalChairsState.Waiting)
                                        return@button

                                    if (musicQueue.isNotEmpty()) {
                                        participatingMembers[member] = MusicalChairsState.DidntWaitUntilSongStopped
                                        return@button
                                    }

                                    val currentlyAvailableChairs =
                                        availableChairs - participatingMembers.entries.count { it.value is MusicalChairsState.Sit }

                                    participatingMembers[member] = if (currentlyAvailableChairs == 0) {
                                        MusicalChairsState.SatOnLap(Instant.now())
                                    } else {
                                        MusicalChairsState.Sit(Instant.now())
                                    }

                                    handleFinish(eventTimeoutJob, false)
                                }
                            } catch (e: Exception) {
                                logger.warn(e) { "Something went wrong in the Musical Chairs event in ${guild.idLong}" }

                                // If something goes wrong, let's remove the current session to avoid new sessions not being able to be initiated
                                musicalChairsSessions.remove(guild.idLong)

                                context.reply(false) {
                                    styled(
                                        content = context.i18nContext.get(I18N_PREFIX.SomethingWentWrong),
                                        prefix = Emotes.LoriSob
                                    )
                                }
                            }
                        }
                    )
                }
            } catch (e: Exception) {
                logger.warn(e) { "Something went wrong in the Musical Chairs event in ${guild.idLong}" }

                // If something goes wrong, let's remove the current session to avoid new sessions not being able to be initiated
                musicalChairsSessions.remove(guild.idLong)

                context.reply(false) {
                    styled(
                        content = context.i18nContext.get(I18N_PREFIX.SomethingWentWrong),
                        prefix = Emotes.LoriSob
                    )
                }
            }
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?> {
            if (args.isEmpty())
                return emptyMap()

            val songNameInput = args.joinToString(" ")

            val songResult = songs.sortedBy {
                LevenshteinDistance.getDefaultInstance().apply(it.name, songNameInput)
            }.first()

            return mapOf(
                options.song to songResult.name
            )
        }
    }

    suspend fun playSoundEffectAndWait(audioManager: AudioManager, audioChannel: AudioChannel, frames: List<ByteArray>): Boolean {
        // Create a channel that indicates when the sfx has finished playing
        val endChannel = Channel<SoundEffectFinishedState>()
        // Change the sending handler
        audioManager.sendingHandler = MusicalChairsSoundEffectAudioProvider(LinkedBlockingQueue(frames), endChannel)

        val audioChannelCheckJob = GlobalScope.async {
            while (true) {
                val invalidChannel = !audioManager.isConnected || audioManager.connectedChannel?.idLong != audioChannel.idLong

                if (invalidChannel) {
                    endChannel.send(SoundEffectFinishedState.INVALID_CHANNEL)
                    return@async
                }

                delay(250)
            }
        }

        // Wait until the sound effect has been played
        val state = endChannel.receive()
        audioChannelCheckJob.cancel()
        return when (state) {
            SoundEffectFinishedState.SUCCESS -> true
            SoundEffectFinishedState.INVALID_CHANNEL -> false
        }
    }

    class MusicalChairsAudioProvider(val queue: LinkedBlockingQueue<ByteArray>) : AudioSendHandler {
        companion object {
            private val SILENCE = ByteBuffer.wrap(byteArrayOf())
        }

        override fun isOpus() = true
        override fun canProvide() = true

        override fun provide20MsAudio(): ByteBuffer {
            val packet = queue.poll() ?: return SILENCE

            return ByteBuffer.wrap(packet)
        }
    }

    class MusicalChairsSoundEffectAudioProvider(val queue: LinkedBlockingQueue<ByteArray>, val endChannel: Channel<SoundEffectFinishedState>) : AudioSendHandler {
        companion object {
            private val SILENCE = ByteBuffer.wrap(byteArrayOf())
        }

        override fun isOpus() = true
        override fun canProvide() = true
        var hasNotified = false

        override fun provide20MsAudio(): ByteBuffer {
            val packet = queue.poll()
            if (packet == null) {
                if (!hasNotified) {
                    hasNotified = true
                    endChannel.trySend(SoundEffectFinishedState.SUCCESS)
                }
                return SILENCE
            }

            return ByteBuffer.wrap(packet)
        }
    }

    sealed class MusicalChairsState {
        object Waiting : MusicalChairsState()
        class Sit(val time: Instant) : MusicalChairsState()
        class SatOnLap(val time: Instant) : MusicalChairsState()
        object DidntWaitUntilSongStopped : MusicalChairsState()
        object TookTooLongToSit : MusicalChairsState()
    }

    class MusicalChairSong(
        val name: String,
        val source: String?,
        val frames: List<ByteArray>
    )

    enum class SoundEffectFinishedState {
        SUCCESS,
        INVALID_CHANNEL
    }
}