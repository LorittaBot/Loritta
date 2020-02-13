package net.perfectdreams.loritta.platform.nodejs

import eris.Eris
import eris.Message
import io.ktor.client.HttpClient
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.perfectdreams.loritta.api.LorittaBot
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.api.commands.command
import net.perfectdreams.loritta.api.messages.LorittaReply
import net.perfectdreams.loritta.api.platform.PlatformFeature
import net.perfectdreams.loritta.api.utils.currentTimeMillis
import net.perfectdreams.loritta.platform.nodejs.commands.JSCommandMap
import net.perfectdreams.loritta.platform.nodejs.plugin.JSPluginManager
import net.perfectdreams.loritta.platform.nodejs.utils.JSLorittaAssets
import net.perfectdreams.loritta.platform.nodejs.utils.tobias
import net.perfectdreams.loritta.plugin.rosbife.RosbifePlugin
import version
import kotlin.random.Random

class LorittaNodeJS : LorittaBot() {
	override val supportedFeatures = PlatformFeature.values().toList()
	override val commandMap = JSCommandMap(this).apply {
		this.register(
				command(this@LorittaNodeJS, "TestCommand", listOf("test"), CommandCategory.MAGIC) {
					executes {
						reply(
								LorittaReply(
										"Hello from Eris! Node.js version: ${version}; Kotlin version: ${KotlinVersion.CURRENT}; Eris version: 0.11.2"
								)
						)
					}
				}
		)
	}
	override val pluginManager = JSPluginManager(this)
	override val assets = JSLorittaAssets()
	override val http: HttpClient
		get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
	override val random = Random(currentTimeMillis)

	fun start() {
		val bot = Eris(tobias)

		bot.on("messageCreate") { msg: Message ->
			console.log(msg.content)

			GlobalScope.launch {
				commandMap.dispatch(msg)
			}
		}

		bot.connect()

		pluginManager.loadPlugin(RosbifePlugin("Rosbife", this))
	}
}