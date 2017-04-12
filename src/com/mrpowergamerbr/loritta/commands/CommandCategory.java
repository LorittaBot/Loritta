package com.mrpowergamerbr.loritta.commands;

import lombok.Getter;

@Getter
public enum CommandCategory {
	MINECRAFT("Minecraft"),
	UNDERTALE("Undertale"),
	DISCORD("Discord"),
	MISC("Miscelânea"),
	FUN("Diversão"),
	ADMIN("Administração"),
	MAGIC("Mágica"); // Esta categoria é usada para comandos APENAS para o dono do bot (no caso, MrPowerGamerBR#4185)
	
	String fancyTitle;
	
	CommandCategory(String fancyTitle) {
		this.fancyTitle = fancyTitle;
	}
}
