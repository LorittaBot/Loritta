package net.perfectdreams.loritta.parallax.wrapper

import com.github.salomonbrys.kotson.fromJson
import net.perfectdreams.loritta.api.commands.SilentCommandException
import net.perfectdreams.loritta.parallax.ParallaxServer
import net.perfectdreams.loritta.parallax.ParallaxUtils
import org.graalvm.polyglot.Value
import org.graalvm.polyglot.proxy.ProxyObject
import java.io.File

/**
 * JavaScript's [JSCommandContext] utils
 *
 * We keep it in a separate class because JavaScript doesn't allow a function with the same name as a method
 */
class JSContextUtils(val context: JSCommandContext) {
	private var loadedDataStore: MutableMap<String, Any?>? = null

	fun user(argumentIndex: Int): User? {
		// TODO: Better implementation by retrieving from the guild members
		val argument = context.args.getOrNull(argumentIndex) ?: return null

		// Mention Check
		if (argument.startsWith("<@") && argument.endsWith(">")) {
			val userId = argument.removePrefix("<@")
					.removePrefix("!") // nicknames
					.removeSuffix(">")
					.toLongOrNull()

			if (userId != null) {
				context.message.channel.guild.members.firstOrNull { it.id == userId }?.let { return it.user }
			}
		}

		return null
	}

	fun member(argumentIndex: Int) = user(argumentIndex)

	fun fail(message: String) {
		context.message.channel.send(message)
		throw SilentCommandException()
	}

	fun fail(embed: ParallaxEmbed) {
		context.message.channel.send(embed)
		throw SilentCommandException()
	}

	fun send(message: Map<*, *>) {
		context.message.channel.send(message)
		throw SilentCommandException()
	}

	fun send(message: String, embed: ParallaxEmbed?) {
		context.message.channel.send(message, embed)
		throw SilentCommandException()
	}

	fun validateUser(user: User?): User {
		// We use "any" due to type erasure
		return user ?: run {
			context.message.channel.send(context.locale["commands.userDoesNotExist"])
			throw SilentCommandException()
		}
	}

	fun loadDataStore(): ProxyObject {
		val guildDataStore = File(ParallaxServer.dataStoreFolder, "${context.message.channel.guild.id}.json")
		val dataStore = if (guildDataStore.exists()) {
			ParallaxServer.gson.fromJson(guildDataStore.readText())
		} else {
			mutableMapOf<String, Any?>()
		}

		for (entry in dataStore) {
			if (entry.value is Map<*, *>) {
				println("${entry.key} is a Map Object! Wrapping as a proxy object...")
				entry.setValue(ParallaxUtils.ParallaxDataStoreProxy(entry.value as MutableMap<String, Any?>))
			}
		}
		this.loadedDataStore = dataStore
		return ProxyObject.fromMap(dataStore)
	}

	fun saveDataStore() {
		val guildDataStore = File(ParallaxServer.dataStoreFolder, "${context.message.channel.guild.id}.json")
		val dataStore = loadedDataStore

		if (dataStore != null && dataStore.isNotEmpty()) {
			ParallaxServer.logger.info { "Data Store is not empty, saving..." }

			println(dataStore)
			for (map in dataStore) {
				val value = map.value
				if (value is ParallaxUtils.ParallaxDataStoreProxy) {
					for (innerEntry in value.values) {
						if (innerEntry.value is Value)
							innerEntry.setValue(ParallaxUtils.convertValueToType(innerEntry.value as Value))
					}
					map.setValue(value.values)
				}
				if (map.value is Value) {
					map.setValue(ParallaxUtils.convertValueToType(map.value as Value))
				}
			}
			println(dataStore)

			// If the data store is not empty, save!
			guildDataStore.writeText(ParallaxServer.gson.toJson(dataStore))
			ParallaxServer.logger.info { "Data Store save complete!" }
		}
	}
}