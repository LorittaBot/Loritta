title: "Levels by Experience"
authors: [ "arth", "peterstark000" ]
---
<div class="centered-text">
<img src="/v3/assets/img/faq/experience/banner.png" height="300" />
</div>

Experience you earn chatting in the text channels of your server. You can see your XP using the command `+profile`. There exists local XP (XP inside only one server) and global XP (XP in all servers that I am in). Each experience level is 1,000 XP.

You can see the experience rank of your server using `+rank` and the global rank using `+rank global`.

In this section you will see the rules that I follow to give XP in a message, the Levels by Experience module in the control panel, things that you can configure in the control panel in relation to XP in your server and how to make a profile card command for your server. <img src="https://cdn.discordapp.com/emojis/731873534036541500.png?v=1" class="inline-emoji">

## Rules that I follow to give XP
I am a little demanding to reward the XP of a message! <img src="https://cdn.discordapp.com/emojis/729723959284727808.png?v=1" class="inline-emoji"> For me to reward the XP:

* The message has to have more than 5 characters.
* The message has to be different from the last message sent.
* The message has to be sent in a way that is humanly possible for the person to have written all that. If, for example, the user's last message was sent 5 seconds ago and the person sends a giant text, I ignore (I calculate the quantity of characters in the message / 7, if the difference between the last message and the calculation is bigger, I let it).
* The message, WITHOUT REPEATED CHARACTERS (for example, kkkkkkkkkk becomes just k) needs to have more than 12 characters.

If everything goes right, the message XP is the quantity of non-repeated characters / 7 and quantity of non-repeated characters / 4, if the XP surpasses 35, I leave 35.

## Configuring the Levels by Experience
1. Access the control panel of your server by [clicking here](/dashboard) or using the command `+panel` in your server.
![1st step](/v3/assets/img/faq/servers.png)

2. Upon accessing the panel, you will start by the General Configuration module. Search then for the Levels by Experience module in the list to your left.
![2nd step](/v3/assets/img/faq/experience/find_module.png)

3. Now that you are in the levels by experience module, you can configure several things related to the XP of your server's members! 
![3rd step](/v3/assets/img/faq/experience/configuration.png)

4. After configuring, scroll down the page and search for a green button written `Save`. It is very important that you save the configurations made when you mess with the control panel. All done! <img src="https://cdn.discordapp.com/emojis/519546310978830355.png?v=1" class="inline-emoji"> I will follow what you asked in question of XP now.
![4° passo](/v3/assets/img/faq/experience/save.png)

**Attention**: Normal users manage to put 15 roles by experience in their server. Whereas <img src="https://cdn.discordapp.com/emojis/593979718919913474.png?v=1" class="inline-emoji"> premium users <img src="https://cdn.discordapp.com/emojis/593979718919913474.png?v=1" class="inline-emoji">, activating a [premium key](/extras/faq-loritta/premium-keys) in the server, manage to put from 30 to 100 roles! Whoosh! [Click here](/donate) to become a premium user.

### In the control panel you can:
* Configure messages so that I send when someone levels up
* Establish roles for people to receive when they reach certain level
* Configure so that specific roles earn more/less experience than other members
* Configure so that specific roles do not earn experience
* Configure text channels to not give experience

## Profile Card (`+level`)
{{ renderDiscordMessage("level_card.html", "no_tooltips.conf") }}

The command `+level` is a command available only in my [community server](https://discord.gg/lori), but you can also add it in your server! [Click here](https://gist.github.com/MrPowerGamerBR/0d85d998e9ef656e7a6ab8b04f029380) if you want to see the code of the command [embed](/extras/faq-loritta/embeds). Want to know more about custom commands? [Click here](/extras/faq-loritta/custom-commands)!
