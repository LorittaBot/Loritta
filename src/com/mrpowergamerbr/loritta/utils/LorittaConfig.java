package com.mrpowergamerbr.loritta.utils;

import lombok.*;
import lombok.experimental.Accessors;

@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
public class LorittaConfig {
	private String clientToken;
	private String clientId;
	private String clientSecret;
	private String youtubeKey;
	private String ownerId;
	private String websiteUrl;
	private String frontendFolder;
	private String mercadoPagoClientId;
	private String mercadoPagoClientToken;
	private String currentlyPlaying;
}
