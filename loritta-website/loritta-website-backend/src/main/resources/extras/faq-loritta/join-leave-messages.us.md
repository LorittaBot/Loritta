title: "How to enable and configure the join and leave message."
authors: [ "ranni" ]
---
<img src="https://cdn.discordapp.com/attachments/397295975175028736/799989411063398400/loritta_welcomer.png" style="text-align: center;" height="300" />
Announce who is joining and leaving your server the way you wanted! Send messages to newbies via direct message with information about your server to not fill the chat with repeated information and much more!

#### Start by choosing the server! Access the Server Control Panel by [clicking here](/dashboard) and choose the server you want to configure!
![Step](https://cdn.discordapp.com/attachments/397295975175028736/819936313582026782/FAQ-1.png)

#### Let's enable the module! Upon enabling it is essential that you choose the options below that you want to enable, so it will send the configured message!
![Step2](https://cdn.discordapp.com/attachments/397295975175028736/819935803064451072/FAQ-2.png)


## How to configure the join/leave of members in a chat?
**1.** Mark the options that you want Loritta to send message about the function! Remember: You have to configure the message.
![Step3](https://cdn.discordapp.com/attachments/397295975175028736/819936291730096148/FAQ-3.png)

**2.** **IMPORTANT!** Choose the channels that are going to send message of leave and join of members.
![Step4](https://cdn.discordapp.com/attachments/397295975175028736/819937033657122856/FAQ-4.png)


## How to customize the messages?
**1.** To customize the messages you have to choose between using or not the Embeds.
* If you are going to use Embed [CLICK HERE](/extras/faq-loritta/embeds) to see all the templates in detail.
* If you are not going to use Embed, just go to the tutorial further below to see the explanation in detail of how to use.

**2.** Inform yourself about the variables by [clicking here](/extras/faq-loritta/placeholders), it is important to use with or without embed.

## Configuring with Embed:
**1.** You can use the base template to start the beautiful editing of your embed, with the code:
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
**2.** Click above on the button **"ADVANCED EDITOR"** and you will be taken to the page to edit the embed, it is very simple!
![Step6](https://cdn.discordapp.com/attachments/397295975175028736/819937702329581608/FAQ-5.png)

**3.** Now just click on the sections and customize it to your taste! Remembering to always put the variables by [clicking here](/extras/faq-loritta/placeholders).
![Step7](https://cdn.discordapp.com/attachments/397295975175028736/819939076081713172/FAQ-6.png)

**4.** Ready! Now just go to the Control Panel tab that the editing is there, then just go to the end of the page and **"SAVE"**.
![Step8](https://cdn.discordapp.com/attachments/397295975175028736/819939670607003678/FAQ-7.png)

*P.S. Any editing that you make on the "ADVANCED EDITOR" page is put automatically in the code, do not CLOSE the Control Panel.*

## Tips
* This embed is just an example, you have infinite customizations! See about Embeds by [clicking here](/extras/faq-loritta/embeds).
* Put the necessary variables by [clicking here](/extras/faq-loritta/placeholders), so that it displays the basic information in the message.
* If you want to customize the embed [click here](https://embeds.loritta.website), just copy the code generated after editing and done.

#### If you followed all the steps above, the tutorial with embed ends here, hope it helped! :)

## Configuring without Embed:
**1.** Put the message that you want in the dialog box, remembering to put the [variables](/extras/faq-loritta/placeholders) together.
![Step9](https://cdn.discordapp.com/attachments/397295975175028736/819941367559880705/FAQ-9.png)

**2.** Ready! Just save as you prefer, this configuration also applies to other functions like "Member leave".

#### If you followed all the steps above, the tutorial without embed ends here, hope it helped! :)

## How do I delete Loritta's message after the join/leave?
For you who configured the join of members in the `#chat` of your server and want to avoid pollution or just want that after a time it is deleted, there is the option that Loritta
deletes the message after some time, just go below the **"Customization message"** of member join and leave and configure the delay in seconds.
![Step11](https://cdn.discordapp.com/attachments/397295975175028736/819941755251458088/FAQ-11.png)

## How do I configure for Loritta to send in DM after joining?
**1.** Enable in the initial configurations  *"Enable the messages sent in the user's direct messages when someone joins"*.
![Step12](https://cdn.discordapp.com/attachments/397295975175028736/819942428396093450/FAQ-12.png)

**2.** In the dialog box type the message or paste the embed code by [clicking here to see the templates](/extras/faq-loritta/embeds).
![Step13](https://cdn.discordapp.com/attachments/397295975175028736/819942903261429760/Sem_Titulo-13.png)

**3.** Ready! Every time some member joins your server (that has private messages open), they will receive the message giving welcome!
![Step14](https://cdn.discordapp.com/attachments/397295975175028736/819944305346478090/Sem_Titulo-14.png)

## How do I see other embed templates?
To see other templates you can [click here](/extras/faq-loritta/embeds) and see what other ways of customizing your beautiful server has available, as well as template for when a member is banned or leaves the server, it is also necessary to understand about the variables by [clicking here](/extras/faq-loritta/placeholders).
