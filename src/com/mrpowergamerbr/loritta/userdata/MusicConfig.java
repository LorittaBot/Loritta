package com.mrpowergamerbr.loritta.userdata;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MusicConfig {
	private boolean isEnabled = false;
	private String musicGuildId = null;
	private boolean hasMaxSecondRestriction = true;
	private int maxSeconds = 420;
}
