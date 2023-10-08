package net.perfectdreams.loritta.morenitta.modules

import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.morenitta.dao.Profile
import net.perfectdreams.loritta.morenitta.dao.ServerConfig
import net.perfectdreams.loritta.morenitta.events.LorittaMessageEvent
import net.perfectdreams.loritta.morenitta.utils.LorittaUser
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
    suspend fun matches(
		event: LorittaMessageEvent,
		lorittaUser: LorittaUser,
		lorittaProfile: Profile?,
		serverConfig: ServerConfig,
		locale: BaseLocale,
		i18nContext: I18nContext
	): Boolean

	/**
	 * What the module should do when it is executed
	 *
	 * @param event        the guild message received event
	 * @param lorittaUser  the message sender
	 * @param legacyServerConfig the server configuration
	 * @return             if true, the original event should be cancelled and nothing else should be processed
	 */
	suspend fun handle(
		event: LorittaMessageEvent,
		lorittaUser: LorittaUser,
		lorittaProfile: Profile?,
		serverConfig: ServerConfig,
		locale: BaseLocale,
		i18nContext: I18nContext
	): Boolean
}