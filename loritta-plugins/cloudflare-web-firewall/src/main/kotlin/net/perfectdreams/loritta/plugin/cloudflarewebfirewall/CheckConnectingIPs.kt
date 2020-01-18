package net.perfectdreams.loritta.plugin.cloudflarewebfirewall

import com.github.salomonbrys.kotson.*
import com.google.common.collect.EvictingQueue
import com.google.common.collect.Queues
import com.google.gson.GsonBuilder
import com.mrpowergamerbr.loritta.utils.extensions.trueIp
import com.mrpowergamerbr.loritta.utils.jsonParser
import com.mrpowergamerbr.loritta.utils.loritta
import io.ktor.client.request.header
import io.ktor.client.request.put
import io.ktor.client.response.HttpResponse
import io.ktor.client.response.readText
import io.ktor.content.TextContent
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import mu.KotlinLogging
import java.io.File

class CheckConnectingIPs(val m: CloudflareWebFirewall, val config: CloudflareConfig) {
	private val logger = KotlinLogging.logger {}
	private val connectedIPsQueue = Queues.synchronizedQueue(EvictingQueue.create<String>(config.queueSize))
	private var lastRatioCheck = System.currentTimeMillis()
	val mutex = Mutex()

	fun register() {
		logger.info { "Registering beforeLoad handler..." }

		m.joobyWebsite.beforeLoad { req, res ->
			val isAWhitelistedPath = config.whitelistedPaths.any { req.path().matches(Regex(it)) }

			if (isAWhitelistedPath)
				return@beforeLoad false

			val trueIp = req.trueIp

			connectedIPsQueue.add(trueIp)
			val diff = System.currentTimeMillis() - lastRatioCheck

			if (diff >= config.delayBetweenChecks || connectedIPsQueue.size == config.queueSize) {
				lastRatioCheck = System.currentTimeMillis()
				if (connectedIPsQueue.size >= config.minimumIpCountToAllowChecks) // Para evitar falsos positivos, vamos apenas ignorar caso o número de conexões nos últimos 15s seja baixo
					checkConnectedIPsRatio()
				else
					logger.info { "Queue Size ${connectedIPsQueue.size} is less than the required IP count ${config.minimumIpCountToAllowChecks} for blacklist check, ignoring and clearing the queue..." }
				connectedIPsQueue.clear()
			}

			false
		}
	}

