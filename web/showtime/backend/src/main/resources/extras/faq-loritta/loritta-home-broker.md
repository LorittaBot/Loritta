title: "Loritta's Home Broker"
authors: [ "mrpowergamerbr" ]
---
<div class="centered-text">
<img src="https://i.imgur.com/FrSMGpZ.png" height="300" />
</div>

Loritta's Home Broker (ou "Corretora da Loritta") é uma funcionalidade da Loritta aonde você pode comprar e vender ações da Bolsa de Valores do Brasil (B3) por sonhos, simulando a bolsa de valores da vida real!

## Valor das ações na Loritta's Home Broker
Os valores das ações na Loritta's Home Broker refletem os valores das ações na vida real, com a diferença que os valores na corretora são multiplicados por 100.

**Exemplo:** Se uma ação vale 20,17 reais na vida real, ela estará por 2017 sonhos na corretora da Loritta!

Como os valores seguem o valor na vida real, se a ação subir na vida real, ela também irá subir na Loritta!

## Horário da Loritta's Home Broker
Como a corretora da Loritta segue as ações na vida real, o horário de funcionamento também segue a bolsa de valores do Brasil da vida real!

A bolsa de valores do Brasil funciona de segunda a sexta, exceto feriados, das 10:00 até as 18:00.

Para mais informações, veja os [horários de negociação no mercado de ações](http://www.b3.com.br/pt_br/solucoes/plataformas/puma-trading-system/para-participantes-e-traders/horario-de-negociacao/acoes/) e o [calendário de negociação](http://www.b3.com.br/pt_br/solucoes/plataformas/puma-trading-system/para-participantes-e-traders/calendario-de-negociacao/feriados/)!

## Comandos da Corretora

* O comando principal da corretora é o `/broker info`, nele você pode ver todas as ações disponíveis, seus preços e se elas estão movimentando. Caso as ações estejam se movimentando, elas estarão com um ":online:" ao lado do nome e se estiverem fechadas, estarão com um ":dnd:";

Veja o exemplo do comando a seguir, você pode passar o cursor do mouse em cima dos <span class="tooltip tooltip-glow"><span class="tooltip-text"><img src="https://cdn.discordapp.com/emojis/519546310978830355.png?v=1" width="64" height="64"></span> textos brilhantes</span> para ver mais informações sobre!

{{ renderDiscordMessage("broker_command_only_one_ticker.html", "broker_command_only_one_ticker_tooltips.conf", "remove-reply", "remove-message-content") }}

* Para comprar as ações, você pode utilizar o comando `/broker buy`, fornecendo o ID do ticker e a quantidade. A quantidade de ações que você vai comprar é opcional, se você não colocar um valor para `quantity`, a Loritta irá comprar apenas 1 ação para você;

* Para vender as ações que você comprou, você pode utilizar `/broker sell`, fornecendo o ID do ticker e a quantidade semelhante ao comando de compra;

* Para ver as ações que você comprou e ver se está lucrando com elas (ou não), você pode usar `/broker portfolio`;

Vale lembrar que o sistema não é quebrado, se você ganhar muitos sonhos no `/broker` e outras pessoas acharem injusto, apenas explique que é assim que o mercado funciona. :lori_nem_ligo: 
