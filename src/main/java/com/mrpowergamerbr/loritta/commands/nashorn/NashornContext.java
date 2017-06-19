package com.mrpowergamerbr.loritta.commands.nashorn;

import com.mrpowergamerbr.loritta.commands.CommandContext;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;

/**
 * Contexto do comando Nashorn executado, é simplesmente um wrapper "seguro" para comandos em JavaScript, para que
 * a Loritta possa controlar os comandos executados de uma maneira segura (para não abusarem da API do Discord)
 */
public class NashornContext {
	public NashornMessage mensagem;
	private CommandContext context; // Context original, jamais poderá ser usado pelo script!
	private int sentMessages = 0; // Quantas mensagens foram enviadas, usado para não levar rate limit
	private long lastMessageSent = 0L; // Quando foi a última mensagem enviada
	public NashornMember membro;

	public NashornContext(CommandContext context) {
		this.context = context;
		this.mensagem = new NashornMessage(context.getMessage());
		this.membro = new NashornMember(context.getHandle());
	}

	public NashornMessage getMensagem() {
		return mensagem;
	}
	public NashornMember getSender() {
		return membro;
	}

	public NashornMessage responder(String mensagem) {
		long diff = System.currentTimeMillis() - lastMessageSent;

		if (sentMessages >= 3) {
			if (diff > 2000) {
				throw new LorittaNashornException("Mais de 3 mensagens em menos de 2 segundos!");
			} else {
				diff = 0L;
				sentMessages = 0;
			}
		}

		sentMessages++;
		diff = System.currentTimeMillis();
		return new NashornMessage(context.sendMessage(context.getAsMention(true) + mensagem));
	}

	public NashornMessage enviarMensagem(String mensagem) {
		long diff = System.currentTimeMillis() - lastMessageSent;

		if (sentMessages >= 3) {
			if (diff > 2000) {
				throw new LorittaNashornException("Mais de 3 mensagens em menos de 2 segundos!");
			} else {
				diff = 0L;
				sentMessages = 0;
			}
		}

		sentMessages++;
		diff = System.currentTimeMillis();
		return new NashornMessage(context.sendMessage(mensagem));
	}

	public NashornMessage enviarImagem(NashornImage imagem) throws NoSuchFieldException, IllegalAccessException, IOException {
		return enviarImagem(imagem, " ");
	}

	public NashornMessage enviarImagem(NashornImage imagem, String mensagem) throws NoSuchFieldException, IllegalAccessException, IOException {
		long diff = System.currentTimeMillis() - lastMessageSent;

		if (sentMessages >= 3) {
			if (diff > 2000) {
				throw new LorittaNashornException("Mais de 3 mensagens em menos de 2 segundos!");
			} else {
				diff = 0L;
				sentMessages = 0;
			}
		}

		sentMessages++;
		diff = System.currentTimeMillis();

		// Reflection, já que nós não podemos acessar o BufferedImage

		Field field = imagem.getClass().getDeclaredField("bufferedImage");
		field.setAccessible(true);
		BufferedImage bufferedImage = (BufferedImage) field.get(imagem);

		ByteArrayOutputStream os = new ByteArrayOutputStream();
		ImageIO.write(bufferedImage, "png", os);
		InputStream is = new ByteArrayInputStream(os.toByteArray());

		return new NashornMessage(context.sendFile(is, "Loritta-NashornCommand.png", mensagem));
	}

	public String pegarArgumento(int idx) {
		return context.getArgs()[idx];
	}

	public String juntarArgumentos() { return juntarArgumentos(" "); }

	public String juntarArgumentos(String delimitador) {
		return String.join(delimitador, context.getArgs()).trim();
	}

	public boolean argumento(int idx, String mensagem) {
		return mensagem.equals(context.getArgs()[idx]);
	}

	public NashornImage criarImagem(int x, int y) {
		return new NashornImage(x, y);
	}
}
