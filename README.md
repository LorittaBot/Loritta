
<p align="center">
<img width="65%" src="https://cdn.discordapp.com/attachments/708017680677863505/709793353478176768/lori_github_logo.png">
<br>

<h1 align="center">⭐ Loritta Morenitta ⭐</h1>

<p align="center">
<a href="https://discordbots.org/bot/297153970613387264?utm_source=widget">
<img src="https://discordbots.org/api/widget/297153970613387264.png?test=123456" alt="Discord Bots" />
</a>
 </p>
<p align="center">
<a href="https://github.com/LorittaBot/Loritta/actions?query=workflow%3A%22Build+Loritta%22"><img src="https://github.com/LorittaBot/Loritta/workflows/Build%20Loritta/badge.svg?event=push"></a>
<a href="https://loritta.crowdin.com/"><img src="https://badges.crowdin.net/e/6c10e98cefdc325e76bd33fb9952e616/localized.svg"></a>
<a href="https://github.com/LorittaBot/Loritta/blob/master/LICENSE"><img src="https://img.shields.io/badge/license-AGPL%20v3-lightgray.svg"></a>
</p>

<p align="center">
<a href="https://loritta.website"><img src="https://img.shields.io/badge/website-loritta-4daff8.svg"></a>
</a>
<a href="https://loritta.website/donate"><img src="https://img.shields.io/badge/donate-loritta-33cf57.svg"></a>
<a href="https://mrpowergamerbr.com/"><img src="https://img.shields.io/badge/website-mrpowergamerbr-fe4221.svg"></a>
<a href="https://loritta.website/support"><img src="https://discordapp.com/api/guilds/297732013006389252/widget.png"></a>
</p>

All Discord servers have the same problems. Members want entertainment, moderators want automation... and you just want to rest.

Your life is too precious to spend your time with useless junk, let me take care of the boring parts while you have fun on your server!

With features to entertain and engage your members, moderation features to keep your server always safe and enjoyable, and with an easy way to set up but with an unmatched power of customization...

And everything thanks to a 16 year old girl trying to make the world a better place!

_Making your server unique and extraordinary has never been easier!_

## 🤔 How can I add her?