	fun checkConnectedIPsRatio() {
		val ipMap = connectedIPsQueue.groupingBy { it }.eachCount()
				.entries
				.sortedByDescending {
					it.value
				}
				.let {
					it.subList(0, Math.min(10, it.size))
				}

		val allConnections = ipMap.sumBy { it.value }
		val connectionsByAsn = mutableMapOf<Int, Int>()
		val numberOfIpsUsingAsn = mutableMapOf<Int, Int>()

		val banConnectedIp = mutableListOf<Map.Entry<String, Int>>()
		val banConnectedAsns = mutableListOf<Pair<Int, CloudflareWebFirewall.ASN>>()

		logger.info { "===[ CONNECTED IPs RATIO ]===" }
		for (ip in ipMap) {
			val retrievedAsnInfo = getAsnForIP(ip.key)
			val asnNumber = retrievedAsnInfo?.first
			val asnName = retrievedAsnInfo?.second

			val connectionRatio = ip.value.toDouble() / allConnections
			logger.info { "$ip - AS$asnNumber (${asnName}): ${ip.value} connections (${connectionRatio * 100}%)" }

			if (asnNumber != null) {
				val currentConnections = connectionsByAsn.getOrPut(asnNumber) { 0 }

				connectionsByAsn[asnNumber] = (currentConnections + ip.value)

				val connectionsUsingAsn = numberOfIpsUsingAsn.getOrPut(asnNumber) { 0 }

				numberOfIpsUsingAsn[asnNumber] = (connectionsUsingAsn + 1)
			}

			if (ip.value >= config.requiredConnectionsForIpBlacklist && ip.key !in config.whitelistedIps)
				banConnectedIp.add(ip)
		}

		val allAsnConnections = connectionsByAsn.entries.sumBy { it.value }

		logger.info { "===[ CONNECTED IPs BY ASN RATIO ]===" }
		for ((asnNumber, connections) in connectionsByAsn.entries.sortedByDescending { it.value }) {
			val asnInfo = m.asns[asnNumber]!!
			val asnName = asnInfo.name
			val connectionRatio = connections.toDouble() / allAsnConnections
			val ipsUsingThisAsn = numberOfIpsUsingAsn[asnNumber]!!

			logger.info { "AS$asnNumber (${asnName}): $connections connections (with $ipsUsingThisAsn IPs) (${connectionRatio * 100}%)" }

			if (connectionRatio >= config.asnBlacklistRatio && asnNumber !in config.whitelistedAsns && ipsUsingThisAsn != 1)
				banConnectedAsns.add(Pair(asnNumber, asnInfo))
		}

		if ((banConnectedIp.isNotEmpty() || banConnectedAsns.isNotEmpty())) {
			GlobalScope.launch(loritta.coroutineDispatcher) {
				mutex.withLock {
					// check for IP bans
					val currentCfExpression = File(m.dataFolder, "cloudflare-blocked.json")
							.readText()

					val elements = jsonParser.parse(currentCfExpression).array

					for ((ip, connections) in banConnectedIp) {
						if (elements.any { it["type"].string == "ipBlock" && it["ip"].string == ip })
							continue

						logger.info { "Adding IP $ip to the blocked list" }
						elements.add(
								jsonObject(
										"type" to "ipBlock",
										"ip" to ip,
										"description" to "$ip with $connections connections blocked at ${System.currentTimeMillis()}"
								)
						)
					}

					for ((asnNumber, asnInfo) in banConnectedAsns) {
						if (elements.any { it["type"].string == "asnBlock" &&it["number"].int == asnNumber })
							continue

						logger.info { "Adding AS${asnNumber} to the blocked list" }
						elements.add(
								jsonObject(
										"type" to "asnBlock",
										"number" to asnNumber,
										"description" to "${asnInfo.name} blocked at ${System.currentTimeMillis()}"
								)
						)
					}

					val cfExpression = StringBuilder()

					for ((index, element) in elements.withIndex()) {
						if (element["type"].string == "asnBlock")
							cfExpression.append("(ip.geoip.asnum eq ${element["number"].int})")
						else
							cfExpression.append("(ip.src eq ${element["ip"].string})")
						if (index != (elements.size() - 1)) {
							cfExpression.append(" or ")
						}
					}

					val prettyGson = GsonBuilder()
							.setPrettyPrinting()
							.create()

					File(m.dataFolder, "cloudflare-blocked.json")
							.writeText(
									prettyGson.toJson(elements)
							)

					val cfExpressionAsString = cfExpression.toString()

					logger.info { "Safe Cloudflare Firewall Rule Expression is $cfExpressionAsString" }

					val jsonPayload = prettyGson.toJson(
							jsonArray(
									jsonObject(
											"id" to config.ruleId,
											"paused" to false,
											"expression" to cfExpressionAsString,
											"description" to "Automated Block"
									)
							)
					)

					logger.info { jsonPayload }

					loritta.http.put<HttpResponse>("https://api.cloudflare.com/client/v4/zones/${config.zoneId}/filters") {
						header("X-Auth-Email", config.authEmail)
						header("X-Auth-Key", config.authKey)

						body = TextContent(jsonPayload, ContentType.Application.Json)
					}.use {
						if (it.status != HttpStatusCode.OK) {
							val responseText = it.readText()

							logger.warn { "Failure while trying to update Cloudflare's Firewall Rules! Status Code ${it.status}: $responseText" }
						} else {
							logger.info { "Successfully updated Cloudflare's Firewall Rules!" }
						}
					}
				}
			}
		}
	}

	fun getAsnForIP(source: String): Pair<Int, CloudflareWebFirewall.ASN>? {
		if (source.contains(":")) // whoops, no IPv6 support yet!
			return null

		for ((asnNumber, asnInfo) in m.asns.entries) {
			val isThisAsn = asnInfo.isThisAsn(source)

			if (isThisAsn)
				return Pair(asnNumber, asnInfo)
		}

		return null
	}
}