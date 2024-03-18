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

*You did it!*
