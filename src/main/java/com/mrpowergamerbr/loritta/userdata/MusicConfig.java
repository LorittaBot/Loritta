package com.mrpowergamerbr.loritta.userdata;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class MusicConfig {
	public boolean isEnabled = false;
	public String musicGuildId = null;
	public boolean hasMaxSecondRestriction = true;
	public int maxSeconds = 420;
	public boolean autoPlayWhenEmpty = false;
	public List<String> urls = new ArrayList<String>();
	public boolean voteToSkip = true;
	public int required = 75;
}
