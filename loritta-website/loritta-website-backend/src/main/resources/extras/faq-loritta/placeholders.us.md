title: "Placeholders & Variables"
authors: [ "peterstark000", "mrpowergamerbr" ]
---

There exist several placeholders that you can use in Loritta's messages!

With them your messages that you make so that Loritta speaks, can become more explicative, clear and more beautiful <img src="https://cdn.discordapp.com/emojis/626942886432473098.png?v=1" class="inline-emoji">.

If you are wanting precisely all this practicality and beauty that this function can provide you, congratulations, you are in the right section!

## Placeholders, what are they?

A placeholder is briefly, a word/term that has the purpose of being replaced by another. Confused? A little right? I will try to explain better! Upon trying to use `+say Hi {user}!` you expect that Loritta says exactly "`Hi {user}`" right?

{{ renderDiscordMessage("placeholders_say_example_1.html", "no_tooltips.conf") }}

But as **{user}** is a placeholder, this part of the message will be replaced by the name of the user that executed the command, still didn't understand? Follow the example:

{{ renderDiscordMessage("placeholders_say_examples_2.html", "no_tooltips.conf") }}

Understood? Great! Now I will list all the placeholders and their functions, follow with me!   

## User

|      Placeholder     |                                                                  Meaning                                                                  |
|:--------------------:|:---------------------------------------------------------------------------------------------------------------------------------------------:|
|        {user}        |                                                 Shows the name of the user that provoked the action.                                                 |
|        {@user}       |                                                    Mentions the user that provoked the action.                                                    |
|       {user.id}      |                                                  Shows the ID of the user that provoked the action.                                                  |
|      {user.tag}      |                                                  Shows the tag of the user that provoked the action.                                                 |
| {user.discriminator} |                                             Shows the discriminator of the user that provoked the action.                                            |
|     {user.avatar}    |                                                       Shows the URL of the user's avatar.                                                      |
|    {user.nickname}   | Shows the name of the user in the server (in case they have changed their nickname in the server, their nickname will appear, instead of the original name) |

## Server

|    Placeholder   |                 Meaning                 |
|:----------------:|:-------------------------------------------:|
|      {guild}     |          Shows the name of the server.         |
|   {guild-size}   | Shows the quantity of members in the server. |
| {guild-icon-url} |      Shows the URL of the server icon.     |

## Moderation

|      Placeholder      |                                         Meaning                                        |
|:---------------------:|:------------------------------------------------------------------------------------------:|
|        {reason}       |      Reason of the punishment, in case no reason has been specified, this will be empty.     |
|       {duration}      | Duration of the punishment applied, in case in the command it is not specified, it will appear as permanent |
|      {punishment}     |                The type of punishment that was applied (ban, mute, kick, etc...)                |
|        {@staff}       |                     Mentions the staff user that applied the punishment                     |
|        {staff}        |                  Shows the name of the staff user that applied the punishment                  |
|       {staff.id}      |                   Shows the ID of the staff user that applied the punishment                   |
|      {staff.tag}      |                   Shows the tag of the staff user that applied the punishment                  |
| {staff.discriminator} |              Shows the discriminator of the staff user that applied the punishment             |
|     {staff.avatar}    |              Shows the URL of the avatar of the staff user that applied the punishment             |

## Video, Live Stream and Tweet Notification System

| Placeholder |                                     Meaning                                     |
|:-----------:|:-----------------------------------------------------------------------------------:|
|    {link}   | Shows the link of the video/tweet/live stream of some channel/account of the notification systems. |
|    {game}   |        Shows which game or application is being broadcast in the live stream (Twitch)       |

## Levels by Experience

|             Placeholder             |                                            Meaning                                            |
|:-----------------------------------:|:-------------------------------------------------------------------------------------------------:|
|               {level}               |                        Shows the current level of the member that provoked the action.                        |
|                 {xp}                |                     Shows the current experience of the member that provoked the action.                     |
|         {experience.ranking}        |            Position in the experience ranking of the member that provoked the action in the server.           |
| {experience.next-level.required-xp} |        Shows the experience necessary for the next level of the member that provoked the action.        |
|   {experience.next-level.total-xp}  | Total of experience necessary for the member that provoked to manage to evolve to the next level |
|       {experience.next-level}       |                       Shows the next level of the member that provoked the action.                       |
