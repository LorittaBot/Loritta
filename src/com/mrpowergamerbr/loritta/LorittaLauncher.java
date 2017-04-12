package com.mrpowergamerbr.loritta;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import lombok.Getter;

@Getter
public class LorittaLauncher {
	public static Loritta loritta;
	
	public static void main(String[] args) {
		Options options = new Options();

		{
			Option input = new Option("t", "clientToken", true, "Bot's client token");
			input.setRequired(true);
			options.addOption(input);
		}
		{
			Option input = new Option("id", "clientId", true, "Bot's client id");
			input.setRequired(true);
			options.addOption(input);
		}
		{
			Option input = new Option("s", "clientSecret", true, "Bot's client secret");
			input.setRequired(true);
			options.addOption(input);
		}
		{
			Option input = new Option("ytkey", "youtubeKey", true, "YouTube's API key");
			input.setRequired(false);
			options.addOption(input);
		}
		{
			Option input = new Option("twKey", "twitterKey", true, "Twitter's API key");
			input.setRequired(false);
			options.addOption(input);
		}
		{
			Option input = new Option("d", "development", false, "Enables development mode, ServerConfig always returns the config for Guild ID \"development\"");
			input.setRequired(false);
			options.addOption(input);
		}
		{
			Option input = new Option("nb", "noBot", false, "Does not enable Loritta's Discord Bot");
			input.setRequired(false);
			options.addOption(input);
		}
		{
			Option input = new Option("nw", "noWebsite", false, "Does not enable Loritta's Website");
			input.setRequired(false);
			options.addOption(input);
		}
		
		CommandLineParser parser = new DefaultParser();
		HelpFormatter formatter = new HelpFormatter();
		CommandLine cmd;

		try {
			cmd = parser.parse(options, args);
		} catch (ParseException e) {
			System.out.println(e.getMessage());
			formatter.printHelp("Loritta", options);

			System.exit(1);
			return;
		}

		String clientToken = cmd.getOptionValue("clientToken");
		String clientId = cmd.getOptionValue("clientId");
		String clientSecret = cmd.getOptionValue("clientSecret");
		String youtubeApiKey = cmd.getOptionValue("youtubeKey");
		String development = cmd.getOptionValue("development");
		String noBot = cmd.getOptionValue("noBot");
		String noWebsite = cmd.getOptionValue("noWebsite");
		loritta = new Loritta(clientToken, clientId, clientSecret, youtubeApiKey);
		loritta.start();
	}

	// STATIC MAGIC(tm)
	public static Loritta getInstance() {
		return loritta;
	}
}
