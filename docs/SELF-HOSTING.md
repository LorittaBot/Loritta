# 🚀 How to self-host Loritta

This section is tailored to users that only want to self-host Loritta without contributing back code, *not* for developers. Check out the [Developing Loritta](DEVELOPERS.md) tutorial if you only want to edit Loritta's code and run her!

## 📜 Self-Hosting conditions and warnings

In addition to the project's [AGPL-3.0 license](../LICENSE), here are some other things that you should keep in mind...

1. We keep the source code open so people can see, learn and be inspired by how Loritta was made and, if they want to, they can help the project with features and bug fixes.
2. This is a community project, if you make changes to Loritta's source code you need to follow the [AGPL-3.0 license](../LICENSE) and keep the changes open source! And, if you want to help Loritta, why not create a pull request? 😉
3. We **won't** give support for self-hosted instances, you need to know how to troubleshoot the issues yourself. We tried to make the self-host process as painless as possible, but it is impossible to know all the different issues you may find.
4. Don't lie saying that you "created Loritta". Please give credits to the creators!
5. Loritta requires a lot of different API keys for a lot of features. While they aren't required, you may encounter issues when trying to use some of the features.
6. To avoid problems and confusions, we **do not allow** using the name "Loritta", "Lori" or any similar names on your selfhosted versions. Call her "Nicole" if you aren't creative enough to create your own name to give to your selfhosted version. Don't like "Nicole"? Generate your own [here](https://www.behindthename.com/random/)!

Seems too hard but you *really* want to use Loritta? Don't worry, you can use our free public instance by clicking here [clicking here](https://l.lori.fun/add-lori)!

## 👷 Requirements

* Docker
* * Windows or macOS: [Docker for Desktop](https://docs.docker.com/desktop/)
* * Linux: [Docker](https://docs.docker.com/engine/install/)
* Docker Desktop
* * Windows or macOS: Docker for Desktop already includes Docker Compose, yay!
* * Linux: [Docker Compose](https://docs.docker.com/compose/install/)

## 🧹 Preparing the environment
Create a empty folder somewhere in your OS, why an empty folder? Just to keep things tidy! :3

```yml
version: "3.9"
services:
  cinnamon:
    image: "ghcr.io/lorittabot/cinnamon-gateway:latest"
    environment:
      LC_CTYPE: "en_US.UTF-8"
      LC_ALL: "en_US.UTF-8"
    volumes:
    - type: bind
      source: ./loritta.conf
      target: /loritta.conf
      read_only: false
  postgresql:
    image: postgres:13-alpine
    environment:
      POSTGRES_USER: loritta
      POSTGRES_PASSWORD: lorisocute
  gabriela-image-server:
    image: ghcr.io/lorittabot/gabriela-image-server:latest
```

**This will create three different containers:**
* Loritta Cinnamon, using interactions over Discord's Gateway
* PostgreSQL 13
* [Gabriela's Image Server](https://github.com/LorittaBot/GabrielaImageServer), used for image generation

In the same folder, create a empty file named `loritta.conf`.

## 🏃‍♂️ Running Loritta with Docker Compose

Open a PowerShell/Terminal in the folder where the `docker-compose.yml` file is, then run `docker-compose up`, this will download the containers and start them!

When the line "After configuring the file, run me again!" is shown in the console, CTRL-C to exit the Docker Compose process and then open the `loritta.conf` and configure the `loritta.conf` file:

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
    │   │   * Set this to the POSTGRES_USER configured in docker-compose.yml
    │   ├── address
    │   │   * Set this to "postgres:5432", "postgres" is PostgreSQL's container name in docker-compose.yml
    │   ├── username
    │   │   * Set this to the POSTGRES_USER configured in docker-compose.yml
    │   └── password
    │       * Set this to the POSTGRES_PASSWORD configured in docker-compose.
    │
    └── gabrielaImageServer
        └── url
            * Set this to "http://gabriela-image-server:8080/", "gabriela-image-server" is Gabriela's Image Server's container name in docker-compose.yml
```

If everything goes well, Loritta will be up and running!