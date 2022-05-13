# 👩‍💻 Developing Loritta

Loritta is made with [Kotlin](https://kotlinlang.org/). Kotlin is a modern, concise and safe programming language made by JetBrains. If you already used Java before, Kotlin will feel familiar to you. Heck, it can even run on the JVM!

Why Kotlin? Because Kotlin is my beloved. 🥺 <img src="https://cdn.discordapp.com/emojis/841285914159611914.gif" height="24" />

Originally Loritta was made in Java, but in May 2017 I decided migrate to Kotlin!

## 🛠️ Compiling Loritta

### 👷 Requirements

* PowerShell (Windows) or Terminal (Linux).
> ⚠️ While Windows' command prompt may work, it is better to use PowerShell!
* You need to have the [Java Development Kit](https://adoptium.net/) installed on your machine. The minimum required version to compile and run Loritta is JDK 15, however newer JDK versions should also compile and run Loritta without any issues.
* You need to have Git installed on your machine.
* Check if your machine has the `JAVA_HOME` property set correctly, newer JDK versions downloaded from Adoptium may already have the variable set correctly.
* You can check if the variable is set by using... `echo $env:JAVA_HOME` 
  * PowerShell: `echo $env:JAVA_HOME`
  * Bash: `echo $JAVA_HOME`
* pngquant (only required if you want to build the `:web:showtime:backend` module)
  * Windows or MacOS: https://pngquant.org/
  * Linux: Install it via your distro's package manager
* gifsicle (only required if you want to build the `:web:showtime:backend` module)
  * Windows or MacOS: https://www.lcdf.org/gifsicle/
  * Linux: Install it via your distro's package manager
* RabbitMQ (only required if you want to run the `:microservices:discord-gateway-events-processor` module)

### 🧹 Preparing the environment 

Clone the repository with git

```bash
git clone -b cinnamon https://github.com/LorittaBot/Loritta.git
```

You may have noticed the `-b` flag, this is used to indicate that we are cloning the `cinnamon` `b`ranch.

#### Preparing pngquant and gifsicle path (`:web:showtime:backend` only)

> pngquant was not found in the path! Please install pngquant or, if it is already installed, provide the path via the "pngquant.path" system property (Example: "./gradlew -Dpngquant.path=/home/lorittapath/to/pngquant/pngquant ...")

If you are going to compile the `:web:showtime:backend` module, you need to setup pngquant's and gifsicle's path so the script is able to find where `pngquant` and `gifsicle` is. By default, it searches on the following paths:
* In your `PATH`
* In `/usr/bin/pngquant`/`/usr/bin/gifsicle`
* In the path specified by the system property `pngquant.path`/`gifsicle.path`

If you installed `pngquant` or `gifsicle` using your distro's package manager, it should already be in your `PATH` so you don't need to do anything, but if you installed it manually or if you are using Windows/MacOS, you need to specify the `pngquant.path` and `gifsicle.path` system property.

Go to the `gradle.properties` file and append
```
systemProp.pngquant.path=/set/the/path/to/pngquant
```

Example:
```
systemProp.pngquant.path=L:\\Tools\\pngquant\\pngquant.exe
```

If you are compiling Loritta via the command line instead of compiling within IntelliJ IDEA, you can provide the system property to the `/gradlew` command directly
```
./gradlew -Dpngquant.path=/home/lorittapath/to/pngquant/pngquant ...
```

#### Preparing RabbitMQ (`:microservices:discord-gateway-events-processor` only)

RabbitMQ is used on the `:microservices:discord-gateway-events-processor` module to process gateway events produced by Loritta's Legacy branch. Splitting the gateway process from the event processor process has the advantage of allowing bug fixes and new features to be deployed without requiring restarting all shards, which on a big bot may take a loooong time.

Setting up a RabbitMQ instance locally is easy with Docker, so that's what we are going to use!

First, we will run RabbitMQ with Docker

```bash
docker run -it --rm --hostname rabbitmq --name rabbitmq -p 5672:5672 rabbitmq:3.8
```

(The `--hostname rabbitmq` is required because RabbitMQ uses the hostname to persist the data, if we don't explicitly set it, Docker will use a random hostname when starting the container: <https://hub.docker.com/_/rabbitmq>)

We will create a virtual host because this allows us to not mix other application data that use RabbitMQ with our application data. If you know SQL, Virtual Hosts are like "databases".

```bash
docker exec rabbitmq rabbitmqctl add_vhost loritta
```

Because we aren't using the default RabbitMQ virtual host, we need to create a user.

```bash
docker exec rabbitmq rabbitmqctl add_user loritta insertnicepasswordhere
```

Our newly created user does not have permission to read our virtual host... So let's grant permission to it!

```bash
docker exec rabbitmq rabbitmqctl set_permissions --vhost loritta loritta ".*" ".*" ".*"
```

And that's it! You can use `docker exec rabbitmq rabbitmqctl list_queues --vhost loritta` to see the created queues and how many pending messages are in the queues.

You can also persist RabbitMQ's data (users, etc) by using this `docker-compose.yml` file.
```yml
# https://stackoverflow.com/a/67959283
version: "3.9"
services:
  rabbitmq:
    image: "rabbitmq:3.8"
    hostname: rabbitmq
    # Persists RabbitMQ data, this path needs to be changed if you are running on Windows!
    # You can also remove this if you don't care about persisting the data
    volumes:
      - /var/docker/rabbitmq/var/lib/rabbitmq:/var/lib/rabbitmq
    ports:
      - "5672:5672"
```

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

This section is tailored to developers, *not* for users that only want to self-host Loritta and do not care about changing her code her code. Check out the [How to self-host Loritta](SELF-HOSTING.md) tutorial if you only plan to host Loritta without doing any changes to her source code!

### 👷 Requirements

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