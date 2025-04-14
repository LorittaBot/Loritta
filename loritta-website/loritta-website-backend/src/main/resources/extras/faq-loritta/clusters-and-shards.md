title: "Clusters e shards"
authors: [ "arth", "mrpowergamerbr" ]
---
Sharding é separar várias instâncias do Discord, para que várias instâncias possam controlar servidores diferentes.

Por padrão o Discord faz com que todos os bots que estão em mais de 2500 servidores precisem usar shards. Cada shard pode controlar, no máximo, 2500 servidores (se você tem um bot e ele não faz sharding, ele não poderá ser adicionado em mais de 2500 servidores).

Uma shard seria como se você abrisse o Discord novamente, só que cada novo "Discord aberto" possuísem servidores diferentes.

O Sharding é usado para diminuir o "peso" de um bot nos servidores do Discord, e também é vantajoso para bots, já que cada shard tem um rate limit diferente, causando menos problemas relacionados a isso.

Clusters são um conjunto de shards da Loritta. Cada cluster é uma aplicação que roda várias shards, assim caso precise atualizar a Loritta ou caso dê algum problema no servidor, só irá afetar aquelas shards que estão no Cluster, em vez de afetar todos os servidores que a Loritta está.

Você pode ver todas elas usando o comando `+ping clusters` e, se você está curioso para saber qual é o cluster do seu servidor, use `+ping` ou `+serverinfo` para saber!

Não é possível escolher qual cluster ou shard é a do seu servidor, já que cada cluster tem shards automaticamente alocadas a ela. E todos os clusters são iguais, então o único motivo para você querer trocar de cluster seria se você gosta mais do nome de outro cluster.

## Coisas que os Clusters influenciam no meu funcionamento

* [Bom Dia & Cia](/extras/faq-loritta/bomdiaecia).
* Comandos e mensagens que envolvem emoji.
* Instabilidades (eu posso ficar instável em um servidor e estar funcionando normalmente em outro devido ao sistema de clusters).