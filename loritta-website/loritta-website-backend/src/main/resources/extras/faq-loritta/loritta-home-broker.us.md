title: "Loritta's Home Broker"
authors: [ "mrpowergamerbr" ]
---
<div class="centered-text">
<img src="https://i.imgur.com/FrSMGpZ.png" height="300" />
</div>

Loritta's Home Broker (or "Loritta's Broker") is a functionality of Loritta where you can buy and sell shares from the Brazil Stock Exchange (B3) for dreams, simulating real life stock exchange!

## Value of shares on Loritta's Home Broker
The values of the shares on Loritta's Home Broker reflect the values of the shares in real life, with the difference that values in the brokerage are multiplied by 100.

**Example:** If a share is worth 20,17 reais in real life, it will be for 2017 dreams in Loritta's brokerage!

As the values follow the value in real life, if the share goes up in real life, it will also go up in Loritta!

## Loritta's Home Broker Hours
As Loritta's brokerage follows the shares in real life, the functioning hours also follow the Brazil stock exchange of real life!

Brazil's stock exchange works from Monday to Friday, except holidays, from 10:00 until 18:00.

For more information, see the [trading hours in the stock market](http://www.b3.com.br/pt_br/solucoes/plataformas/puma-trading-system/para-participantes-e-traders/horario-de-negociacao/acoes/) and the [trading calendar](http://www.b3.com.br/pt_br/solucoes/plataformas/puma-trading-system/para-participantes-e-traders/calendario-de-negociacao/feriados/)!

## Broker Commands

* The main command of the brokerage is `/broker info`, in it you can see all available shares, their prices and if they are moving. In case the shares are moving, they will be with an ":online:" next to the name and if they are closed, they will be with a ":dnd:";

See the example of the command below, you can pass the mouse cursor over the <span class="tooltip tooltip-glow"><span class="tooltip-text"><img src="https://cdn.discordapp.com/emojis/519546310978830355.png?v=1" width="64" height="64"></span> glowing texts</span> to see more information about!

{{ renderDiscordMessage("broker_command_only_one_ticker.html", "broker_command_only_one_ticker_tooltips.conf", "remove-reply", "remove-message-content") }}

* To buy shares, you can utilize the command `/broker buy`, providing the ticker ID and the quantity. The quantity of shares that you are going to buy is optional, if you don't put a value for `quantity`, Loritta will buy only 1 share for you;

* To sell shares that you bought, you can utilize `/broker sell`, providing the ticker ID and the quantity similar to the buy command;

* To see shares that you bought and see if you are profiting with them (or not), you can use `/broker portfolio`;

Values to remember that the system is not broken, if you win many dreams in `/broker` and other people think it unfair, just explain that is how the market works. :lori_nem_ligo: 
