package com.mrpowergamerbr.loritta.commands.vanilla.magic

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.lorittaShards
import kotlinx.coroutines.runBlocking
import me.lucko.spark.common.CommandHandler
import me.lucko.spark.sampler.ThreadDumper
import me.lucko.spark.sampler.TickCounter

class ProfilerCommand : AbstractCommand("profiler", category = CommandCategory.MAGIC, onlyOwner = true) {
	val handler = SparkCommandHandler()

	override fun getDescription(locale: BaseLocale): String {
		return "Profiler"
	}

	override suspend fun run(context: CommandContext,locale: BaseLocale) {
		handler.handleCommand(context, context.rawArgs)
	}

	class SparkCommandHandler : CommandHandler<CommandContext>() {
		override fun getVersion(): String {
			return "1.0.0"
		}

		override fun getLabel(): String {
			return "spark"
		}

		override fun sendMessage(context: CommandContext, message: String) {
			runBlocking {
				context.sendMessage(message)
			}
		}

		override fun sendMessage(message: String) {
			val user = lorittaShards.getUserById(Loritta.config.ownerId)

			if (user == null) {
				println(message)
				return
			}

			user.openPrivateChannel().queue {
				it.sendMessage(message).queue()
			}
		}

		override fun sendLink(url: String) {
			val user = lorittaShards.getUserById(Loritta.config.ownerId)

			if (user == null) {
				println(url)
				return
			}

			user.openPrivateChannel().queue {
				it.sendMessage(url).queue()
			}
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