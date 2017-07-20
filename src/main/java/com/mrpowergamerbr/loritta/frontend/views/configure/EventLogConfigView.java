package com.mrpowergamerbr.loritta.frontend.views.configure;

import com.mitchellbosecke.pebble.error.PebbleException;
import com.mitchellbosecke.pebble.template.PebbleTemplate;
import com.mrpowergamerbr.loritta.LorittaLauncher;
import com.mrpowergamerbr.loritta.frontend.LorittaWebsite;
import com.mrpowergamerbr.loritta.frontend.utils.RenderContext;
import com.mrpowergamerbr.loritta.userdata.EventLogConfig;
import com.mrpowergamerbr.loritta.userdata.ServerConfig;
import com.mrpowergamerbr.loritta.utils.PanelOptionWrapper;
import com.mrpowergamerbr.temmiediscordauth.TemmieDiscordAuth;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class EventLogConfigView {
	public static PebbleTemplate render(RenderContext context, TemmieDiscordAuth temmie, ServerConfig sc)
			throws PebbleException {
		EventLogConfig eventLogConfig = sc.eventLogConfig;
		if (context.request().param("memberBanned").isSet()) { // O usuário está salvando as configurações?
			try {
				for (Field f : eventLogConfig.getClass().getDeclaredFields()) {
					f.setAccessible(true);
					if (f.getType() == Boolean.TYPE) {
						f.setBoolean(eventLogConfig, context.request().param(f.getName()).isSet());
					} else if (f.getType() == String.class) {
						if (context.request().param(f.getName()).isSet()) {
							f.set(eventLogConfig, context.request().param(f.getName()).value());
						}
					}
				}
				sc.eventLogConfig(eventLogConfig);
				LorittaLauncher.getInstance().getDs().save(sc);
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
		context.contextVars().put("whereAmI", "eventLogConfig");

		List<PanelOptionWrapper> options = new ArrayList<>();

		options.add(new PanelOptionWrapper(eventLogConfig, "memberBanned", "Avisar quando alguém for banido"));
		options.add(new PanelOptionWrapper(eventLogConfig, "memberUnbanned", "Avisar quando alguém for desbanido"));
		options.add(new PanelOptionWrapper(eventLogConfig, "channelCreated", "Avisar quando um canal for criado"));
		options.add(new PanelOptionWrapper(eventLogConfig, "channelNameUpdated", "Avisar quando o nome de um canal de texto for alterado"));
		options.add(new PanelOptionWrapper(eventLogConfig, "channelTopicUpdated", "Avisar quando o tópico de um canal de texto for alterado"));
		options.add(new PanelOptionWrapper(eventLogConfig, "channelPositionUpdated", "Avisar quando a posição de um canal de texto for alterada"));
		options.add(new PanelOptionWrapper(eventLogConfig, "channelDeleted", "Avisar quando um canal de texto"));
		options.add(new PanelOptionWrapper(eventLogConfig, "nicknameChanges", "Avisar quando alguém alterar o nickname"));
		options.add(new PanelOptionWrapper(eventLogConfig, "usernameChanges", "Avisar quando alguém alterar o nome"));
		options.add(new PanelOptionWrapper(eventLogConfig, "avatarChanges", "Avisar quando alguém alterar o avatar"));
		options.add(new PanelOptionWrapper(eventLogConfig, "voiceChannelJoins", "Avisar quando alguém entrar em um canal de voz"));
		options.add(new PanelOptionWrapper(eventLogConfig, "voiceChannelLeaves", "Avisar quando alguém sair de um canal de voz"));

		context.contextVars().put("options", options);

		PebbleTemplate template = LorittaWebsite.getEngine().getTemplate("eventlog_config.html");
		return template;
	}
}
