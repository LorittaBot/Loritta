title: "Placeholders & Variáveis"
authors: [ "peterstark000", "mrpowergamerbr" ]
---

Existem vários placeholders que você pode usar nas mensagens da Loritta!

Com eles as suas mensagens que você faz para que a Loritta fale, podem ficar mais explicativas, claras e mais bonitas <img src="https://cdn.discordapp.com/emojis/626942886432473098.png?v=1" class="inline-emoji">.

Se você está querendo justamente toda essa praticidade e beleza que essa função pode te proporcionar, parabéns, você está na sessão certa!

## Placeholders, o que são?

Um placeholder é resumidamente, uma palavra/termo que tem proposito de ser substituído por outro. Confuso? Um pouco né? Vou tentar explicar melhor! Ao tentar utilizar `+say Oi {user}!` você espera que a Loritta diga exatamente "`Oi {user}`" né?

{{ renderDiscordMessage("placeholders_say_example_1.html", "no_tooltips.conf") }}

Mas como **{user}** é um placeholder, essa parte da mensagem será substituida pelo nome do usuário que executou o comando, ainda não entendeu? Segue o exemplo:

{{ renderDiscordMessage("placeholders_say_examples_2.html", "no_tooltips.conf") }}

Entendeu? Ótimo! Agora vou listar todos os placeholders e suas funções, segue comigo!   

## Usuário

|      Placeholder     |                                                                  Significado                                                                  |
|:--------------------:|:---------------------------------------------------------------------------------------------------------------------------------------------:|
|        {user}        |                                                 Mostra o nome do usuário que provocou a ação.                                                 |
|        {@user}       |                                                    Menciona o usuário que provocou a ação.                                                    |
|       {user.id}      |                                                  Mostra o ID do usuário que provocou a ação.                                                  |
|      {user.tag}      |                                                  Mostra a tag do usuário que provocou a ação.                                                 |
| {user.discriminator} |                                             Mostra o discriminator do usuário que provocou a ação.                                            |
|     {user.avatar}    |                                                       Mostra a URL do avatar do usuário.                                                      |
|    {user.nickname}   | Mostra o nome do usuário no servidor (caso ele tenha mudado o apelido dele no servidor, irá aparecer o apelido dele, em vez do nome original) |

## Servidor

|    Placeholder   |                 Significado                 |
|:----------------:|:-------------------------------------------:|
|      {guild}     |          Mostra o nome do servidor.         |
|   {guild-size}   | Mostra a quantidade de membros no servidor. |
| {guild-icon-url} |      Mostra a URL do ícone do servidor.     |

## Moderação

|      Placeholder      |                                         Significado                                        |
|:---------------------:|:------------------------------------------------------------------------------------------:|
|        {reason}       |      Motivo da punição, caso nenhum motivo tenha sido especificado, isto estará vazio.     |
|       {duration}      | Duração da punição aplicada, caso no comando não se especifique, aparecerá como permanente |
|      {punishment}     |                O tipo de punição que foi aplicada (ban, mute, kick, etc...)                |
|        {@staff}       |                     Menciona o usuário da equipe que aplicou a punição                     |
|        {staff}        |                  Mostra o nome do usuário da equipe que aplicou a punição                  |
|       {staff.id}      |                   Mostra o ID do usuário da equipe que aplicou a punição                   |
|      {staff.tag}      |                   Mostra a tag do usuário da equipe que aplicou a punição                  |
| {staff.discriminator} |              Mostra o discriminator do usuário da equipe que aplicou a punição             |
|     {staff.avatar}    |              Mostra a URL do avatar do usuário da equipe que aplicou a punição             |

## Sistema de Notificação de Vídeos, Lives e Tweets

| Placeholder |                                     Significado                                     |
|:-----------:|:-----------------------------------------------------------------------------------:|
|    {link}   | Mostra o link do vídeo/tweet/live de algum canal/conta dos sistemas de notificação. |
|    {game}   |        Mostra qual jogo ou aplicativo está sendo transmitido na live (Twitch)       |

## Níveis por Experiência

|             Placeholder             |                                            Significado                                            |
|:-----------------------------------:|:-------------------------------------------------------------------------------------------------:|
|               {level}               |                        Mostra o nível atual do membro que provocou a ação.                        |
|                 {xp}                |                     Mostra a experiência atual do membro que provocou a ação.                     |
|         {experience.ranking}        |            Posição no ranking de experiência do membro que provocou a ação no servidor.           |
| {experience.next-level.required-xp} |        Mostra a experiência necessária para o próximo nível do membro que provocou a ação.        |
|   {experience.next-level.total-xp}  | Total de experiência necessária para o membro que provocou conseguir evoluir para o próximo nível |
|       {experience.next-level}       |                       Mostra o próximo nível do membro que provocou a ação.                       |
