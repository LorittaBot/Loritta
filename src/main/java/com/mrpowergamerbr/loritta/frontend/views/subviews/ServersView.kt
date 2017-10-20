package com.mrpowergamerbr.loritta.frontend.views.subviews

import com.mrpowergamerbr.loritta.frontend.evaluate
import com.mrpowergamerbr.loritta.utils.lorittaShards
import org.jooby.Request
import org.jooby.Response

class ServersView : AbstractView() {
	override fun handleRender(req: Request, res: Response, variables: MutableMap<String, Any?>): Boolean {
		return req.path() == "/servers"
	}

	override fun render(req: Request, res: Response, variables: MutableMap<String, Any?>): String {
		val guilds = lorittaShards.getGuilds()
		variables["serversSortedByMembers"] = guilds.sortedByDescending { it.members.size - it.members.count { it.user.isBot } }
		var members = 0
		var realUsers = 0
		var bots = 0
		var botCollections = 0
		var realAverage = 0
		var realAverageBots = 0
		var realAverageServerCount = 0

		guilds.forEach {
			members += it.members.size
			realUsers += it.members.count { !it.user.isBot }
			bots += it.members.count { it.user.isBot }
			botCollections += if (it.members.count { it.user.isBot } / 3 > it.members.count { !it.user.isBot }) { 1 } else { 0 }
			if (it.members.count { !it.user.isBot } > it.members.count { it.user.isBot } / 3) {
				realAverage += it.members.count { !it.user.isBot }
				realAverageBots += it.members.count { it.user.isBot }
				realAverageServerCount++;
			}
		}

		var averageTotal = realUsers / guilds.size
		var realAverageTotal = realAverage / realAverageServerCount
		var realAverageBotsTotal = realAverageBots / realAverageServerCount
		var averageBots = bots / guilds.size

		variables["averageTotal"] = averageTotal
		variables["realAverageTotal"] = realAverageTotal
		variables["realAverageBots"] = realAverageBotsTotal
		variables["averageBots"] = averageBots
		variables["botCollections"] = botCollections

		return evaluate("server_list.html", variables)
	}
}