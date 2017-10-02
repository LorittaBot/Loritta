package com.mrpowergamerbr.loritta.frontendold.views.configure;

import com.mitchellbosecke.pebble.error.PebbleException;
import com.mitchellbosecke.pebble.template.PebbleTemplate;
import com.mrpowergamerbr.loritta.LorittaLauncher;
import com.mrpowergamerbr.loritta.frontendold.LorittaWebsite;
import com.mrpowergamerbr.loritta.frontendold.utils.RenderContext;
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
		if (context.request().param("saving").isSet()) { // O usuário está salvando as configurações?
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

		options.add(new PanelOptionWrapper(eventLogConfig, "memberBanned", context.locale.get("ADMINP_EVENTLOG_MEMBER_BAN")));
		options.add(new PanelOptionWrapper(eventLogConfig, "memberUnbanned", context.locale.get("ADMINP_EVENTLOG_MEMBER_UNBAN")));
		options.add(new PanelOptionWrapper(eventLogConfig, "messageEdit", context.locale.get("ADMINP_EVENTLOG_MESSAGE_EDIT")));
		options.add(new PanelOptionWrapper(eventLogConfig, "messageDeleted", context.locale.get("ADMINP_EVENTLOG_MESSAGE_DELETE")));
		options.add(new PanelOptionWrapper(eventLogConfig, "channelCreated", context.locale.get("ADMINP_EVENTLOG_CHANNEL_CREATED")));
		options.add(new PanelOptionWrapper(eventLogConfig, "channelNameUpdated", context.locale.get("ADMINP_EVENTLOG_CHANNEL_NAME_CHANGED")));
		options.add(new PanelOptionWrapper(eventLogConfig, "channelTopicUpdated", context.locale.get("ADMINP_EVENTLOG_CHANNEL_TOPIC_UPDATED")));
		options.add(new PanelOptionWrapper(eventLogConfig, "channelPositionUpdated", context.locale.get("ADMINP_EVENTLOG_CHANNEL_POSITION_UPDATED")));
		options.add(new PanelOptionWrapper(eventLogConfig, "channelDeleted", context.locale.get("ADMINP_EVENTLOG_CHANNEL_DELETED")));
		options.add(new PanelOptionWrapper(eventLogConfig, "nicknameChanges", context.locale.get("ADMINP_EVENTLOG_SERVER_NICK_CHANGE")));
		options.add(new PanelOptionWrapper(eventLogConfig, "usernameChanges", context.locale.get("ADMINP_EVENTLOG_USERNAME_CHANGE")));
		options.add(new PanelOptionWrapper(eventLogConfig, "avatarChanges", context.locale.get("ADMINP_EVENTLOG_AVATAR_CHANGE")));
		options.add(new PanelOptionWrapper(eventLogConfig, "voiceChannelJoins", context.locale.get("ADMINP_EVENTLOG_VOICE_JOIN")));
		options.add(new PanelOptionWrapper(eventLogConfig, "voiceChannelLeaves", context.locale.get("ADMINP_EVENTLOG_VOICE_LEAVE")));

		context.contextVars().put("options", options);

		PebbleTemplate template = LorittaWebsite.getEngine().getTemplate("eventlog_config.html");
		return template;
	}
}
