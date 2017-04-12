package com.mrpowergamerbr.loritta.utils.correios;

import java.util.List;

import lombok.Getter;

@Getter
public class EncomendaResponse {
	private String date;
	private String time;
	private String state;
	private List<PackageUpdate> locations;
	
	@Getter
	public static class PackageUpdate {
		private String state;
		private String reason;
		private String location;
		private String receiver;
		private String date;
		private String time;
	}
}
