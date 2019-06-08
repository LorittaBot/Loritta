package net.perfectdreams.loritta.socket.network.commands.config.get

import com.fasterxml.jackson.databind.node.ObjectNode
import com.mrpowergamerbr.loritta.dao.ServerConfig
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.userdata.MongoServerConfig
import net.perfectdreams.loritta.socket.network.ConfigSectionOpCode
import net.perfectdreams.loritta.utils.extensions.objectNode
import net.perfectdreams.loritta.utils.extensions.set
import org.jetbrains.exposed.sql.transactions.experimental.transaction

class GetDonationConfigCommand : GetConfigCommand(ConfigSectionOpCode.DONATION) {
    override suspend fun process(guildId: Long, serverConfig: MongoServerConfig): ObjectNode {
        val objectNode = objectNode()

        val donationConfig = transaction(Databases.loritta) {
            val config = ServerConfig.findById(guildId)
            return@transaction config?.donationConfig
        }

        objectNode["customBadge"] = donationConfig?.customBadge ?: false
        objectNode["dailyMultiplier"] = donationConfig?.dailyMultiplier ?: false

        return objectNode
    }
}