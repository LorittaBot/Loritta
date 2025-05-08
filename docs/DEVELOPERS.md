# 👩‍💻 Developing Loritta

Loritta is made with [Kotlin](https://kotlinlang.org/). Kotlin is a modern, concise and safe programming language made by JetBrains. If you already used Java before, Kotlin will feel familiar to you. Heck, it can even run on the JVM!

Why Kotlin? Because Kotlin is my beloved. 🥺 <img src="https://cdn.discordapp.com/emojis/841285914159611914.gif" height="24" />

Originally Loritta was made in Java, but in May 2017 I decided migrate to Kotlin!

## 🛠️ Compiling Loritta

### 👷 Requirements

* PowerShell (Windows) or Terminal (Linux).
> ⚠️ While Windows' command prompt may work, it is better to use PowerShell!
* You need to have the [Java Development Kit](https://adoptium.net/) installed on your machine. The minimum required version to compile and run Loritta is JDK 17, however newer JDK versions should also compile and run Loritta without any issues.
* You need to have Git installed on your machine.
* Check if your machine has the `JAVA_HOME` property set correctly, newer JDK versions downloaded from Adoptium may already have the variable set correctly.
* You can check if the variable is set by using... `echo $env:JAVA_HOME`
    * PowerShell: `echo $env:JAVA_HOME`
    * Bash: `echo $JAVA_HOME`

### 🧹 Preparing the environment

Clone the repository with git

```bash
git clone -b main https://github.com/LorittaBot/Loritta.git
```

### 🐘 Compiling with Gradle

Open the cloned repository folder and open PowerShell or the terminal inside of the folder, then, build Loritta with Gradle:

```bash
./gradlew build
```

> 💡 If you have Gradle installed on your computer, you can use `gradle build` instead of `./gradlew build`

> ⚠️ If Gradle complains that methods from the JDK are missing, then you are using an outdated method (pre-JDK 17) version, please update your JDK! We are in `${currentYear}`, get off your dinosaur and get on the [latest JDK from the Adoptium train](https://adoptium.net/), choo choo! 🚄

If the build is successful, then congratulations 🎉! You have successfully compiled Loritta!

*You did it! Now... why not run her?* 🙃

## 💫 Running Loritta

This section is tailored to developers, *not* for users that only want to self-host Loritta and do not care about changing her code. Check out the [How to self-host Loritta](SELF-HOSTING.md) tutorial if you only plan to host Loritta without doing any changes to her source code!

### 👷 Requirements

* Everything from the "Compiling Loritta" section.
* [JetBrains IntelliJ IDEA](https://www.jetbrains.com/pt-br/idea/)! The community edition is good enough, and it is free, so you don't need to be like "oh my god I need to *pay* for it". 😉
* PostgreSQL 12+, latest recommended.

### 🧹 Opening Loritta's project in IntelliJ IDEA

Open IntelliJ IDEA, then File -> Open -> Select the folder where you cloned Loritta and click "OK". IDEA will ask if the project is trusted by you, click "Trust Project".

After opening the project, wait until IDEA downloads all dependencies and indexes everything related to the project, this may take a long time, depending on your internet connection and your computers CPU and disk I/O speed.

### 🏃‍♂️ Running Loritta within IntelliJ IDEA

While it is possible to create a Docker image via Gradle, running Loritta within IntelliJ IDEA is easier and faster if you are changing code.

Right-click the `LorittaLauncher` class and click on the "Run 'LorittaLauncher'" option.

If this is the first time running Loritta, she will create a file named `loritta.conf` in your project's root folder. Open the `loritta.conf` and configure the `loritta.conf` file:

Then run the `LorittaLauncher` class again! If everything goes well, Loritta will be up and running!

### ⚡ Tips

* When developing things for the `:loritta-bot-discord` module, you can skip the time-consuming `:web:spicy-morenitta` distribution task during compilation by adding `net.perfectdreams.loritta.skipSpicyMorenittaDistribution=true` to your `gradle.properties`.
* The [`.junie/guidelines.md`](../.junie/guidelines.md) file has explanations of how Loritta the core principles behind Loritta's design. The document is for [JetBrains's Junie AI agent](https://www.jetbrains.com/junie/), but it is also useful because it explains how Loritta's code is structured.