If you want to use Loritta on your server, you can add our public instance by [clicking here](https://loritta.website/dashboard)! We recommend using the public instance, after all, more than 400k guilds already use, trust and love her, so why not try it out?

You can also host Loritta yourself, however we won't give support for people that are trying to selfhost her, we don't want to spend hours trying to troubleshoot other people issues that only happens on selfhosted instances, so you should at least know how to troubleshoot issues, if you find any.

## 📁 Project Structure
* 📜 **Loritta's API** [`loritta-api`]

> Multiplatform Loritta API, commands and features that only depend on the Loritta's API can be ported to other platforms, as long as they implement Loritta's API.

* 🍃 **Loritta's Serializable Commons** [`loritta-serializable-commons`]

> Serializable classes that are shared between `loritta-discord`, `spicy-morenitta` and other modules. The main purpose of it is to share data between the backend and the frontend in a easy way without messing around with different libraries.They aren't in the `loritta-api` module to avoid filling the `loritta-api` with module-specific classes that aren't needed to implement Loritta!

* 🎀 **Loritta (Discord/JDA)** [`loritta-discord` ]

> Discord implementation of Loritta's API, this is the public bot you all know and love so much! If you are planning to help Loritta's development, this is where to start!

* 🔌**Loritta's Plugins** [`loritta-plugins`]

> Sometimes restarting Loritta just to fix a small bug in a command can be a pain, that's why plugins exist! Plugins can be loaded/unloaded/updated during runtime, so you don't need to restart just to add a new cool command.
* * 🥩 **Rosbife** [`rosbife`]

>> Commands related to image edits.

* * 🎨 **Profile Designs** [`profile-designs`]

>> Defines the profiles users can buy for their `+profile`.

* * 🤑 **Donators Ostentation** [`donators-ostentation`]

>> Handles Nitro Boost features, premium slots channels automation, auto sonhos payout and other miscellaneous features.

* * 🖼️ **Auto Banner Changer** [`auto-banner-changer`]

>> Automatically changes the banner in the offical Loritta support servers... yup, that's it.

* * 👩‍💻 **Parallax Routes** [`parallax-routes`]

>> Creates endpoints for the Parallax Code Server.

* * And many others!

* 🔗 **Loritta Website** [`loritta-website`]
* * 🌶️ **Spicy Morenitta** [`spicy-morenitta`]

>> Spicying up Loritta's frontend! This is the code that gets executed in the browser.

* 🐶 **Loritta Watchdog (Discord/JDA)** [`loritta-watchdog-bot`]

> bark bark! Used to track Loritta's cluster statuses and other miscellaneous stuff.

* 🐱‍💻 **Parallax Code Server** [`parallax-code-server`]

> Executes custom JavaScript commands with GraalJS. Runs in a separate JVM to avoid malicious users crashing Loritta or breaking out of the sandbox, also because it is easier to update the code server with new features!

* 💫 **Shard Controller** [`shard-controller`]

> Large bots with the "Sharding for very large bots" feature requires something to synchronize their shard login status to avoid getting ratelimited during login. The shard controller (named [Tsuki](https://fortnite.fandom.com/wiki/Tsuki)) is a very small http server that controls what shards can login at any given time.

* 💸 **Loritta Premium** [`loritta-premium`]

> Does absolutely nothing! No, really, this is just a bot for premium users to show off that they bought premium features. All premium features are in the main bot.

* 🚧 **Loritta (Discord/Eris)** [`loritta-eris`]

> *Very* experimental (proof of concept) implementation of Loritta's API on a node.js environment using Kotlin/JS. This is just a "Hey look at this! *Code sharing* between modules! Sooooo cool!" project.

* 🐱 **Temmie Discord Auth** [`temmie-discord-auth`]

> Discord OAuth2 Client, named after [Temmie](https://youtu.be/_BD140nCDps). Why Temmie? Why *not* Temmie!

## 👨‍💻 Compiling Loritta

### [](https://emojipedia.org/construction-worker/)

### `0.` 👷 Prerequisites

* PowerShell (Windows) or Terminal (Linux).
> ⚠️ While Windows' command prompt may work, it is better to use PowerShell!
* You need to have the [Java Development Kit](https://adoptopenjdk.net/) installed on your machine. The minimum required version to compile and run Loritta is JDK 14.
* You need to have Git installed on your machine.
* Check if your machine has the `JAVA_HOME` property set correctly, newer JDK versions downloaded from AdoptOpenJDK may already have the variable set correctly. You can check if the variable is set by using `echo $env:JAVA_HOME` in PowerShell.
* If you want to help to develop Loritta, or if you only want a good Kotlin IDE, then download [JetBrains IntelliJ IDEA](https://www.jetbrains.com/pt-br/idea/)! The community edition is enough, so you don't need to be like "oh my god I need to *pay* for it". 😉

### `1.` 🧹 Preparing the environment
* Clone the repository with git:
```bash
git clone https://github.com/LorittaBot/Loritta.git
```

### `2.` 💻 Compiling
* Go inside the source code folder and open PowerShell or the terminal inside of it.
* Build Loritta with Gradle:
```bash
./gradlew build
```
> 💡 If you have Gradle installed on your computer, you can use `gradle build` instead of `./gradlew build`

> ⚠️ If Gradle complains that the `readAllBytes()` method is missing, then you are using an outdated method (pre-JDK 9) version, please update your JDK! We are in `${currentYear}`, get off your dinosaur and get on the [latest JDK from AdoptOpenJDK train]((https://adoptopenjdk.net/)), choo choo! 🚄
* If the build is successful, then congratulations 🎉! You have successfully compiled Loritta!
* The final artifacts will be inside of the `build/libs/*.jar` on every module folder, Loritta's dependencies will be inside of the `libs/` folder.

*You did it! Now... why not run her?* 🙃

## 🚀 Selfhosting Loritta (Discord)

### `X.` 📜 Selfhosting conditions and warnings
If you are planning to selfhost Loritta, here are some stuff that you should keep in mind...
1. We keep the source code open so people can see, learn and be inspired by how Loritta was made and, if they want to, they can help the project with features and bug fixes.
2. This is a community project, if you make changes to Loritta's source code you need to follow the [AGPL-3.0](LICENSE) and keep the changes open source! And, if you want to help Loritta, why not create a pull request? 😉
3. We **won't** give support for selfhosted instances, you need to know how to troubleshoot the issues yourself. We tried to make the selfhost process as painless as possible, but it is impossible to know all the different issues you may find.
4. Don't lie saying that you "created Loritta". Please give credits to the creators!
5. Loritta requires a lot of different API keys for a lot of features. While they aren't required, you may encounter issues when trying to use some of the features.
6. Loritta's assets (fonts, images, etc) aren't not distributed with the source code, you will need to create and include your own assets.
7. We use Ubuntu 18.04 to run her, she may work on other Linux operating systems or even in Windows, but we recommend hosting her on Ubuntu!
8. To avoid problems and confusions, we **do not allow** using the name "Loritta", "Lori" or any similar names on your selfhosted versions. Call her "Nicole" if you aren't creative enough to create your own name to give to your selfhosted version. Don't like "Nicole"? Generate your own [here](https://www.behindthename.com/random/)!

Seems too hard but you *really* want to use Loritta? Don't worry, you can use our free public instance by clicking here [clicking here](https://loritta.website/dashboard)!

### `0.` 👷 Prerequisites

* You will need the same prerequisites from the Compiling Loritta section, please check that section first.
* *(Optional, but highly recommended)* Install PostgreSQL, while Loritta also supports SQLite as a database, we only support and recommend using PostgreSQL as the database! (that's what the public Loritta instance uses!)

### `1.` 🧹 Preparing the environment
* Create a empty folder somewhere in your OS, why an empty folder? Just to keep things tidy! :3

### `2.` 📥 Getting the required JARs

#### If you compiled it yourself...

**Loritta's Discord JAR**: `loritta-discord/build/libs/` (get the Fat JAR version!)

**Loritta's Libraries:** `libs/`

#### If you are lazy and don't want to compile it yourself...
**You can find precompiled artifacts here:** https://github.com/LorittaBot/Loritta/actions?query=workflow%3A%22Build+Loritta%22

You will need to get `Loritta (Discord)` and `Loritta (Libs)`

### `3.` 🧹 Preparing the environment²
* Copy the `loritta-discord-*-fat.jar` to your created folder.
* Copy the `libs` folder to your created folder.
* If you did everything right, you should have in the root folder...
* * A file named `loritta-discord-*.jar`, this is Loritta's executable.
* * A folder named `libs` containing all Loritta dependencies.

### `4.` 🚶 The pre-start saga

* Run Loritta again with `java -jar loritta-discord-*-fat.jar` (replace the JAR name with the JAR in your folder)
* Update the configurations with your own values. You don't *need* to configure everything, just the bare minimum (bot token, folders, databases, etc) to get her up and running!

#### ⚠️ Values that you *need* to change before starting Loritta
```ascii
📄 loritta.conf
└── database
    ├── type
    │   * Configures what database type you will use
    └── the rest of the database values depending on your database type

📄 loritta.instance.conf
└── loritta
    └── folders
        ├── root
        │   * If you are on Windows, you need to use \\ as the path separator!
        └── * Don't forget to add your OS path separator at the end of the root value!

📄 discord.conf
└── discord
    ├── client-token
    │   * Your bot's Discord token
    ├── client-id
    │   * Your bot's client ID
    ├── client-secret
    └── * Your bot's OAuth2 secret
```

> ⚠️ Those are only the values that you *will* need to change before starting Loritta, there are other recommended values that you need to change too, but they aren't required to start Loritta.

* Locales are downloaded automatically by Loritta when the default locale isn't found on the locales folder you configured in the previous step, but you can still download them from the [LorittaLocales repository](https://github.com/LorittaBot/LorittaLocales).

### `5.` 🏃‍♂️ Starting Loritta
* Run Loritta again with `java -jar loritta-discord-*-fat.jar` (replace the JAR name with the JAR in your folder)
* Check the console to see if there are any errors... Well, we hope that there are none. :3
* If you haven't, add your bot to a Discord server.
* After booting up, try using `+ping` on your Discord server.
* If everything went well, your very own Loritta instance should be up and running! Congratulations! 🎉
* *(Optional)* You can add plugins to your instance!
* *(Optional)* Set up the Parallax Code Server + Parallax Routes plugin if you want to be able to execute custom JavaScript commands.
* *(Optional)* If you are planning on using it on a bot that has the "Sharding for very large bots" feature, set up the Shard Controller.

___

## 💫 Special thanks to...

![YourKit-Logo](https://www.yourkit.com/images/yklogo.png)

[YourKit](http://www.yourkit.com/), creators of the YourKit Java Profiler, providing support for open-source projects of all forms and shapes with their awesome Java-Application profiler. [Java](https://www.yourkit.com/java/profiler/index.jsp) and [.NET](https://www.yourkit.com/.net/profiler/index.jsp). Thank you for granting us an open source licenses that helps us to make Lori more useful and awesome for all our users!

___

<p align="center">
<img src="https://cdn.discordapp.com/attachments/708017680677863505/709834156145770534/lori_deitada.png">
</p>
