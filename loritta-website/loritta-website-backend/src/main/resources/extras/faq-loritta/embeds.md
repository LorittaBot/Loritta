title: "Embeds"
authors: [ "peterstark000", "mrpowergamerbr" ]
---
<div class="centered-text">
<img src="/v3/assets/img/faq/embeds/banner.png" height="300" />
</div>

Embeds são um tipo especial de mensagem no Discord, você provavelmente já viu elas antes... aquelas caixinhas que possuem uma corzinha na esquerda e que ainda por cima podem ter imagens, autor, footer e muito mais! Sim, elas são bonitas, e é claro que a Loritta suporta elas também!

{{ renderDiscordMessage("embeds_embed_example.html", "no_tooltips.conf", "remove-reply", "remove-message-content") }}

Na Loritta, embeds são suportadas em todos os lugares que aceitam uma mensagem no painel, ao colocar uma embed no local da mensagem, ela irá mostrar a embed na preview e irá mostrar que está no modo "Extended Code".

Embeds também são suportados em alguns comandos como o `+say`, basta colocar o código JSON em frente do nome do comando. Desde modo, é possível enviar essas mensagens bonitinhas em canais. (Observação: Placeholders também funcionam no say e em alguns outros comandos. Se você ainda não sabe o que são placeholders, veja [clicando aqui](/extras/faq-loritta/placeholders)!)

Embeds são formatadas em JSON, baseado no jeito que o Discord envia embeds.

## Dicas
* [Placeholders](/extras/faq-loritta/placeholders) são suportados dentro de embeds!
* Não está conseguindo ver embeds? Então quer dizer que você desativou a pré-visualização de links nas configurações de "Texto & Imagem" do seu Discord!
* Menções dentro de embeds podem não funcionar corretamente (usuários mencionados dentro de uma embed não receberão notificações e, caso seja um usuário que o seu Discord não tenha carregado, irá aparecer `<@IdDoUsuario>`), caso isto aconteça, adicione a menção do usuário no content também!
* Embeds no Discord Mobile (iOS/Android) podem não funcionar igual como elas funcionam no Discord de computador/web!
* As cores utilizadas nos embeds estão em formato decimal, para pegar a cor em formato decimal, utilize `+colorinfo`!

## Editor de Embeds da Loritta

O website da Loritta tem o seu próprio editor de embeds, isto facilita muito tanto para vocês e para nós, já que reaproveitamos a renderização de mensagens do website do Embed Editor para o website da Loritta. Se você não conhecer o editor de embeds ainda, basta [clicar aqui](https://embeds.loritta.website/) para ser redirecionado para ele.

<div class="centered-text">
<img src="/v3/assets/img/faq/embeds/embed_editor.png" height="300" />
</div>

*Print demonstrativa do Editor de Embeds, para começar a edição clique em "Adicionar embed".*

Você pode editar textos/cores/etc clicando na seção que você quer editar, por exemplo: Se você quer alterar a cor de embed, é só clicar na lateral aonde fica a cor. Se você quer alterar o título da embed, só clicar no título.

O embed editor está integrado com o website da Loritta! Para abrir uma mensagem no Embed Editor, basta clicar no botão de "Editor Avançado" que você será levado para o embed editor.

## Templates

Para você ver como é feito os embeds, deixamos alguns embeds prontos para você ver, usar e se divertir! Para utilizar eles, copie o código, coloque no lugar de texto no painel da Loritta aonde você quer usar o embed e veja como ele ficou!

### Mensagem de Entrada
```json
{
   "content":"{@user}",
   "embed":{
      "color":-9270822,
      "title":"👋 Bem-vindo(a)!",
      "description":"Olá {@user}, espero que você se divirta no meu servidor! <:loritta:331179879582269451>",
      "author":{
         "name":"{user.tag}",
         "icon_url":"{user.avatar}"
      },
      "thumbnail":{
         "url":"{user.url}"
      },
    "footer": {
      "text": "ID do usuário: {user.id}"
    }
   }
}
```

### Mensagem de Saída
```json
{
   "content":"",
   "embed":{
      "color":-6250077,
      "title":"😭 #chateada!",
      "description":"⚰ **{user}** saiu do servidor... <:lori_triste:370344565967814659>",
      "author":{
         "name":"{user.tag}",
         "icon_url":"{user.avatar}"
      },
      "thumbnail":{
         "url":"{user.avatar}"
      },
    "footer": {
      "text": "ID do usuário: {user.id}"
    }
   }
}
```

### Mensagem de Entrada (via mensagem direta)
```json
{
   "content":" ",
   "embed":{
      "color":-16727585,
      "title":"👋 Bem-Vindo(a) ao {guild}!",
      "description":"Obrigado por entrar no meu servidor e divirta-se! Ah, e é claro, não se esqueça de seguir as regras do servidor! <:loritta:331179879582269451>",
      "thumbnail":{
         "url":"https://loritta.website/assets/img/loritta_star.png"
      },
     "image":{ "url":"https://loritta.website/assets/img/fanarts/Loritta_Headset_-_N3GR3SC0.png"}
   }
}
```

### Mensagem quando alguém é punido (Moderação)
```json
{
   "content":"",
   "embed":{
      "color":-9270822,
      "title":"{user.tag} | {punishment}",
      "thumbnail": { "url" : "{staff-avatar-url}" },
      "description":"O usuário foi punido por quebrar as regras do {guild}, fazer o que né, quebrou as #regras levou punição! <:tavares:412577570190655489> :BlobAxolotlPride: <:smol_lori_putassa_aline:395010059157110785>",
      "fields": [
        {
            "name": "Tag do Usuário",
            "value": "`{user.tag}`",
            "inline": true
        },
        {
            "name": "ID do Usuário",
            "value": "`{user.id}`",
            "inline": true
        },
        {
            "name": "Quem puniu",
            "value": "`{staff}#{staff-discriminator}`",
            "inline": true
        },
        {
            "name": "Motivo",
            "value": "{reason}",
            "inline": true
        }
      ],
    "footer": {
      "text": "ID do usuário: {user-id}"
    }
   }
}
```

### Mensagem de Level Up
```json
{
  "content": "{@user}",
  "embed": {
    "description": "• Parabéns, **{user}**! Você passou para o nível **{level}** (*{xp} XP*) e agora está em #{experience.ranking} lugar no ranking de experiência do servidor! <a:lori_yay_wobbly:638040459721310238>",
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

### Mensagem de Card de Experiência

* Este em especial é utilizado em um [Comando Personalizado](/extras/faq-loritta/custom-commands). Clique nas letras em azul para ver o nosso FAQ sobre!
```json
{
  "content": "{@user}",
  "embed": {
    "title": "<:lori_kamehameha_1:727280767893504022> **| Profile Card de `{user.tag}`**",
    "description": "\n<:lori_barf:727583763646644225> **| Nível atual:** `{level}`\n<:lori_water:728761705148186726> **| XP Atual:** `{xp}`\n<:lori_point:731876009548316712> **| Colocação:** `#{experience.ranking}`\n<:lori_stonks:788434890927505448> **| XP necessário para o próximo nível ({experience.next-level}):** `{experience.next-level.required-xp}`\n> <:lori_nice:726845783344939028> • **Dica da Lorota Jubinha:** continue conversando para passar de nível. Eu sei que você vai conseguir!\n",
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
