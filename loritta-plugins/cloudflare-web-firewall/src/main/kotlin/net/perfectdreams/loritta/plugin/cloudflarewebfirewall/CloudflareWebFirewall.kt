package net.perfectdreams.loritta.plugin.cloudflarewebfirewall

import com.fasterxml.jackson.module.kotlin.readValue
import com.mrpowergamerbr.loritta.LorittaLauncher
import com.mrpowergamerbr.loritta.utils.Constants
import mu.KotlinLogging
import net.perfectdreams.loritta.platform.discord.plugin.DiscordPlugin
import org.apache.commons.net.util.SubnetUtils
import java.io.File

class CloudflareWebFirewall : DiscordPlugin() {
	private val logger = KotlinLogging.logger {}
	val asns = mutableMapOf<Int, ASN>()

	override fun onEnable() {
		if (!LorittaLauncher.loritta.isMaster) { // Caso não seja o master cluster, apenas ignore
			logger.info { "Disabling Cloudflare Automatic Web Firewall because we are not the master cluster..." }
			return
		}

		val config = Constants.HOCON_MAPPER.readValue<CloudflareConfig>(File(dataFolder, "config.conf"))

		if (!config.enabled) {
			logger.info { "Disabling Cloudflare Automatic Web Firewall because the feature is disabled..." }
			return
		}

		val file = File(this.dataFolder, "GeoLite2-ASN-Blocks-IPv4.csv")

		var idx = 0
		file.bufferedReader().useLines {
			for (line in it) {
				if (idx != 0) { // != 0, header
					val split = line.split(",")

					val ip = split[0].removePrefix("\"")
							.removeSuffix("\"")

					val asnNumber = split[1].removePrefix("\"")
							.removeSuffix("\"")
							.toInt()

					val asnName = split[2].removePrefix("\"")
							.removeSuffix("\"")

					val asn = asns.getOrPut(asnNumber, { ASN(asnName) })
					val ipAndRange = ip.split("/")
					val ipSplit = ipAndRange.first().split(".")

					asn.ranges.add(
							IPWithBitmask(
									ipSplit[0].toInt(),
									ipSplit[1].toInt(),
									ipSplit[2].toInt(),
									ipSplit[3].toInt(),
									ipAndRange.last().toInt()
							)
					)
				}
				idx++
			}
		}

		logger.info { "Loaded ${asns.size} ASNs!" }

		val checkConnectingIPs = CheckConnectingIPs(this, config)

		checkConnectingIPs.register()
	}

	data class ASN(
			val name: String
	) {
		val ranges = mutableListOf<IPWithBitmask>()

		fun isThisAsn(source: String): Boolean {
			val split = source.split(".")

			val first = split[0].toInt()
			val second = split[1].toInt()
			val third = split[2].toInt()
			val fourth = split[3].toInt()

			for (range in ranges) {
				var matchedRange: IPWithBitmask? = range

				if (range.mask in 24..32) {
					if (range.first == first && range.second == second && range.third == third) {
						matchedRange = range
					}
				} else if (range.mask in 16..23) {
					if (range.first == first && range.second == second) {
						matchedRange = range
					}
				} else {
					if (range.first == first) {
						matchedRange = range
					}
				}

				if (matchedRange != null) {
					val subnetUtils = SubnetUtils("${matchedRange.first}.${matchedRange.second}.${matchedRange.third}.${matchedRange.fourth}/${matchedRange.mask}")

					if (subnetUtils.getInfo().isInRange(source))
						return true
				}
			}

			return false
		}
	}

	data class IPWithBitmask(
			val first: Int,
			val second: Int,
			val third: Int,
			val fourth: Int,
			val mask: Int
	)
}