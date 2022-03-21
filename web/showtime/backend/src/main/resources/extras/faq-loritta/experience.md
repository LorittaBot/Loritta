title: "Níveis por Experiência"
authors: [ "arth", "peterstark000" ]
---
<div class="centered-text">
<img src="/v3/assets/img/faq/experience/banner.png" height="300" />
</div>

Experiência você ganha conversando nos canais de texto do seu servidor. Você pode ver o seu XP usando o comando `+perfil`. Existe o XP local (XP dentro de apenas um servidor) e o XP global (XP em todos os servers que eu estou). Cada nível de experiência são 1.000 XP.

Você pode ver o rank de experiência do seu servidor usando `+rank` e o rank global usando `+rank global`.

Nesta seção você verá as regras que eu sigo para dar XP em uma mensagem, o módulo de Níveis por Experiência no painel de controle, coisas que você pode configurar no painel de controle em relação ao XP no seu servidor e como fazer um comando de cartão de perfil para o seu servidor. <img src="https://cdn.discordapp.com/emojis/731873534036541500.png?v=1" class="inline-emoji">

## Regras que eu sigo para dar XP
Eu sou um pouco exigente para recompensar o XP de uma mensagem! <img src="https://cdn.discordapp.com/emojis/729723959284727808.png?v=1" class="inline-emoji"> Para eu recompensar o XP:

* A mensagem tem que ter mais de 5 caracteres.
* A mensagem tem que ser diferente da última mensagem enviada.
* A mensagem tem que ser enviada de um jeito que seja humanamente possível da pessoa ter escrito tudo aquilo. Se, por exemplo, a última mensagem do usuário foi enviada a 5 segundos atrás e a pessoa enviar um texto gigante, eu ignoro (eu calculo a quantidade de caracteres na mensagem / 7, se a diferença entre a última mensagem e o cálculo for maior, eu deixo).
* A mensagem, SEM CARACTERES REPETIDOS (por exemplo, kkkkkkkkkk vira apenas k) precisa ter mais de 12 caracteres.

Se tudo der certo, o XP da mensagem é a quantidade de caracteres não repetidos / 7 e quantidade de caracteres não repetidos / 4, se o XP ultrapassar 35, eu deixo 35.

## Configurando os Níveis por Experiência
1. Acesse o painel de controle do seu servidor [clicando aqui](/dashboard) ou usando o comando `+painel` no seu servidor.
![1° passo](/v3/assets/img/faq/servers.png)

2. Ao acessar o painel, você começará pelo módulo Configurações Gerais. Procure então pelo módulo Níveis por Experiência na lista à sua esquerda.
![2° passo](/v3/assets/img/faq/experience/find_module.png)

3. Agora que você está no módulo dos níveis por experiência, você pode configurar várias coisas relacionadas ao XP dos membros do seu servidor! 
![3° passo](/v3/assets/img/faq/experience/configuration.png)

4. Após configurar, desça a página e procure por um botão verde escrito `Salvar`. É muito importante que você salve as configurações feitas quando você mexe no painel de controle. Prontinho! <img src="https://cdn.discordapp.com/emojis/519546310978830355.png?v=1" class="inline-emoji"> Eu irei seguir o que você pediu em questão de XP agora.
![4° passo](/v3/assets/img/faq/experience/save.png)

**Atenção**: Usuários normais conseguem colocar 15 cargos por experiência no servidor deles. Já <img src="https://cdn.discordapp.com/emojis/593979718919913474.png?v=1" class="inline-emoji"> usuários premium <img src="https://cdn.discordapp.com/emojis/593979718919913474.png?v=1" class="inline-emoji">, ativando uma [key premium](/extras/faq-loritta/premium-keys) no servidor, conseguem colocar de 30 a 100 cargos! Whoosh! [Clique aqui](/donate) para tornar-se um usuário premium.

### No painel de controle você pode:
* Configurar mensagens para que eu envie quando alguém subir de nível
* Estabelecer cargos para as pessoas receberem quando atingirem certo nível
* Configurar que cargos específicos ganhem mais/menos experiência que outros membros
* Configurar que cargos específicos não ganhem experiência
* Configurar canais de texto para não darem experiência

## Profile Card (`+level`)
{{ renderDiscordMessage("level_card.html", "no_tooltips.conf") }}

O comando `+level` é um comando disponível apenas no meu [servidor de comunidade](https://discord.gg/lori), mas você também pode adicioná-lo no seu servidor! [Clique aqui](https://gist.github.com/MrPowerGamerBR/0d85d998e9ef656e7a6ab8b04f029380) se quiser ver o código da [embed](/extras/faq-loritta/embeds) do comando. Quer saber mais sobre comandos personalizados? [Clique aqui](/extras/faq-loritta/custom-commands)!

