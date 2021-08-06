package net.perfectdreams.loritta.commands.`fun`

import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import net.perfectdreams.loritta.commands.`fun`.declarations.RateWaifuCommand
import net.perfectdreams.loritta.common.commands.CommandArguments
import net.perfectdreams.loritta.common.commands.CommandContext
import net.perfectdreams.loritta.common.commands.CommandExecutor
import net.perfectdreams.loritta.common.commands.declarations.CommandExecutorDeclaration
import net.perfectdreams.loritta.common.commands.options.CommandOptions
import net.perfectdreams.loritta.common.emotes.Emotes
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import kotlin.random.Random

class RateWaifuExecutor(val emotes: Emotes, val textConverter: TextConverter) : CommandExecutor() {
    companion object : CommandExecutorDeclaration(RateWaifuExecutor::class) {
        object Options : CommandOptions() {
            val waifu = string("waifu", LocaleKeyData("${RateWaifuCommand.LOCALE_PREFIX}.selectWaifu"))
                .register()
        }

        override val options = Options
    }

    override suspend fun execute(context: CommandContext, args: CommandArguments) {
        val waifu = textConverter.convert(context, args[options.waifu])

        val waifuLowerCase = waifu.lowercase()

        // Always use the same seed for the random generator, but change it every day
        val random = Random(Clock.System.now().toLocalDateTime(TimeZone.UTC).dayOfYear + waifuLowerCase.hashCode().toLong())
        val nota = random.nextInt(0, 11)

        val scoreReason = context.locale.getList("${RateWaifuCommand.LOCALE_PREFIX}.note${nota}").random()

        var reason = when (nota) {
            10 -> "$scoreReason ${emotes.loriWow}"
            9 -> "$scoreReason ${emotes.loriHeart}"
            8 -> "$scoreReason ${emotes.loriPat}"
            7 -> "$scoreReason ${emotes.loriSmile}"
            3 -> "$scoreReason ${emotes.loriShrug}"
            2 -> "$scoreReason ${emotes.loriHmpf}"
            1 -> "$scoreReason ${emotes.loriRage}"
            else -> scoreReason
        }

        var strNota = nota.toString()
        if (waifuLowerCase == "loritta" || waifuLowerCase == "lori") {
            strNota = "∞"
            reason = "${context.locale.getList("${RateWaifuCommand.LOCALE_PREFIX}.noteLoritta").random()} ${emotes.loriYay}"
        }
        if (waifuLowerCase == "pollux") {
            strNota = "10"
            reason = context.locale.getList("${RateWaifuCommand.LOCALE_PREFIX}.notePollux").random()
        }
        if (waifuLowerCase == "pantufa") {
            strNota = "10"
            reason = context.locale.getList("${RateWaifuCommand.LOCALE_PREFIX}.notePantufa").random() + " ${emotes.loriHeart}"
        }
        if (waifuLowerCase == "tatsumaki") {
            strNota = "10"
            reason = context.locale.getList("${RateWaifuCommand.LOCALE_PREFIX}.noteTatsumaki").random()
        }
        if (waifuLowerCase == "mee6") {
            strNota = "10"
            reason = context.locale.getList("${RateWaifuCommand.LOCALE_PREFIX}.noteMee6").random()
        }
        if (waifuLowerCase == "mantaro") {
            strNota = "10"
            reason = context.locale.getList("${RateWaifuCommand.LOCALE_PREFIX}.noteMantaro").random()
        }
        if (waifuLowerCase == "dyno") {
            strNota = "10"
            reason = context.locale.getList("${RateWaifuCommand.LOCALE_PREFIX}.noteDyno").random()
        }
        if (waifuLowerCase == "mudae") {
            strNota = "10"
            reason = context.locale.getList("${RateWaifuCommand.LOCALE_PREFIX}.noteMudae").random()
        }
        if (waifuLowerCase == "nadeko") {
            strNota = "10"
            reason = context.locale.getList("${RateWaifuCommand.LOCALE_PREFIX}.noteNadeko").random()
        }
        if (waifuLowerCase == "unbelievaboat") {
            strNota = "10"
            reason = context.locale.getList("${RateWaifuCommand.LOCALE_PREFIX}.noteUnbelievaBoat").random()
        }
        if (waifuLowerCase == "chino kafuu") {
            strNota = "10"
            reason = context.locale.getList("${RateWaifuCommand.LOCALE_PREFIX}.noteChinoKafuu").random()
        }
        if (waifuLowerCase == "groovy") {
            strNota = "10"
            reason = context.locale.getList("${RateWaifuCommand.LOCALE_PREFIX}.noteGroovy").random()
        }
        if (waifuLowerCase == "lorita" || waifuLowerCase == "lorrita") {
            strNota = "-∞"
            reason = "${context.locale.getList("${RateWaifuCommand.LOCALE_PREFIX}.noteLorrita").random()} ${emotes.loriHmpf}"
        }

        // TODO: Fix stripCodeMarks, maybe implement a safer way to sanitize user input?
        context.sendReply(
            content = context.locale["${RateWaifuCommand.LOCALE_PREFIX}.result", strNota, waifu, reason],
            prefix = "\uD83E\uDD14"
        )
    }

    /**
     * Interface used to convert an input to an output
     *
     * This is used in the [RateWaifuExecutor] to convert platform-specific mentions into plain text
     */
    interface TextConverter {
        suspend fun convert(context: CommandContext, input: String): String
    }

    /**
     * An noop convert operation, always returns the input
     */
    class NoopTextConverter : TextConverter {
        override suspend fun convert(context: CommandContext, input: String) = input
    }
}