package com.mrpowergamerbr.loritta.userdata;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MusicConfig {
	private boolean isEnabled = false;
	private String musicGuildId = null;
	private boolean hasMaxSecondRestriction = true;
	private int maxSeconds = 420;
	private boolean autoPlayWhenEmpty = false;
	private List<String> urls = new ArrayList<String>();
	private boolean voteToSkip = true;
	private int required = 75;
}
