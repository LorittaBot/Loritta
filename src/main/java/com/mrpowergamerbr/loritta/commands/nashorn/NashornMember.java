package com.mrpowergamerbr.loritta.commands.nashorn;

import net.dv8tion.jda.core.entities.Member;

/**
 * Wrapper de um membro de um comando Nashorn executado, é simplesmente um wrapper "seguro" para comandos em JavaScript, para que
 * a Loritta possa controlar as mensagens enviadas de uma maneira segura (para não abusarem da API do Discord)
 */
public class NashornMember {
	private Member member;

	public NashornMember(Member member) {
		this.member = member;
	}

	public String nomeNoServidor() {
		return member.getEffectiveName();
	}

	public String nome() {
		return member.getUser().getName();
	}

	public String avatar() {
		return member.getUser().getEffectiveAvatarUrl();
	}
}
