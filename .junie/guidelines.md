Loritta is a general-purpose Discord bot.

## Commands

The `net.perfectdreams.loritta.morenitta.interactions` package has "InteraKTions Unleashed", Loritta's command framework.

The `net.perfectdreams.loritta.morenitta.interactions.vanilla` package has Loritta's application commands, this includes:
* Slash Commands (type `/` in the chat -> Select the command)
* User Commands (right-click on a user -> Apps)
* Message Commands (right-click on a message -> Apps)

InteraKTions Unleashed supports "legacy" mode, which lets Loritta process the slash command by typing Loritta's prefix in chat and using the command. Legacy mode is desired when you are able to add support to them, because while slash commands are the modern way of interacting with bots, there are a lot of people that still like to use prefixed commands. You can enable legacy mode by setting `enableLegacyMessageSupport = true` in the slash command variable, and by implementing the `LorittaLegacyMessageCommandExecutor` interface on all executors related to that command.

The `net.perfectdreams.loritta.morenitta.commands` package also has Loritta's commands, however, they were created using old frameworks and they do not support slash commands. You should NOT create commands using that framework unless explicitly stated otherwise.

## Internationalization

When implementing features that have user facing strings, the strings should be added to the correct file in the `resources/languages/pt` folder, you NEED to run the `generateI18nKeys` Gradle task AFTER writing the strings on the folder, to be able to access them on the generated `I18nKeysData`.

Do NOT use the string key directly, use `I18nKeysData` unless there is no other choice, if you aren't able to find the generated strings in the `I18nKeysData` file, try running the `generateI18nKeys` Gradle task.