# 👩‍💻 Developing Loritta

## 🛠️ Compiling Loritta

### 👷 Requirements

* PowerShell (Windows) or Terminal (Linux).
> ⚠️ While Windows' command prompt may work, it is better to use PowerShell!
* You need to have the [Java Development Kit](https://adoptium.net/) installed on your machine. The minimum required version to compile and run Loritta is JDK 15, however newer JDK versions should also compile and run Loritta without any issues.
* You need to have Git installed on your machine.
* Check if your machine has the `JAVA_HOME` property set correctly, newer JDK versions downloaded from Adoptium may already have the variable set correctly.
* You can check if the variable is set by using... `echo $env:JAVA_HOME` 
* * PowerShell: `echo $env:JAVA_HOME`
* * Bash: `echo $JAVA_HOME`

### 🧹 Preparing the enviornment 

Clone the repository with git

```bash
git clone -b cinnamon https://github.com/LorittaBot/Loritta.git
```

You may have noticed the `-b` flag, this is used to indicate that we are cloning the `cinnamon` `b`ranch.

### 🐘 Compiling with Gradle

Open the cloned repository folder and open PowerShell or the terminal inside of the folder, then, build Loritta with Gradle:
   
```bash
./gradlew build
```
   
> 💡 If you have Gradle installed on your computer, you can use `gradle build` instead of `./gradlew build`

> ⚠️ If Gradle complains that the `readAllBytes()` method is missing, then you are using an outdated method (pre-JDK 9) version, please update your JDK! We are in `${currentYear}`, get off your dinosaur and get on the [latest JDK from the Adoptium train](https://adoptium.net/), choo choo! 🚄

If the build is successful, then congratulations 🎉! You have successfully compiled Loritta!

*You did it! Now... why not run her?* 🙃

## 💫 Running Loritta

This section is tailored to developers, *not* for users that want to only self-host Loritta without contributing back code. Check out the [How to self-host Loritta](SELF-HOSTING.md) tutorial if you only want to self-host Loritta and not contribute back code.

### 👷 Prerequisites

* Everything from the "Compiling Loritta" section.
* [JetBrains IntelliJ IDEA](https://www.jetbrains.com/pt-br/idea/)! The community edition is good enough and it is free, so you don't need to be like "oh my god I need to *pay* for it". 😉
* PostgreSQL 12+, latest recommended.

### 🧹 Opening Loritta's project in IntelliJ IDEA

Open IntelliJ IDEA, then File -> Open -> Select the folder where you cloned Loritta and click "OK". IDEA will ask if the project is trusted by you, click "Trust Project".

After opening the project, wait until IDEA downloads all dependencies and indexes everything related to the project, this may take a long time, depending on your internet connection and your computers CPU and disk I/O speed.

### 🏃‍♂️ Running Loritta within IntelliJ IDEA

While it is possible to create a Docker image via Gradle, running Loritta within IntelliJ IDEA is easier and faster if you are changing code.

**There are two launchers that you can use:**

#### `LorittaCinnamonGatewayLauncher` (`:discord:gateway`)
* Interactions over Discord's Gateway.
* No need to expose a public connection.
* Used for testing.

#### `LorittaCinnamonWebServerLauncher` (`:discord:web-server`)
* Interactions over HTTP.
* Requires exposing a public web server.
* Sharding a web server is easier and faster than sharding a Discord Gateway connection.
* Used in production.

So, because we are going to run Loritta just for testing purposes, let's use `LorittaCinnamonGatewayLauncher`!

After finding the class, right click the `LorittaCinnamonGatewayLauncher` class and click on the "Run 'LorittaCinnamonGatewayLauncher'" option.

If this is the first time running Loritta, she will create a file named `loritta.conf` in your project's root folder. Open the `loritta.conf` and configure the `loritta.conf` file:

```ascii
📄 loritta.conf
├── discord
│   ├── token
│   │   * Your bot's Discord token
│   └── applicationId
│       * Your bot's application ID
│
├── interactions
│   └── guildsToBeRegistered
│       * The guild ID of your server
│
└── services
    ├── pudding
    │   ├── type
    │   │   * Set this to POSTGRESQL
    │   ├── database
    │   │   * Set this to your PostgreSQL database name
    │   ├── address
    │   │   * Set this to your PostgreSQL address
    │   ├── username
    │   │   * Set this to your PostgreSQL username
    │   └── password
    │       * Set this to your PostgreSQL password
    │
    └── gabrielaImageServer
        └── url
            * Set this to "http://YourGabrielaImageServerIp:8080/", you can set it to "https://gabriela.loritta.website/" if you aren't creating or editing new image/video generation commands
```

Then run the `LorittaCinnamonGatewayLauncher` class again! If everything goes well, Loritta will be up and running!