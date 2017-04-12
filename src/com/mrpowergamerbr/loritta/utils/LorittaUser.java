package com.mrpowergamerbr.loritta.utils;

import com.mrpowergamerbr.loritta.userdata.ServerConfig;

import lombok.Getter;
import net.dv8tion.jda.core.entities.Member;

/**
 * Um usuário que está comunicando com a Loritta
 */
@Getter
public class LorittaUser {
	private Member member;
	private ServerConfig config;
	
	public LorittaUser(Member member, ServerConfig config) {
		this.member = member;
		this.config = config;
	}
	
	public String getAsMention() {
		return getAsMention(false);
	}
	
	public String getAsMention(boolean addSpace) {
		return (config.mentionOnCommandOutput() ? member.getAsMention() + (addSpace ? " " : "") : "");
	}
}
