title: "Moderação"
authors: [ "nightdavisao" ]
---
A Loritta tem um **módulo de moderação** no Dashboard.  
Configurando ele, você pode definir um canal para anunciar punições feitas com os comandos.  
Note que, você precisa usar os comandos de moderação para que a Loritta anuncie as punições no seu servidor.  
Para acessar o módulo, use o comando `painel` no seu servidor, e em seguida, clique em "Moderação".  

## Comandos essenciais para o seu servidor

#### Existe comandos feitos para punições, esses são:
* `ban`: Bane membros do seu servidor, esse comando usa o banimento do Discord, ou seja, os usuários são banidos por IP.
* `mute`: Silencia membros em seu servidor. Será perguntado qual a duração do silenciamento depois que você usar o comando. Note que você não deve colocar a duração ao usar o comando e sim depois de executar o comando. 
* `warn`: Dá um aviso em membros em seu servidor. No painel do seu servidor, em Moderação, você pode ajustar punições diferentes a cada avisos feitos com o comando.
* `kick`: Expulsa membros do seu servidor.

Observe que nos comandos de punição, você pode inserir vários usuários para punir.

#### Também existe comandos para remover punições, esses são:
* `unban`: Desbane membros do seu servidor, nesse comando, você precisa inserir o ID do usuário.
* `unwarn`: Remove um aviso de um membro.
* `unmute`: Remove silenciamento de membros.

#### Por último, os comandos que não são para punir ou remover punições:
* `baninfo`: Examine um banimento de um membro do seu servidor.
* `warnlist`: Examine avisos feitos com o `warn` em um membro do seu servidor.
* `lock`: Bloqueie membros de enviar mensagens em um canal. Ao usar o comando sem especificar um canal, o canal qual o comando foi executado será bloqueado.
* `unlock`: Desbloqueie membros de enviar mensagens em um canal. Ao usar o comando sem especificar um canal, o canal qual o comando foi executado será desbloqueado.

## Os comandos `lock` ou `mute` não funcionam corretamente no seu servidor?
* Várias coisas podem fazer o `lock` e o `mute` não funcionarem. Se eu responder alguma coisa quando você tentar o comando, leia a minha mensagem com a atenção pois lá pode estar a resposta.
* Eu disse que bloqueei o chat/silenciei a pessoa mas o(s) membro(s) ainda continuam conversando? Esta pessoa pode estar com um cargo que possui a permissão de Administrador no servidor. Administradores conseguem falar nos chats mesmo com eles bloqueados ou mesmo com o cargo de silenciado.

#### Estas pessoas não são administradoras? Então pode ser algum problema nas permissões do canal.
* Quando o comando `lock` é executado, a permissão "Enviar Mensagens" do cargo `@everyone` é **negada**.
* E quando o comando `mute` é usado, é definido um cargo chamado "Silenciado" em que a permissão de "Enviar Mensagens" em canais de texto é **negada**.
* Se a pessoa estiver com outro cargo acima do cargo usado pelo o tal comando, e esse cargo tem a tal permissão concedida, essa negação de permissão é anulada.
* Deixe a permissão desse cargo no nulo, clicando na barra que está no meio. Edite as permissões dos canais acessando **"Editar Canal"** com o botão direito do mouse ou segurando o touch no canal.