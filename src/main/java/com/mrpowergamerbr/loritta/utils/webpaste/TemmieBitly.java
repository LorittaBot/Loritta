package com.mrpowergamerbr.loritta.utils.webpaste;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import com.github.kevinsawicki.http.HttpRequest;
import com.github.kevinsawicki.http.HttpRequest.HttpRequestException;

public class TemmieBitly {
	String apiKey;
	String login;
	
	public TemmieBitly(String apiKey, String login) {
		this.apiKey = apiKey;
		this.login = login;
	}
	
	public String shorten(String longUrl) {
		String body = null;
		try {
			body = HttpRequest.get(String.format("https://api-ssl.bitly.com/v3/shorten?format=txt&apiKey=%s&login=%s&longUrl=%s", apiKey, login, URLEncoder.encode(longUrl, "UTF-8"))).body();
		} catch (HttpRequestException | UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return body;
	}
	
	public String shorten(String longUrl, String customUrl) {
		String body = null;
		try {
			body = HttpRequest.get(String.format("https://api-ssl.bitly.com/v3/shorten?format=txt&apiKey=%s&login=%s&longUrl=%s&domain=%s", apiKey, login, URLEncoder.encode(longUrl, "UTF-8"), URLEncoder.encode(customUrl, "UTF-8"))).body();
		} catch (HttpRequestException | UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return body;
	}
}
