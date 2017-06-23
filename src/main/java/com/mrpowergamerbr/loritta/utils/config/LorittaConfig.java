package com.mrpowergamerbr.loritta.utils.config;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
public class LorittaConfig {
	public String clientToken;
	public String clientId;
	public String clientSecret;
	public String youtubeKey;
	public String ownerId;
	public String websiteUrl;
	public String frontendFolder;
	public String mercadoPagoClientId;
	public String mercadoPagoClientToken;
	public String mashapeKey;
	public String discordBotsKey;
	public List<String> currentlyPlaying = new ArrayList<String>();
}
