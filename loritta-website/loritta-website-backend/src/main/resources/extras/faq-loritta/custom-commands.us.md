title: "Custom Commands"
authors: [ "arth", "peterstark000" ]
---
<div class="centered-text">
<img src="/v3/assets/img/faq/custom_commands/banner.png" height="300" />
</div>

Did you know that you can create commands for your server? At the moment, only commands that send a ready message are possible, but even so you can create several things with this! Follow the steps below to know how to create one and know more about them. <img src="https://cdn.discordapp.com/emojis/673868465433477126.png?v=1" class="inline-emoji">

## How to create a custom command?
1. Access the control panel of your server by [clicking here](/dashboard) or using the command `+panel` in your server.
![1st step](/v3/assets/img/faq/custom_commands/1.png)

2. Upon accessing the panel, search for the Custom Commands module in the list to your right.
![2.1 step](/v3/assets/img/faq/custom_commands/2.1.png)
![2.2 step](/v3/assets/img/faq/custom_commands/2.2.png)

3. Now that you are already in the module, just click on `Add` and select `Text Command` to start your creations!
![3rd step](/v3/assets/img/faq/custom_commands/3.png)

4. A pop-up will open with the information for you to fill upon doing this. So that's where the magic starts! First you put the name of the command that you want, and as it says in the notice, do not put my prefix in the name of the command (prefix is the character that you configure to put before using any command, like `+`, my default prefix). After, in the text box below, you fill what you want me to speak every time someone uses a command!
![4th step](/v3/assets/img/faq/custom_commands/4.png)

5. After filled, click on `Save` in the pop-up and on the green button of the page. All done!  <img src="https://cdn.discordapp.com/emojis/519546310978830355.png?v=1" class="inline-emoji"> Now I will answer the members of your server when they use a command with the name that you put.
![5.1 step](/v3/assets/img/faq/custom_commands/5.1.png)
![5.2 step](/v3/assets/img/faq/custom_commands/5.2.png)

**Attention**: Putting anything NSFW in the custom commands like in any other section of the panel will result in your permanent ban of the use of my functions! <img src="https://cdn.discordapp.com/emojis/395010059157110785.png?v=1" class="inline-emoji"> Do not be fooled, we know when you do this. [Click here](/guidelines) to read my guidelines.

## Ideas for you to make
You can make several things, like putting so that I send the link of your channel when they use the command, thus avoiding people asking what is your channel and you having to go get it every time... That is annoying! And it doesn't need to be only link of your channel, you can put what you want.

Want to make something more complex? Can too! Use the option of [Advanced Editor](https://embed.loritta.website/) to configure an [embed](/extras/faq-loritta/embeds) for you or then use [placeholders](/extras/faq-loritta/placeholders), or... combine the two! In [my community server](https://discord.gg/lori) I have a custom command that I combined the two things. If you use `+level` in one of the commands channels that we have there, I will send you information about your XP in the server. If you want the embed code of this command, [click here](https://gist.github.com/MrPowerGamerBR/0d85d998e9ef656e7a6ab8b04f029380)!

{{ renderDiscordMessage("level_card.html", "no_tooltips.conf") }}

## Notes
* At the moment it is still not possible to make commands that interact with users, like the action commands (`+kiss @user`, `+hug @user`, etc).

* At the moment it is still not possible to make so that I send files, like images, in the custom commands. If you want to put an image in your command, just put the link, but if you want to leave the link hidden, leave inside an [embed](/extras/faq-loritta/embeds). I recommend that you use my [Advanced Editor](https://embed.loritta.website/).

* In the past my custom commands were made in JavaScript, so you managed to do MANY things using them. Only that due to a very grave bug that could have exploded all servers (discovered by Mantaro developers <img src="https://cdn.discordapp.com/emojis/732706868224327702.png?v=1" class="inline-emoji">) I had to remove this function from my panel... <img src="https://cdn.discordapp.com/emojis/626942886251855872.png?v=1" class="inline-emoji"> Maybe in a not so distant future you manage to make custom commands only that this time in Kotlin or then in a programming language of mine.
