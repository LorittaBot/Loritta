package com.mrpowergamerbr.loritta.commands.vanilla.magic

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import me.lucko.spark.common.CommandHandler
import me.lucko.spark.sampler.ThreadDumper
import me.lucko.spark.sampler.TickCounter

class ProfilerCommand : AbstractCommand("profiler", category = CommandCategory.MAGIC, onlyOwner = true) {
	override fun getDescription(locale: BaseLocale): String {
		return "Profiler"
	}

	override fun run(context: CommandContext, locale: BaseLocale) {
		val handler = SparkCommandHandler(context)
		handler.handleCommand(context, context.rawArgs)
	}

	class SparkCommandHandler(val context: CommandContext) : CommandHandler<CommandContext>() {
		override fun getVersion(): String {
			return "1.0.0"
		}

		override fun getLabel(): String {
			return "spark"
		}

		override fun sendMessage(context: CommandContext, message: String) {
			context.sendMessage(message)
		}

		override fun sendMessage(message: String) {
			context.sendMessage(message)
		}

		override fun sendLink(url: String) {
			context.sendMessage(url)
		}

		override fun runAsync(r: Runnable) {
			r.run()
		}

		override fun getDefaultThreadDumper(): ThreadDumper {
			return ThreadDumper.ALL
		}

		override fun newTickCounter(): TickCounter {
			throw UnsupportedOperationException()
		}
	}
}