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

## Comando da Corretora

O comando principal da corretora é a `+corretora`, nele você pode ver se a bolsa está aberta e ver os preços das ações.

Veja o exemplo do comando a seguir, você pode passar o cursor do mouse em cima dos <span class="tooltip tooltip-glow"><span class="tooltip-text"><img src="https://cdn.discordapp.com/emojis/519546310978830355.png?v=1" width="64" height="64"></span> textos brilhantes</span> para ver mais informações sobre!

{{ renderDiscordMessage("broker_command_only_one_ticker.html", "broker_command_only_one_ticker_tooltips.conf", "remove-reply", "remove-message-content") }}

Você pode ver os valores das ações e se a bolsa está aberta utilizando `+broker`, caso ela esteja com <img src="https://cdn.discordapp.com/emojis/639993552113500172.png?v=1" class="inline-emoji" /> 

* :lori_rica: Se você usar apenas +broker a Loritta irá mostrar todas as ações disponíveis e se elas estão se movimentando, caso as ações estejam se 
movimentando elas estarão com um ":online:" antes do nome. Ou se elas estiverem fechadas irá aparecer isto ":dnd:";

* :lori_ameno: Já que a Loritta acompanha a bolsa da vida real, as ações só se movimentam em dias úteis (seg à sex);

* :lori_thinking~1: Para comprar as ações você pode utilizar +broker comprar <TICKER_DA_AÇÃO> [quantidade];

* :lori_shrug1: A quantidade de ações que você vai comprar é opcional, se você não colocar uma "quantidade" vai fazer a Loritta comprar apenas apenas 1 ação;

* :lori_barf: Para vender as ações que você comprou você pode utilizar o +broker vender <TICKER_DA_AÇÃO> [quantidade];

* :bug: Para ver as ações que você comprou e ver se você lucrou com elas ou não você pode usar +broker p/portfólio/portfolio;

* :alarm_clock: As ações começam a se movimentar das 10:00 da manhã até as 18:00 da noite;

* :analise: E outra, vale lembrar que o sistema não é quebrado, se você ganhar muitos sonhos no +corretora e outras pessoas acharem injusto apenas explique que é assim que o mercado funciona.
