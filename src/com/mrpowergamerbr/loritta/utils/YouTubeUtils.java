package com.mrpowergamerbr.loritta.utils;

import java.util.List;
import java.util.stream.Collectors;

import com.mrpowergamerbr.loritta.Loritta;
import com.mrpowergamerbr.loritta.utils.temmieyoutube.response.SearchResponse;
import com.mrpowergamerbr.loritta.utils.temmieyoutube.utils.YouTubeItem;

public class YouTubeUtils {
	/**
	 * Procura algo no YouTube e retorna o SearchResponse
	 * 
	 * @param query  O que você deseja pesquisar no YouTube
	 * @return O SearchResponse
	 */
	public static SearchResponse searchOnYouTube(String query) {
		SearchResponse response = Loritta.getYoutube().searchOnYouTube(query);
		return response;
	}
	
	public static List<YouTubeItem> searchAnythingOnYouTube(String query) {
		SearchResponse response = searchOnYouTube(query);
		return response.getItems();
	}
	
	public static List<YouTubeItem> searchVideosOnYouTube(String query) {
		List<YouTubeItem> response = searchAnythingOnYouTube(query);
		return response.stream().filter((item) -> item.getId().getKind().equals("youtube#video")).collect(Collectors.toList());
	}
}
