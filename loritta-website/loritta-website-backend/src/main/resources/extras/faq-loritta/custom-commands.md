title: "Comandos Personalizados"
authors: [ "arth", "peterstark000" ]
---
<div class="centered-text">
<img src="/v3/assets/img/faq/custom_commands/banner.png" height="300" />
</div>

Sabia que você pode criar comandos para o seu servidor? No momento, apenas comandos que enviam uma mensagem prontas são possíveis, mas ainda assim você pode criar várias coisas com isso! Siga os passos a seguir para saber como criar um e saber mais sobre eles. <img src="https://cdn.discordapp.com/emojis/673868465433477126.png?v=1" class="inline-emoji">

## Como criar um comando personalizado?
1. Acesse o painel de controle do seu servidor [clicando aqui](/dashboard) ou usando o comando `+painel` no seu servidor.
![1° passo](/v3/assets/img/faq/custom_commands/1.png)

2. Ao acessar o painel, procure pelo módulo Comandos Personalizados na lista à sua direita.
![2.1° passo](/v3/assets/img/faq/custom_commands/2.1.png)
![2.2° passo](/v3/assets/img/faq/custom_commands/2.2.png)

3. Agora que você já está no módulo, basta clicar em `Adicionar` e selecionar `Comando de Texto` para começar suas criações!
![3° passo](/v3/assets/img/faq/custom_commands/3.png)

4. Abrirá um pop-up com as informações para você preencher ao fazer isso. Então aí que começa a mágica! Primeiro você coloca o nome do comando que você quer, e como diz no aviso, não coloque o meu prefixo no nome do comando (prefixo é o caractere que você configura para colocar antes de usar qualquer comando, como o `+`, o meu prefixo padrão). Depois, na caixa de texto embaixo, você preenche o que quiser que eu fale toda vez que alguém usar um comando!
![4° passo](/v3/assets/img/faq/custom_commands/4.png)

5. Depois de preenchido, clique em `Salvar` no pop-up e no botão verde da página. Prontinho!  <img src="https://cdn.discordapp.com/emojis/519546310978830355.png?v=1" class="inline-emoji"> Agora eu irei responder os membros do seu servidor quando usarem um comando com o nome que você colocou.
![5.1° passo](/v3/assets/img/faq/custom_commands/5.1.png)
![5.2° passo](/v3/assets/img/faq/custom_commands/5.2.png)

**Atenção**: Colocar qualquer coisa NSFW nos comandos personalizados como em qualquer outra seção do painel resultará no seu ban permanente do uso das minhas funções! <img src="https://cdn.discordapp.com/emojis/395010059157110785.png?v=1" class="inline-emoji"> Não se engane, nós sabemos quando você faz isso. [Clique aqui](/guidelines) para ler as minhas guidelines.

## Ideias para você fazer
Você pode fazer várias coisas, como colocar para que eu envie o link do seu canal quando usarem o comando, assim evitando pessoas perguntando qual é o seu canal e você tendo que ir pegar ele toda hora... Isso é chato! E não precisa ser apenas link do seu canal, você pode colocar o que quiser.

Quer fazer algo mais complexo? Também pode! Use a opção de [Editor Avançado](https://embed.loritta.website/) para configurar uma [embed](/extras/faq-loritta/embeds) para você ou então use [placeholders](/extras/faq-loritta/placeholders), ou... combine os dois! No [meu servidor de comunidade](https://discord.gg/lori) eu tenho um comando personalizado que combinei as duas coisas. Se você usar `+level` em um dos canais de comandos que temos lá, eu te enviarei informações sobre o seu XP no servidor. Se quiser o código da embed deste comando, [clique aqui](https://gist.github.com/MrPowerGamerBR/0d85d998e9ef656e7a6ab8b04f029380)!

{{ renderDiscordMessage("level_card.html", "no_tooltips.conf") }}

## Notas
* No momento ainda não é possível fazer comandos que interajam com usuários, como os comandos de ação (`+kiss @user`, `+hug @user`, etc).

* No momento ainda não é possível fazer com que eu envie arquivos, como imagens, nos comandos personalizados. Se quiser colocar uma imagem no seu comando, basta colocar o link, mas se quiser deixar o link escondido, deixe dentro de uma [embed](/extras/faq-loritta/embeds). Recomendo que use meu [Editor Avançado](https://embed.loritta.website/).

* No passado meus comandos personalizados eram feitos em JavaScript, logo você conseguia fazer MUITAS coisas usando eles. Só que devido a um bug muito grave que podia ter explodido todos os servidores (descoberto pelos desenvolvedores da Mantaro <img src="https://cdn.discordapp.com/emojis/732706868224327702.png?v=1" class="inline-emoji">) eu tive que remover esta função do meu painel... <img src="https://cdn.discordapp.com/emojis/626942886251855872.png?v=1" class="inline-emoji"> Talvez em um futuro não tão distante você consiga fazer comandos personalizados só que dessa vez em Kotlin ou então numa linguagem minha de programação.
