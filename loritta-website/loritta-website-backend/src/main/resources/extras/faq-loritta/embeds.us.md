title: "Embeds"
authors: [ "peterstark000", "mrpowergamerbr" ]
---
<div class="centered-text">
<img src="/v3/assets/img/faq/embeds/banner.png" height="300" />
</div>

Embeds are a special type of message on Discord, you probably have seen them before... those little boxes that possess a little color on the left and that moreover can have images, author, footer and much more! Yes, they are beautiful, and of course Loritta supports them too!

{{ renderDiscordMessage("embeds_embed_example.html", "no_tooltips.conf", "remove-reply", "remove-message-content") }}

On Loritta, embeds are supported in all places that accept a message in the panel, when putting an embed in the message location, it will show the embed in the preview and will show that it is in "Extended Code" mode.

Embeds are also supported in some commands like `+say`, just put the JSON code in front of the command name. In this way, it is possible to send those cute messages in channels. (Obs: Placeholders also work in say and in some other commands. If you still don't know what placeholders are, see by [clicking here](/extras/faq-loritta/placeholders)!)

Embeds are formatted in JSON, based on the way that Discord sends embeds.

## Tips
* [Placeholders](/extras/faq-loritta/placeholders) are supported inside embeds!
* Not managing to see embeds? Then it means that you disabled the link preview in the "Text & Images" settings of your Discord!
* Mentions inside embeds may not work correctly (users mentioned inside an embed will not receive notifications and, in case it is a user that your Discord has not loaded, it will appear `<@UserId>`), in case this happens, add the user mention in the content too!
* Embeds on Discord Mobile (iOS/Android) may not work exactly as they work on Discord computer/web!
* The colors utilized in embeds are in decimal format, to get the color in decimal format, utilize `+colorinfo`!

## Loritta's Embed Editor

Loritta's website has its own embed editor, this facilitates a lot both for you and for us, since we reuse the message rendering from the Embed Editor website for Loritta's website. If you don't know the embed editor yet, just [click here](https://embeds.loritta.website/) to be redirected to it.

<div class="centered-text">
<img src="/v3/assets/img/faq/embeds/embed_editor.png" height="300" />
</div>

*Demonstrative screenshot of the Embed Editor, to start editing click on "Add embed".*

You can edit texts/colors/etc clicking on the section that you want to edit, for example: If you want to change the embed color, just click on the side where the color stays. If you want to change the embed title, just click on the title.

The embed editor is integrated with Loritta's website! To open a message in the Embed Editor, just click on the "Advanced Editor" button that you will be taken to the embed editor.

## Templates

For you to see how embeds are made, we left some ready embeds for you to see, use and have fun! To use them, copy the code, put in the text place in Loritta's panel where you want to use the embed and see how it looked!

### Join Message
```json
{
   "content":"{@user}",
   "embed":{
      "color":-9270822,
      "title":"👋 Welcome!",
      "description":"Hello {@user}, I hope you have fun in my server! <:loritta:331179879582269451>",
      "author":{
         "name":"{user.tag}",
         "icon_url":"{user.avatar}"
      },
      "thumbnail":{
         "url":"{user.url}"
      },
    "footer": {
      "text": "User ID: {user.id}"
    }
   }
}
```

### Leave Message
```json
{
   "content":"",
   "embed":{
      "color":-6250077,
      "title":"😭 #upset!",
      "description":"⚰ **{user}** left the server... <:lori_triste:370344565967814659>",
      "author":{
         "name":"{user.tag}",
         "icon_url":"{user.avatar}"
      },
      "thumbnail":{
         "url":"{user.avatar}"
      },
    "footer": {
      "text": "User ID: {user.id}"
    }
   }
}
```

### Join Message (via direct message)
```json
{
   "content":" ",
   "embed":{
      "color":-16727585,
      "title":"👋 Welcome to {guild}!",
      "description":"Thanks for joining my server and have fun! Ah, and of course, don't forget to follow the server rules! <:loritta:331179879582269451>",
      "thumbnail":{
         "url":"https://loritta.website/assets/img/loritta_star.png"
      },
     "image":{ "url":"https://loritta.website/assets/img/fanarts/Loritta_Headset_-_N3GR3SC0.png"}
   }
}
```

### Message when someone is punished (Moderation)
```json
{
   "content":"",
   "embed":{
      "color":-9270822,
      "title":"{user.tag} | {punishment}",
      "thumbnail": { "url" : "{staff-avatar-url}" },
      "description":"The user was punished for breaking the rules of {guild}, what can you do right, broke the #rules got punished! <:tavares:412577570190655489> :BlobAxolotlPride: <:smol_lori_putassa_aline:395010059157110785>",
      "fields": [
        {
            "name": "User Tag",
            "value": "`{user.tag}`",
            "inline": true
        },
        {
            "name": "User ID",
            "value": "`{user.id}`",
            "inline": true
        },
        {
            "name": "Who punished",
            "value": "`{staff}#{staff-discriminator}`",
            "inline": true
        },
        {
            "name": "Reason",
            "value": "{reason}",
            "inline": true
        }
      ],
    "footer": {
      "text": "User ID: {user-id}"
    }
   }
}
```

### Level Up Message
```json
{
   "content": "{@user}",
   "embed": {
     "description": "• Congratulations, **{user}**! You leveled up to level **{level}** (*{xp} XP*) and now are in #{experience.ranking} place in the server experience ranking! <a:lori_yay_wobbly:638040459721310238>",
     "color": 16773632,
     "image": {
       "url": "https://cdn.discordapp.com/attachments/358774895850815488/745417561524666459/advancement.png"
     },
     "author": {
       "name": "{user.tag}",
       "icon_url": "{user-avatar-url}"
     }
   }
}
```

### Experience Card Message

* This one in special is utilized in a [Custom Command](/extras/faq-loritta/custom-commands). Click on the blue letters to see our FAQ about it!
```json
{
  "content": "{@user}",
  "embed": {
    "title": "<:lori_kamehameha_1:727280767893504022> **| Profile Card of `{user.tag}`**",
    "description": "\n<:lori_barf:727583763646644225> **| Current Level:** `{level}`\n<:lori_water:728761705148186726> **| Current XP:** `{xp}`\n<:lori_point:731876009548316712> **| Rank:** `#{experience.ranking}`\n<:lori_stonks:788434890927505448> **| XP needed for the next level ({experience.next-level}):** `{experience.next-level.required-xp}`\n> <:lori_nice:726845783344939028> • **Tip from Lorota Jubinha:** keep chatting to level up. I know you will make it!\n",
    "color": -16145192,
    "thumbnail": {
      "url": "{user.avatar}"
    },
    "author": {
      "name": "{user.tag}",
      "icon_url": "{user.avatar}"
    }
  }
}
```
