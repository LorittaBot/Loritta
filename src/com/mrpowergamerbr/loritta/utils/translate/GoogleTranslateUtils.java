package com.mrpowergamerbr.loritta.utils.translate;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.List;

import org.json.JSONArray;

public class GoogleTranslateUtils {
	/**
	 * Traduz uma mensagem de uma língua para outra
	 * 
	 * Criado por Kurimatzu
	 * 
	 * @param message
	 * @param from
	 * @param to
	 * @return A mensagem traduzida, caso seja null, quer dizer que falhou ao traduzir a mensagem
	 */
	@SuppressWarnings("unchecked")
	public static String translate(String message, String from ,String to) {
        try {
            message = URLEncoder.encode(message, "UTF-8");
            URL url = new URL("http://translate.googleapis.com/translate_a/single?client=gtx&sl=" + from + "&tl="
                    + to + "&dt=t&q=" + message + "&ie=UTF-8&oe=UTF-8");
            URLConnection uc = url.openConnection();
            uc.setRequestProperty("User-Agent", "Mozilla/5.0"); 
            BufferedReader in = new BufferedReader(new InputStreamReader(uc.getInputStream(), "UTF-8"));
            String inputLine;
            StringBuilder builder = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                builder.append(inputLine);
            }
            String s = builder.toString();
            StringBuilder sb = new StringBuilder();
            
            // Como parsear coisas não-JSON usando JSON? Sexta, no Globo Repórter.
            // Na verdade o website NÃO retorna um JSON válido, e nós NÃO devemos parsear assim!
            // Mas como o código não fui eu que fiz, eu só resolvi dar uma melhorada para corrigir
            // um pequeno bug que o texto não era pegado 100%.
            // 
            // Ou seja, isto é algo que funciona que NÃO deveria funcionar, e que provavelmente só
            // funciona porque o JSON do Java é lenient e não strict!
            JSONArray jsonArray = new JSONArray(s);
            JSONArray jsonArray2 = (JSONArray) jsonArray.get(0);
            for (Object obj : jsonArray2.toList()) {
            	if (obj instanceof List) {
            		List<Object> listInsideArray = (List<Object>) obj;
            		sb.append(listInsideArray.get(0));
            	}
            }
            
            in.close();
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
