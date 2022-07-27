package net.perfectdreams.loritta.cinnamon.platform.commands.`fun`.soundbox

import dev.kord.common.entity.ButtonStyle
import net.perfectdreams.discordinteraktions.common.builder.message.actionRow
import net.perfectdreams.loritta.cinnamon.common.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.platform.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.platform.commands.*
import net.perfectdreams.discordinteraktions.common.commands.options.SlashCommandArguments
import net.perfectdreams.loritta.cinnamon.platform.components.interactiveButton
import net.perfectdreams.loritta.cinnamon.pudding.data.notifications.*

class SoundboardBoardExecutor(loritta: LorittaCinnamon) : CinnamonSlashCommandExecutor(loritta) {
    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
        if (context !is GuildApplicationCommandContext) // Only guilds
            return

        context.sendEphemeralMessage {
            styled(
                "Isto é uma funcionalidade super hiper mega ultra experimental e ela pode *explodir* a qualquer momento! Ela ainda não está pronta e será melhorada com o passar do tempo... ou talvez até mesmo removida! ${Emotes.LoriSob}",
                Emotes.LoriMegaphone
            )
        }

        context.sendEphemeralMessage {
            content = "Clique em um som!"

            actionRow {
                interactiveButton(
                    ButtonStyle.Primary,
                    PlayAudioClipButtonExecutor,
                    loritta.encodeDataForComponentOrStoreInDatabase(
                        PlayAudioClipData(
                            SoundboardAudio.AMONG_US_ROUND_START
                        )
                    )
                ) {
                    label = "amogus"
                }

                interactiveButton(
                    ButtonStyle.Primary,
                    PlayAudioClipButtonExecutor,
                    loritta.encodeDataForComponentOrStoreInDatabase(
                        PlayAudioClipData(
                            SoundboardAudio.RAPAIZ
                        )
                    )
                ) {
                    label = "rapaiz"
                }

                interactiveButton(
                    ButtonStyle.Primary,
                    PlayAudioClipButtonExecutor,
                    loritta.encodeDataForComponentOrStoreInDatabase(
                        PlayAudioClipData(
                            SoundboardAudio.CHAVES_RISADAS,
                        )
                    )
                ) {
                    label = "risadas"
                }

                interactiveButton(
                    ButtonStyle.Primary,
                    PlayAudioClipButtonExecutor,
                    loritta.encodeDataForComponentOrStoreInDatabase(
                        PlayAudioClipData(
                            SoundboardAudio.DANCE_CAT_DANCE,
                        )
                    )
                ) {
                    label = "Dança Gatinho Dança"
                }

                interactiveButton(
                    ButtonStyle.Primary,
                    PlayAudioClipButtonExecutor,
                    loritta.encodeDataForComponentOrStoreInDatabase(
                        PlayAudioClipData(
                            SoundboardAudio.ESSE_E_O_MEU_PATRAO_HEHE,
                        )
                    )
                ) {
                    label = "Esse é o meu patrão hehe"
                }
            }

            actionRow {
                interactiveButton(
                    ButtonStyle.Primary,
                    PlayAudioClipButtonExecutor,
                    loritta.encodeDataForComponentOrStoreInDatabase(
                        PlayAudioClipData(
                            SoundboardAudio.IRRA,
                        )
                    )
                ) {
                    label = "Irrá"
                }

                interactiveButton(
                    ButtonStyle.Primary,
                    PlayAudioClipButtonExecutor,
                    loritta.encodeDataForComponentOrStoreInDatabase(
                        PlayAudioClipData(
                            SoundboardAudio.RATINHO,
                        )
                    )
                ) {
                    label = "RATINHO!"
                }

                interactiveButton(
                    ButtonStyle.Primary,
                    PlayAudioClipButtonExecutor,
                    loritta.encodeDataForComponentOrStoreInDatabase(
                        PlayAudioClipData(
                            SoundboardAudio.UEPA,
                        )
                    )
                ) {
                    label = "Uepâ!"
                }

                interactiveButton(
                    ButtonStyle.Primary,
                    PlayAudioClipButtonExecutor,
                    loritta.encodeDataForComponentOrStoreInDatabase(
                        PlayAudioClipData(
                            SoundboardAudio.UI,
                        )
                    )
                ) {
                    label = "Uí!"
                }

                interactiveButton(
                    ButtonStyle.Primary,
                    PlayAudioClipButtonExecutor,
                    loritta.encodeDataForComponentOrStoreInDatabase(
                        PlayAudioClipData(
                            SoundboardAudio.NICELY_DONE_CHEER,
                        )
                    )
                ) {
                    label = "Aplausos #1"
                }
            }
        }
    }
}