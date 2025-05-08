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

I18n strings are formatted using ICU4J, so you should name each formatting variable with a proper name, like `{userMention}`.

If you have a file in the path `resources/languages/pt/test.yml` with the following contents:

```yml
helloWorld: "Loritta is so cute! Don't you think {userMention}?"
```

You can use it in the code with `I18nKeysData.HelloWorld(userMention = "<@297153970613387264>")`. When the key is parsed in the `I18nKeysData` task for the Kotlin code, it is converted to Title Case, that is, if you have a key named `helloWorld` it will be `HelloWorld` in the generated files.

You can create nested keys. So, if the file content was like this:
```yml
loritta:
  lorittaIsCute: "Loritta is so cute!"
  helloWorld: "Loritta is so cute! Don't you think {userMention}?"
```

You can use it in the code with `I18nKeysData.Loritta.LorittaIsCute` and `I18nKeysData.Loritta.HelloWorld(userMention = "<@297153970613387264>")`. The `lorittaIsCute` example does not have any parameters, so the generated code in the `I18nKeysData` file will be a property. The `helloWorld` example does have a `userMention` parameter in the i18n string, so we need to pass it to the generated `HelloWorld` function when using it in our code.

In Loritta's code, you use the `StringI18nData` and `ListI18nData` with a `I18nContext` instance, so you end up using it like this `i18nContext.get(I18nKeysData.Loritta.HelloWorld(userMention = "<@297153970613387264>"))`.

There are some special folders where the `I18nKeysData` is generated differently, here are some examples:

If you have a file in the path `resources/languages/pt/commands/test.yml` with the following contents:

```yml
helloWorld: "Loritta is so cute! Don't you think {userMention}?"
```
You can use it in the code with `I18nKeysData.Commands.Command.Test.HelloWorld(userMention = "<@297153970613387264>")`. The `.Commands.Command.FileNameHere` is automatically added to the generated key!

When creating i18n keys for slash command options, you should follow this convention:

```yml
options:
  nameOfTheOptionHere:
    text: "Description of the option here"
```

So, as an example: Let's suppose you are creating an `/user info` slash command that has an option named `user`, where the provided user is the user that you will look up their information, the i18n keys should be implemented as follows:

```yml
options:
  user:
    text: "O usuário que você deseja ver as informações dele"
```

## Interactivity

Interactions (such as buttons, select menus, entity menus, etc) should be handled via the `InteractivityManager` class.

You should ALWAYS prefer the `___forUser(...)` functions in the `InteractivityManager` class UNLESS if the component you are creating NEEDS to be able to be used by other users. Example: If you are implementing a button that, when clicked, increases a counter, and anyone can click on the button to increase the counter. Example: `buttonForUser(...)`.

If you need examples of how button interactivity works, check the following classes: `AchievementsExecutor`, `GiveawayBuilderScreen`, `UserInfoExecutor`.

You should ONLY handle the interactivity manually when requested. When asked to do it manually, you should create the interaction listener and store the data related to the button on the database, similar to how the `SonhosPayExecutor` class works.