package com.mrpowergamerbr.loritta.modules

import com.mrpowergamerbr.loritta.dao.Profile
import com.mrpowergamerbr.loritta.dao.ServerConfig
import com.mrpowergamerbr.loritta.events.LorittaMessageEvent
import com.mrpowergamerbr.loritta.utils.LorittaUser
import net.perfectdreams.loritta.common.locale.BaseLocale

interface MessageReceivedModule {
	/**
	 * If the module should be executed
	 *
	 * @param event        the guild message received event
	 * @param lorittaUser  the message sender
	 * @param legacyServerConfig the server configuration
	 * @return             if the event should be handled
	 */
    suspend fun matches(event: LorittaMessageEvent, lorittaUser: LorittaUser, lorittaProfile: Profile?, serverConfig: ServerConfig, locale: BaseLocale): Boolean

	/**
	 * What the module should do when it is executed
	 *
	 * @param event        the guild message received event
	 * @param lorittaUser  the message sender
	 * @param legacyServerConfig the server configuration
	 * @return             if true, the original event should be cancelled and nothing else should be processed
	 */
	suspend fun handle(event: LorittaMessageEvent, lorittaUser: LorittaUser, lorittaProfile: Profile?, serverConfig: ServerConfig, locale: BaseLocale): Boolean
}