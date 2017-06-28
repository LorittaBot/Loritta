package com.mrpowergamerbr.loritta.utils.correios;

import lombok.Getter;

import java.util.List;

@Getter
public class EncomendaResponse {
	public String date;
	public String time;
	public String state;
	public List<PackageUpdate> locations;
	
	@Getter
	public static class PackageUpdate {
		public String state;
		public String reason;
		public String location;
		public String receiver;
		public String date;
		public String time;
	}
}
