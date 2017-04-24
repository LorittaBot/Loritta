package com.mrpowergamerbr.loritta.utils;

public class VaporwaveUtils {
	public static String vaporwave(String str) {
		str = str.toLowerCase(); // Como a gente abusa dos códigos unicode, é necessário dar lowercase antes de aplicar o efeito
		StringBuilder sb = new StringBuilder();
		for (char c : str.toCharArray()) {
			if (Character.isSpaceChar(c)) {
				sb.append(" ");
				continue;
			}
			char vaporC = (char) (c + 0xFEE0);

			if (Character.getType(vaporC) != 2) {
				sb.append(c);
				continue;
			}
			
			sb.append(vaporC);
		}
		return sb.toString();
	}
}
