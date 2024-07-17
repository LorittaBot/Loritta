# 🚀 Selfhosting Loritta

This section is not recommended to users who wants to contribute to her code, check out [Developing Loritta](https://github.com/hechfx).

**DISCLAIMER**: We don't give support for self-hosted instances. You need to troubleshoot the issues yourself.

## Table of Contents
1. [Conditions and Warnings](#-conditions-and-warnings)
2. [Requirements](#-requirements)
3. [Preparing Environment](#-preparing-environment)
4. [Running Loritta](#-running-loritta)

## 📜 Conditions and Warnings

In addition to the project's [AGPL-3.0 license](../LICENSE), here are some other things that you should keep in mind:
1. We keep the source code open so people can see, learn and be inspired by how Loritta was made and, if they want to, they can help the project with features and bug fixes.
2. This is a community project, if you make changes to Loritta's source code you need to follow the [AGPL-3.0 license](../LICENSE) and keep the changes open source! And, if you want to help Loritta, why not create a pull request? 😉
3. **We don't give support for self-hosted instances**, you need to know how to troubleshoot the issues yourself. We tried to make the selfhost process as painless as possible, but it's impossible to be aware of all the differente issues you may find.
4. Don't spread misunderstanding about Loritta's ownership. Please, give credits to the creators.
5. Loritta requires a lot of different API keys for a lot of features. While they aren't required, you may encounter issues when trying to use some of the features.
6. To avoid problems, **we don't allow using "Loritta", "Lori" or similar names on your selfhosted version**. You can call her "Nicole" if you aren't creative enough to give a name to your selfhost. Don't like "Nicole"? Generate your own name [here](https://www.behindthename.com/random/).

It's too complicated for you? Don't worry, you can use our free public instance by [clicking here](https://l.lori.fun/add-lori).

## 👷 Requirements
- Docker
    - [Docker for Desktop](https://docs.docker.com/desktop/) (Windows & MacOS; Includes Docker Compose)
    - [Docker](https://docs.docker.com/engine/install/) (Linux)
- Docker Compose
    - [Docker Compose](https://docs.docker.com/compose/install/) (Linux)

## 🧹 Preparing Environment

First, you need to create an empty folder somewhere in our OS. Why? Just to keep things tidy! :3

Then, create a file named `docker-compose.yml` and paste this inside:

```yml
version: "3.9"
services:
  cinnamon:
    image: "ghcr.io/lorittabot/loritta-morenitta:latest"
    environment:
      LC_CTYPE: "en_US.UTF-8"
      LC_ALL: "en_US.UTF-8"
    volumes:
    - type: bind
      source: ./loritta.conf
      target: /loritta.conf
      read_only: false
  postgresql:
    image: postgres:16-bullseye
    environment:
      POSTGRES_USER: loritta
      POSTGRES_PASSWORD: lorisocute
  gabriela-image-server:
    image: ghcr.io/lorittabot/gabriela-image-server:latest
```

### This file contains instructions to Docker Compose and tells it to create three specific containers:
- Loritta;
- PostgreSQL 13;
- [Gabriela's Image Server](https://github.com/LorittaBot/GabrielaImageServer) (Image generation purposes).

In the same folder as `docker-compose.yml` create an empty file named `loritta.conf`.

## 🏃‍♂️ Running Loritta

Open your favorite terminal inside the folder where are the files then run `docker-compose up`, this will download the containers and start them!

When the line `After configuring the file, run me again` is shown in console, exit the Docker Compose process with `CTRL+C`, open `loritta.conf` and configure it:

```plaintext
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

If everything goes well, your instance will be up and running!
