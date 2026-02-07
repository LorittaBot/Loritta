title: "Moderation"
authors: [ "nightdavisao" ]
---
Loritta has a **moderation module** in the Dashboard.  
Configuring it, you can define a channel to announce punishments made with the commands.  
Note that, you need to use the moderation commands so that Loritta announces the punishments in your server.  
To access the module, use the command `panel` in your server, and next, click on "Moderation".  

## Essential commands for your server

#### There exists commands made for punishments, these are:
* `ban`: Bans members from your server, this command uses the Discord ban, that is, the users are banned by IP.
* `mute`: Mutes members in your server. It will be asked what is the duration of the mute after you use the command. Note that you must not put the duration upon using the command but rather after executing the command. 
* `warn`: Gives a warn in members in your server. In the panel of your server, in Moderation, you can adjust different punishments to each warns made with the command.
* `kick`: Kicks members from your server.

Observe that in the punishment commands, you can insert several users to punish.

#### There also exists commands to remove punishments, these are:
* `unban`: Unbans members of your server, in this command, you need to insert the user ID.
* `unwarn`: Removes a warn from a member.
* `unmute`: Removes mute from members.

#### Lastly, the commands that are not to punish or remove punishments:
* `baninfo`: Examine a ban of a member of your server.
* `warnlist`: Examine warns made with `warn` in a member of your server.
* `lock`: Lock members from sending messages in a channel. Upon using the command without specifying a channel, the channel which the command was executed will be locked.
* `unlock`: Unlock members from sending messages in a channel. Upon using the command without specifying a channel, the channel which the command was executed will be unlocked.

## The commands `lock` or `mute` do not work correctly in your server?
* Several things can make `lock` and `mute` not work. If I answer something when you try the command, read my message with attention because there can be the answer.
* I said that I locked the chat/muted the person but the member(s) still continue conversing? This person can be with a role that possesses the Administrator permission in the server. Administrators manage to speak in chats even with them locked or even with the muted role.

#### Are these people not administrators? Then it can be some problem in the channel permissions.
* When the command `lock` is executed, the permission "Send Messages" of the role `@everyone` is **denied**.
* And when the command `mute` is used, a role called "Muted" is defined in which the permission of "Send Messages" in text channels is **denied**.
* If the person is with another role above the role used by the such command, and this role has the such permission granted, this permission denial is annulled.
* Leave the permission of this role in null, clicking on the slash that is in the middle. Edit the channel permissions accessing **"Edit Channel"** with the right mouse button or holding the touch on the channel.
