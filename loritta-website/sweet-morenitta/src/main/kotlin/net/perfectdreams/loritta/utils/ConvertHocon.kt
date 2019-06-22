package net.perfectdreams.loritta.utils

import com.fasterxml.jackson.module.kotlin.readValue
import com.github.salomonbrys.kotson.*
import com.typesafe.config.ConfigRenderOptions
import net.perfectdreams.loritta.utils.config.FanArt
import net.perfectdreams.loritta.utils.config.FanArtArtist
import net.perfectdreams.loritta.website.utils.jsonParser
import java.io.File
import java.nio.file.Files
import java.nio.file.attribute.BasicFileAttributes
import java.time.LocalDate
import java.time.ZoneId


fun main() {
    val str = """{
  "artists": {
    "203199927986159616": {
      "discord-id": "203199927986159616",
      "social": [

      ]
    },
    "162571626477518848": {
      "discord-id": "162571626477518848",
      "social": [
        {
          "social-network": "DEVIANTART",
          "display": "Heatheclif",
          "link": "https://www.deviantart.com/heatheclif"
        },
        {
          "social-network": "AMINO",
          "display": "Heathecliff",
          "link": "http://aminoapps.com/p/1kybbd"
        }
      ]
    },
    "88120564400553984": {
      "discord-id": "88120564400553984",
      "social": [
        {
          "social-network": "WEBSITE",
          "display": "Pollux",
          "link": "https://pollux.fun"
        }
      ]
    },
    "379360433641095169": {
      "discord-id": "379360433641095169",
      "social": [
        {
          "social-network": "DEVIANTART",
          "display": "IsaFlokHyung",
          "link": "https://deviantart.com/isaflokhyung"
        }
      ]
    },
    "304292804216225793": {
      "discord-id": "304292804216225793",
      "social": [
        {
          "social-network": "TWITTER",
          "display": "@rickinho3kkkkkk",
          "link": "https://twitter.com/rickinho3kkkkkk"
        },
        {
          "social-network": "STEAM",
          "display": "Rickinho3",
          "link": "https://steamcommunity.com/profiles/76561198805862810/"
        },
        {
          "social-network": "YOUTUBE",
          "display": "Rickinho3",
          "link": "https://www.youtube.com/channel/UCkRtyhlfPbqfWE6CKfpHOJg"
        }
      ]
    },
    "320338866940936194": {
      "discord-id": "320338866940936194",
      "social": [
        {
          "social-network": "TWITTER",
          "display": "@Haaataoh",
          "link": "https://twitter.com/Haaataoh"
        }
      ]
    },
    "512397127427686400": {
      "discord-id": "512397127427686400",
      "social": [
        {
          "social-network": "TWITTER",
          "display": "@atumalaca",
          "link": "https://twitter.com/atumalaca"
        }
      ]
    },
    "366297726293508096": {
      "discord-id": "366297726293508096",
      "social": [
        {
          "social-network": "YOUTUBE",
          "display": "ღEzel Furryღ",
          "link": "https://www.youtube.com/channel/UC5nRaG4ZX8_bE7CYzSO4WLA"
        }
      ]
    },
    "261337059069788163": {
      "discord-id": "261337059069788163",
      "social": [
        {
          "social-network": "TWITTER",
          "display": "@negresconfusoo",
          "link": "https://twitter.com/negresconfusoo"
        }
      ]
    },
    "0": {
      "social": [
        {
          "social-network": "DEVIANTART",
          "display": "July97Chan",
          "link": "https://deviantart.com/july97chan"
        }
      ]
    },
    "1": {
      "social": [
        {
          "social-network": "TWITTER",
          "display": "Minibyte2",
          "link": "https://twitter.com/Minibyte2"
        }
      ]
    },
    "2": {
      "social": [
        {
          "social-network": "TWITTER",
          "display": "NyahKun",
          "link": "https://twitter.com/NyahKun"
        }
      ]
    },
    "288417511286898689": {
      "discord-id": "288417511286898689",
      "social": [
        {
          "social-network": "TWITTER",
          "display": "@myahcontre",
          "link": "https://twitter.com/myahcontre"
        }
      ]
    },
    "385479127656038412": {
      "discord-id": "385479127656038412",
      "social": [
        {
          "social-network": "TWITTER",
          "display": "@Anenha_L",
          "link": "https://twitter.com/Anenha_L"
        }
      ]
    },
    "402519250578964481": {
      "discord-id": "402519250578964481",
      "social": [
        {
          "social-network": "TWITTER",
          "display": "@KanekiArtz_",
          "link": "https://twitter.com/KanekiArtz_"
        },
        {
          "social-network": "TWITTER",
          "display": "@Randomneki",
          "link": "https://twitter.com/Randomneki"
        }
      ]
    }
  },
  "fan-arts": [
    {
      "artistId": "203199927986159616",
      "fileName": "Loritta_-_Gabriela.png"
    },
    {
      "artistId": "197500441079054336",
      "fileName": "Loritta_-_Poker.png"
    },
    {
      "artistId": "232279251972259840",
      "fileName": "Loritta_-_Shadow_Kisha-tu.png"
    },
    {
      "artistId": "277503267939024896",
      "fileName": "Loritta_-_WaterFox_.png"
    },
    {
      "artistId": "249292429436387328",
      "fileName": "Loritta_-_pitssaguey.png"
    },
    {
      "artistId": "325099700569112576",
      "fileName": "Loritta_-_ERKS.png"
    },
    {
      "artistId": "255109335951081472",
      "fileName": "Loritta_-_Mari_Kobayashi.png"
    },
    {
      "artistId": "261337059069788163",
      "fileName": "Loritta_-_N3GR3SC0.png"
    },
    {
      "artistId": "261337059069788163",
      "fileName": "Loritta_Headset_-_N3GR3SC0.png"
    },
    {
      "artistId": "271793320634744832",
      "fileName": "Loritta_-_Aline.png"
    },
    {
      "artistId": "168123859898204161",
      "fancyName": "Mikaru",
      "fileName": "Loritta_-_Deusa_Doce.png",
      "additionalInfo": "<b>YouTube:</b> <a href=\"https://www.youtube.com/user/deusadocecartoon\">Link</a></br>Twitter: <a href=\"https://twitter.com/mik4ru\">Link</a>"
    },
    {
      "artistId": "224299319396663296",
      "fileName": "Loritta_-_Ayano.png"
    },
    {
      "artistId": "347421169621794821",
      "fileName": "Loritta_-_CupHead_Girl.png"
    },
    {
      "artistId": "267742777620692992",
      "fileName": "Loritta_-_Katori.png"
    },
    {
      "artistId": "255109335951081472",
      "fileName": "Loritta_2_-_Mari_Kobayashi.png"
    },
    {
      "artistId": "262709817934610435",
      "fileName": "Loritta_-_Land.png"
    },
    {
      "artistId": "264577834213834752",
      "fileName": "Loritta_-_ThirdOPPAI.png"
    },
    {
      "artistId": "351009396047740928",
      "fileName": "Loritta_Natal_-_Swag.png"
    },
    {
      "artistId": "229560030813224961",
      "fileName": "Loritta_-_Tio_Max.png"
    },
    {
      "artistId": "162716114957107201",
      "fileName": "Loritta_2_-_Cormano.png"
    },
    {
      "artistId": "325099700569112576",
      "fileName": "Loritta_2_-_ERKS.png"
    },
    {
      "artistId": "367893062812434432",
      "fileName": "Loritta_2_-_StarlineBR.png"
    },
    {
      "artistId": "336631633685774347",
      "fileName": "Loritta_-_Taiga.png"
    },
    {
      "artistId": "248237759456608256",
      "fileName": "Loritta_-_Brockolis.png"
    },
    {
      "artistId": "341615273071214598",
      "fileName": "Loritta_-_Mip.png"
    },
    {
      "artistId": "325440390297550852",
      "fileName": "Loritta_-_Kiuukin.png"
    },
    {
      "artistId": "396633588986019848",
      "fileName": "Loritta_-_Nescau.png"
    },
    {
      "artistId": "248236658359533569",
      "fileName": "Loritta_-_Link_Princesa_Fundubs.png"
    },
    {
      "artistId": "361531707180187650",
      "fileName": "Loritta_-_portugalense.png"
    },
    {
      "artistId": "317089777998495765",
      "fileName": "Loritta_-_Bluelerry2.png"
    },
    {
      "artistId": "203199927986159616",
      "fileName": "Loritta_Festa_Junina_-_Gabriela.png"
    },
    {
      "artistId": "295241093241503744",
      "fileName": "Loritta_-_Alone_Friend.png"
    },
    {
      "artistId": "365319185498243074",
      "fileName": "Loritta_-_Estevao.png"
    },
    {
      "artistId": "398286046309122049",
      "fileName": "Loritta_-_Jessie.png"
    },
    {
      "artistId": "273957582224228353",
      "fileName": "Loritta_-_MarkinhosMLP.png"
    },
    {
      "artistId": "274643892752613376",
      "fileName": "Loritta_-_Zabuza.png"
    },
    {
      "artistId": "284121713749524480",
      "fileName": "Loritta_-_Silvacrest.png"
    },
    {
      "artistId": "334346778650345493",
      "fileName": "Loritta_-_ItzCratts.png"
    },
    {
      "artistId": "290837622946004992",
      "fileName": "Loritta_-_Zyna.png"
    },
    {
      "artistId": "281623942467158019",
      "fileName": "Loritta_-_toy_bonni.png"
    },
    {
      "artistId": "406106482036310026",
      "fileName": "Loritta_-_Lydia.png"
    },
    {
      "artistId": "514961762148155398",
      "fileName": "Loritta_-_Amanda.png"
    },
    {
      "artistId": "170125660801597440",
      "fileName": "Loritta_-_Krul.png"
    },
    {
      "artistId": "420692526182432788",
      "fileName": "Loritta_-_cellphone_kawaii.png"
    },
    {
      "artistId": "409111367543554068",
      "fileName": "Loritta_-_FofuraCanina.png"
    },
    {
      "artistId": "289159849315663872",
      "fileName": "Loritta_-_vipernegorgon.png"
    },
    {
      "artistId": "390976882998509569",
      "fileName": "Loritta_-_York.png"
    },
    {
      "artistId": "301870694822707204",
      "fileName": "Loritta_-_NicoBros.png"
    },
    {
      "artistId": "340500843801608193",
      "fileName": "Loritta_-_Tokyo.png"
    },
    {
      "artistId": "423870140115845120",
      "fileName": "Loritta_-_Arthur2809.png"
    },
    {
      "artistId": "372470709961621520",
      "fileName": "Loritta_-_Mandy.png"
    },
    {
      "artistId": "343897778709200898",
      "fileName": "Loritta_-_San_Loves_Vini.png"
    },
    {
      "artistId": "197308318119755776",
      "fileName": "Loritta_-_JvGm45.png"
    },
    {
      "artistId": "390976882998509569",
      "fileName": "Loritta2_-_York.png"
    },
    {
      "artistId": "286132898824847372",
      "fileName": "Loritta_-_Eco.png"
    },
    {
      "artistId": "396633588986019848",
      "fileName": "Loritta_Headset_-_Nescau.png"
    },
    {
      "artistId": "359507149635846145",
      "fileName": "Loritta_-_Rafaella.png"
    },
    {
      "artistId": "390976882998509569",
      "fileName": "Loritta3_-_York.png"
    },
    {
      "artistId": "340500843801608193",
      "fileName": "Loritta2_-_Tokyo.png"
    },
    {
      "artistId": "310455729691557889",
      "fileName": "Loritta_-_Ano.png"
    },
    {
      "artistId": "392641259254317066",
      "fileName": "Loritta_-_AFurryBoio.png"
    },
    {
      "artistId": "400115809130774528",
      "fileName": "Loritta_-_Ryu.png"
    },
    {
      "artistId": "152107139601661962",
      "fileName": "Loritta_-_Gak.png"
    },
    {
      "artistId": "379360433641095169",
      "fileName": "Loritta_-_Isa_Flok.png"
    },
    {
      "artistId": "417703545559711748",
      "fileName": "Loritta_-_RainbowGirlOficial.png"
    },
    {
      "fancyName": "LunaCriCri",
      "fileName": "Loritta_-_LunaCriCri.png",
      "additionalInfo": "<b>Amino:</b> <a href=\"http://aminoapps.com/p/jycacw\">Link</a>"
    },
    {
      "artistId": "462700466204180490",
      "fileName": "Loritta_-_Batatinha.png",
      "additionalInfo": "<b>Amino:</b> <a href=\"http://aminoapps.com/p/wkjlr2\">Link</a>"
    },
    {
      "fancyName": "Alice",
      "fileName": "Loritta_-_Alice.png",
      "additionalInfo": "<b>Amino:</b> <a href=\"http://aminoapps.com/p/38las4\">Link</a>"
    },
    {
      "artistId": "451545021108191257",
      "fileName": "Loritta_-_Gasolina.png",
      "additionalInfo": "<b>Amino:</b> <a href=\"http://aminoapps.com/p/9jkwt7\">Link</a>"
    },
    {
      "artistId": "385594208431374337",
      "fileName": "Loritta_-_pq.png"
    },
    {
      "artistId": "348641184056475656",
      "fileName": "Loritta_-_Megamenizada.png"
    },
    {
      "artistId": "224299319396663296",
      "fileName": "Loritta3_-_Ayano.png"
    },
    {
      "artistId": "284121713749524480",
      "fileName": "Loritta2_-_Silvacrest.png"
    },
    {
      "artistId": "197501878399926272",
      "fileName": "LorittaVSJerbs_-_Paum.png"
    },
    {
      "artistId": "303291649742864384",
      "fileName": "Loritta_-_Avineto.png"
    },
    {
      "artistId": "422839753923362827",
      "fileName": "Loritta_-_Goth!SansG.png"
    },
    {
      "artistId": "466795961667158027",
      "fileName": "Loritta_-_Dusk_Otavio.png"
    },
    {
      "artistId": "351183336531820546",
      "fileName": "Loritta_-_Lil_Kashi.png"
    },
    {
      "artistId": "346779649247936522",
      "fileName": "Loritta_-_Helena.png"
    },
    {
      "artistId": "359507149635846145",
      "fileName": "Loritta_Loja_-_Rafaella.png"
    },
    {
      "artistId": "162571626477518848",
      "fileName": "Loritta_Body_-_Heathecliff.png"
    },
    {
      "artistId": "162571626477518848",
      "fileName": "Loritta_Cabelo_-_Heathecliff.png"
    },
    {
      "artistId": "162571626477518848",
      "fileName": "Loritta_Placa_-_Heathecliff.png"
    },
    {
      "artistId": "162571626477518848",
      "fileName": "Loritta_Music_-_Heathecliff.gif"
    },
    {
      "artistId": "162571626477518848",
      "fileName": "Loritta_OwO_-_Heathecliff.png"
    },
    {
      "artistId": "287743476467105792",
      "fileName": "Loritta_-_woldas.png"
    },
    {
      "artistId": "162571626477518848",
      "fileName": "Loritta_Girando_-_Heathecliff.png"
    },
    {
      "artistId": "302128231400603650",
      "fileName": "Loritta_-_Gab.png"
    },
    {
      "artistId": "365319185498243074",
      "fileName": "Loritta2_-_Estevao.png"
    },
    {
      "artistId": "359507149635846145",
      "fileName": "Loritta_-_Editedjeans.png"
    },
    {
      "artistId": "203199927986159616",
      "fileName": "Loritta_Pistola_-_Gabizinha.png"
    },
    {
      "artistId": "462993747949387796",
      "fileName": "Loritta_-_Lucas.png"
    },
    {
      "artistId": "512397127427686400",
      "fileName": "Loritta_-_ag.png"
    },
    {
      "artistId": "249557171300335616",
      "fileName": "Loritta_-_Dudys.png"
    },
    {
      "artistId": "335569045875195914",
      "fileName": "Loritta_-_Arcen.jpg"
    },
    {
      "artistId": "335569045875195914",
      "fileName": "Loritta_17sz_-_Arcen.jpg"
    },
    {
      "artistId": "403591142584614912",
      "fileName": "Loritta_-_Pardo_Player.jpg"
    },
    {
      "artistId": "402271735447289866",
      "fileName": "Loritta_-_GR4VIDADE.jpg"
    },
    {
      "artistId": "255109335951081472",
      "fileName": "Loritta_-_My_Name_is_Tom.png"
    },
    {
      "artistId": "88120564400553984",
      "fileName": "Pollux_e_Loritta_-_Flicky.png",
      "additionalInfo": "<b>Twitter:</b> <a href=\"https://twitter.com/dasFlicksie\">Link</a></br><b>Pollux:</b> <a href=\"https://pollux.fun\">Link</a>"
    },
    {
      "artistId": "488420809501835268",
      "fileName": "Loritta_-_FabricioCosmo.jpg"
    },
    {
      "artistId": "162571626477518848",
      "fileName": "Loritta_-_Heathecliff.gif"
    },
    {
      "artistId": "304292804216225793",
      "fileName": "Smol_Loritta_-_Rickinho.png"
    },
    {
      "artistId": "343897778709200898",
      "fileName": "Loritta_-_SanLovesVini.jpg"
    },
    {
      "artistId": "445306203019411467",
      "fileName": "Loritta_-_VadenAng.png"
    },
    {
      "artistId": "342381543139966997",
      "fileName": "Loritta_-_Naomi-kun.jpg"
    },
    {
      "artistId": "301870694822707204",
      "fileName": "Loritta_-_NicoBros_3.png"
    },
    {
      "artistId": "162571626477518848",
      "fileName": "Loritta_Churrasco_-_Heathecliff.png"
    },
    {
      "artistId": "203199927986159616",
      "fileName": "lori_torrada.png"
    },
    {
      "artistId": "534186420299104256",
      "fileName": "Loritta_-_Gatinha.png"
    },
    {
      "artistId": "335569045875195914",
      "fileName": "Loritta_-_Arcen.png"
    },
    {
      "artistId": "224299319396663296",
      "fileName": "Loritta_Dormindo_-_Ayano.png"
    },
    {
      "artistId": "304292804216225793",
      "fileName": "Loritta_Comendo_-_Rickinho3.png"
    },
    {
      "artistId": "304292804216225793",
      "fileName": "Loritta_Pudim_-_Rickinho3.png"
    },
    {
      "artistId": "304292804216225793",
      "fileName": "Vem_de_Zap_-_Rickinho.png"
    },
    {
      "artistId": "379360433641095169",
      "fileName": "Loritta_Sentada_-_Isa_Flok.png"
    },
    {
      "artistId": "359507149635846145",
      "fileName": "Loritta_Brasil_-_Editedjeans.png"
    },
    {
      "artistId": "379360433641095169",
      "fileName": "Loritta_Brasil_-_Isa_Flok.png"
    },
    {
      "artistId": "392641259254317066",
      "fileName": "Loritta_Pudim_-_AFurryBoio.png"
    },
    {
      "artistId": "474589029078269952",
      "fileName": "Loritta_-_p4nic.jpg"
    },
    {
      "artistId": "366297726293508096",
      "fileName": "Loritta_-_The_Edgy_Mystery.png"
    },
    {
      "artistId": "184817548368281603",
      "fileName": "Loritta_-_Nick.png"
    },
    {
      "artistId": "429788299419451412",
      "fileName": "Loritta_-_AnaBia.jpg"
    },
    {
      "artistId": "456229024478396427",
      "fileName": "Loritta_-_Miguel_309.png"
    },
    {
      "artistId": "248237759456608256",
      "fileName": "Loritta_2_-_Brockolis.png"
    },
    {
      "artistId": "273957582224228353",
      "fileName": "Loritta_2_-_MarkinhosMLP.jpg"
    },
    {
      "fileName": "Loritta_Dinheiro_-_July97Chan.jpg",
      "fancyName": "July97Chan",
      "additionalInfo": "<b>DeviantArt:</b> <a href=\"https://deviantart.com/july97chan\">Link</a>"
    },
    {
      "fileName": "Loritta_Temmie_-_July97Chan.gif",
      "fancyName": "July97Chan",
      "additionalInfo": "<b>DeviantArt:</b> <a href=\"https://deviantart.com/july97chan\">Link</a>"
    },
    {
      "fileName": "Loritta_Temmie_2_-_July97Chan.gif",
      "fancyName": "July97Chan",
      "additionalInfo": "<b>DeviantArt:</b> <a href=\"https://deviantart.com/july97chan\">Link</a></br>"
    },
    {
      "artistId": "359507149635846145",
      "fileName": "Loritta_Real_-_Editedjeans.png"
    },
    {
      "artistId": "406953290346135552",
      "fileName": "Loritta_-_Gigzu.png"
    },
    {
      "artistId": "314966364873818112",
      "fileName": "Loritta_-_Azu.png"
    },
    {
      "artistId": "351760430991147010",
      "fileName": "Loritta_Pudim_-_Arth.png"
    },
    {
      "artistId": "249557171300335616",
      "fileName": "Loritta_2_-_Dudys.png"
    },
    {
      "artistId": "366405124702339072",
      "fileName": "Loritta_-_Aniih.jpg"
    },
    {
      "artistId": "327632348881485825",
      "fileName": "Loritta_-_Alone.png"
    },
    {
      "artistId": "524624364277334036",
      "fancyName": "Drawn Mask",
      "fileName": "Loritta_Thumbnail_-_Drawn_Mask.png",
      "additionalInfo": "<b>Video:</b> <a href=\"https:/bit.ly/loritales\">Link</a>"
    },
    {
      "artistId": "524624364277334036",
      "fancyName": "Drawn Mask",
      "fileName": "Loritta_Mesa_-_Drawn_Mask.png",
      "additionalInfo": "<b>Video:</b> <a href=\"https:/bit.ly/loritales\">Link</a>"
    },
    {
      "artistId": "524624364277334036",
      "fancyName": "Drawn Mask",
      "fileName": "Loritta_Corda_-_Drawn_Mask.png",
      "additionalInfo": "<b>Video:</b> <a href=\"https:/bit.ly/loritales\">Link</a>"
    },
    {
      "artistId": "524624364277334036",
      "fancyName": "Drawn Mask",
      "fileName": "Loritta_Cara_-_Drawn_Mask.png",
      "additionalInfo": "<b>Video:</b> <a href=\"https:/bit.ly/loritales\">Link</a>"
    },
    {
      "artistId": "183639869308796928",
      "fileName": "Loritta_-_Hiro.png"
    },
    {
      "artistId": "496433725136044032",
      "fileName": "Loritta_-_bettyecharagames.jpg"
    },
    {
      "fileName": "Loritta_-_bluebbird_.jpg",
      "fancyName": "bia",
      "additionalInfo": "<b>Twitter:</b> <a href=\"https://twitter.com/bluebbird_\">Link</a></br><b>Video:</b> <a href=\"https:/bit.ly/loritales\">Link</a>"
    },
    {
      "artistId": "405933975270326272",
      "fileName": "Loritta_-_Pietr0Chan.png"
    },
    {
      "artistId": "358394549175058432",
      "fileName": "Loritta_-_vitor.png"
    },
    {
      "artistId": "447820831339315211",
      "fileName": "Loritta_-_Thomas_Dantas.png"
    },
    {
      "artistId": "435510880764166164",
      "fileName": "Loritta_-_Akko.png"
    },
    {
      "artistId": "317663282041323520",
      "fileName": "Loritta_-_Arroux_Fryto.png"
    },
    {
      "artistId": "361977144445763585",
      "fileName": "Loritta_-_PeterStark000.png"
    },
    {
      "artistId": "361977144445763585",
      "fileName": "Loritta2_-_PeterStark000.png"
    },
    {
      "artistId": "492079389928849418",
      "fileName": "Loritta_-_Rogertardado.png"
    },
    {
      "artistId": "492079389928849418",
      "fileName": "Loritta2_-_Rogertardado.png"
    },
    {
      "artistId": "256939071664816129",
      "fileName": "Loritta_-_Yumi.png"
    },
    {
      "artistId": "256939071664816129",
      "fileName": "Loritta_2_-_Yumi.png"
    },
    {
      "artistId": "256939071664816129",
      "fileName": "Loritta_3_-_Yumi.png"
    },
    {
      "artistId": "256939071664816129",
      "fileName": "Loritta_4_-_Yumi.png"
    },
    {
      "artistId": "256939071664816129",
      "fileName": "Loritta_5_-_Yumi.png"
    },
    {
      "artistId": "426742520106713089",
      "fileName": "Loritta_-_xxxtentacion.png"
    },
    {
      "artistId": "460465516469813260",
      "fileName": "Loritta_-_Moji_Paladins.png"
    },
    {
      "artistId": "244853550209957889",
      "fileName": "Loritta_-_Hide.png"
    },
    {
      "artistId": "359507149635846145",
      "fileName": "Loritta_Real_2_-_Editedjeans.png"
    },
    {
      "fancyName": "bel",
      "fileName": "Loritta_1_-_bel.jpg"
    },
    {
      "fancyName": "bel",
      "fileName": "Loritta_2_-_bel.jpg"
    },
    {
      "artistId": "514961762148155398",
      "fileName": "Loritta_-_veriixx.jpg"
    },
    {
      "artistId": "514961762148155398",
      "fileName": "Loritta_2_-_veriixx.jpg"
    },
    {
      "artistId": "341391253616721920",
      "fileName": "Loritta_-_KingSmooth.jpg"
    },
    {
      "artistId": "392641259254317066",
      "fileName": "Loritta_Rosa_-_furryboio.png"
    },
    {
      "artistId": "392641259254317066",
      "fileName": "Loritta_Cowboy_-_furryboio.png"
    },
    {
      "artistId": "416056545051279370",
      "fileName": "Loritta_-_DokyoTV.jpg"
    },
    {
      "artistId": "328190215556431873",
      "fileName": "Loritta_-_DostyaAK.jpg"
    },
    {
      "artistId": "419125869475397633",
      "fileName": "Loritta_-_Allouette.jpg"
    },
    {
      "artistId": "328225105027268611",
      "fileName": "Loritta_-_EvilEdd.png"
    },
    {
      "artistId": "485595319313235981",
      "fileName": "Loritta_-_Aiiko.png"
    },
    {
      "artistId": "512953484560302090",
      "fileName": "Loritta_-_RicardoPR.png"
    },
    {
      "artistId": "335193454034419715",
      "fileName": "Loritta_-_N1.png"
    },
    {
      "artistId": "451415020769902615",
      "fileName": "Loritta_-_come.png"
    },
    {
      "artistId": "451415020769902615",
      "fileName": "Loritta_Sans_-_snowl.png"
    },
    {
      "artistId": "451415020769902615",
      "fileName": "Loritta_Pascoa_-_snowl.png"
    },
    {
      "artistId": "492079389928849418",
      "fileName": "Loritta_Cat_-_Raspozaa.jpg"
    },
    {
      "artistId": "449940691045318656",
      "fileName": "Loritta_-_Foguinel.png"
    },
    {
      "artistId": "529077351321960448",
      "fileName": "Loritta_-_BielMene.png"
    },
    {
      "artistId": "423465457673306114",
      "fileName": "Loritta_-_Kened.jpg"
    },
    {
      "artistId": "406802429456416768",
      "fileName": "Loritta_-_Bia_wolf_games.png"
    },
    {
      "artistId": "462379701391065098",
      "fileName": "Loritta_-_Susie.png"
    },
    {
      "artistId": "464850798631976965",
      "fileName": "Loritta_-_Lott.jpg"
    },
    {
      "artistId": "544223901459283989",
      "fileName": "Loritta_-_Ino.jpg"
    },
    {
      "artistId": "467496654531067914",
      "fileName": "Loritta_-_Po_Yoyo_Mano.png"
    },
    {
      "artistId": "168123859898204161",
      "fileName": "Loritta_-_Mikaru.png",
      "fancyName": "Mikaru",
      "additionalInfo": "<b>YouTube:</b> <a href=\"https://www.youtube.com/user/deusadocecartoon\">Link</a></br>Twitter: <a href=\"https://twitter.com/mik4ru\">Link</a>"
    },
    {
      "artistId": "390976882998509569",
      "fileName": "Loritta_Natal_-_Natan.png"
    },
    {
      "artistId": "351760430991147010",
      "fileName": "Loritta_Ano_Novo_-_Arth.png"
    },
    {
      "artistId": "304292804216225793",
      "fileName": "Loritta_Buxin_Chei_-_Rickinho3.png"
    },
    {
      "artistId": "304292804216225793",
      "fileName": "Loritta_Shrug_-_Rickinho3.png"
    },
    {
      "artistId": "359507149635846145",
      "fileName": "Loritta_Aww_-_Editedjeans.png"
    },
    {
      "artistId": "203199927986159616",
      "fileName": "Loritta_Acordando_-_Gabizinha.png"
    },
    {
      "artistId": "203199927986159616",
      "fileName": "Loritta_Presents_-_Gabizinha.png"
    },
    {
      "artistId": "203199927986159616",
      "fileName": "Loritta_Aww_-_Gabizinha.png"
    },
    {
      "artistId": "203199927986159616",
      "fileName": "Loritta_Beijo_-_Gabizinha.png"
    },
    {
      "artistId": "203199927986159616",
      "fileName": "Loritta_Comida_-_Gabizinha.png"
    },
    {
      "artistId": "203199927986159616",
      "fileName": "Loritta_Heart_-_Gabizinha.png"
    },
    {
      "artistId": "203199927986159616",
      "fileName": "Loritta_Pipoca_-_Gabizinha.png"
    },
    {
      "artistId": "203199927986159616",
      "fileName": "Loritta_Triste_-_Gabizinha.png"
    },
    {
      "artistId": "203199927986159616",
      "fileName": "Loritta_Taca_-_Gabizinha.png"
    },
    {
      "artistId": "203199927986159616",
      "fileName": "Loritta_Yay_-_Gabizinha.png"
    },
    {
      "artistId": "162571626477518848",
      "fileName": "lori_natal_1.png"
    },
    {
      "artistId": "162571626477518848",
      "fileName": "lori_natal_2.png"
    },
    {
      "artistId": "162571626477518848",
      "fileName": "lori_natal_3.png"
    },
    {
      "artistId": "162571626477518848",
      "fileName": "l1.png"
    },
    {
      "artistId": "162571626477518848",
      "fileName": "l2.png"
    },
    {
      "artistId": "162571626477518848",
      "fileName": "l3.png"
    },
    {
      "artistId": "162571626477518848",
      "fileName": "l4.png"
    },
    {
      "artistId": "162571626477518848",
      "fileName": "l5.png"
    },
    {
      "artistId": "162571626477518848",
      "fileName": "l6.png"
    },
    {
      "artistId": "162571626477518848",
      "fileName": "l7.png"
    },
    {
      "artistId": "162571626477518848",
      "fileName": "l8.png"
    },
    {
      "artistId": "216586322788352010",
      "fileName": "Lori_Filosofando_-_KneezMz.png"
    },
    {
      "artistId": "451415020769902615",
      "fileName": "Loritta_Cabelo_Curto_-_snolw.png"
    },
    {
      "artistId": "451415020769902615",
      "fileName": "Lori_Feijoada_-_snowl.png"
    },
    {
      "artistId": "419125869475397633",
      "fileName": "Loritta_Baby_-_Allouette.jpg"
    },
    {
      "artistId": "419125869475397633",
      "fileName": "Loritta_2_-_Allouette.jpg"
    },
    {
      "artistId": "419125869475397633",
      "fileName": "Loritta_Crayons_-_Allouette.jpg"
    },
    {
      "artistId": "419125869475397633",
      "fileName": "Loritta_Crescida_-_Allouette.jpg"
    },
    {
      "artistId": "419125869475397633",
      "fileName": "Loritta_LongCat_-_Allouette.png"
    },
    {
      "artistId": "419125869475397633",
      "fileName": "Loritta_Que_-_Allouette.jpg"
    },
    {
      "artistId": "419125869475397633",
      "fileName": "Loritta_Owo_-_Allouette.jpg"
    },
    {
      "artistId": "419125869475397633",
      "fileName": "Loritta_Triggered_-_Allouette.jpg"
    },
    {
      "artistId": "419125869475397633",
      "fileName": "Loritta_Piscina_-_Allouette.png"
    },
    {
      "artistId": "419125869475397633",
      "fileName": "Loritta_Streamer_-_Allouette.png"
    },
    {
      "artistId": "419125869475397633",
      "fileName": "Loritta_Dormindo_-_Allouette.gif"
    },
    {
      "artistId": "419125869475397633",
      "fileName": "Loritta_Vestido_-_Allouette.jpg"
    },
    {
      "artistId": "419125869475397633",
      "fileName": "Loritta_3_-_Allouette.jpg"
    },
    {
      "artistId": "419125869475397633",
      "fileName": "Loritta_Chorando_-_Allouette.jpg"
    },
    {
      "artistId": "419125869475397633",
      "fileName": "Loritta_Love_-_Allouette.jpg"
    },
    {
      "artistId": "419125869475397633",
      "fileName": "Loritta_Effect_-_Allouette.jpg"
    },
    {
      "artistId": "544223901459283989",
      "fileName": "Loritta_-_Inork.png"
    },
    {
      "artistId": "544223901459283989",
      "fileName": "Loritta_2_-_Inork.png"
    },
    {
      "artistId": "335193454034419715",
      "fileName": "Loritta_Sorvete_-_N1.png"
    },
    {
      "artistId": "492079389928849418",
      "fileName": "Loritta_Dinheiro_Voando_-_Raspozaa.jpg"
    },
    {
      "artistId": "544223901459283989",
      "fileName": "Loritta_3_-_Inork.png"
    },
    {
      "artistId": "492079389928849418",
      "fileName": "Loritta_Peace_-_Raspozaa.jpg"
    },
    {
      "artistId": "419125869475397633",
      "fileName": "Loritta_Olhando_-_Allouette.jpg"
    },
    {
      "artistId": "458762940594782249",
      "fileName": "Loritta_-_Dreazinha.png"
    },
    {
      "artistId": "304292804216225793",
      "fileName": "Loritta_Abracando_Gessy_-_Rickinho3.png"
    },
    {
      "artistId": "419125869475397633",
      "fileName": "Loritta_Flutuando_-_Allouette.jpg"
    },
    {
      "artistId": "419125869475397633",
      "fileName": "Loritta_Lolipop_-_Allouette.png"
    },
    {
      "artistId": "366405124702339072",
      "fileName": "Loritta_2_-_Aniih.png"
    },
    {
      "artistId": "288417511286898689",
      "fileName": "Loritta_-_myah.png"
    },
    {
      "artistId": "301624425047130112",
      "fileName": "Loritta_-_Factory_Girl.jpg"
    },
    {
      "artistId": "301624425047130112",
      "fileName": "Loritta_2_-_Factory_Girl.png"
    },
    {
      "artistId": "301624425047130112",
      "fileName": "Loritta_3_-_Factory_Girl.jpg"
    },
    {
      "artistId": "224167422519803904",
      "fileName": "Loritta_-_Calop34.png"
    },
    {
      "artistId": "402519250578964481",
      "fileName": "Loritta_-_Kaneki.png"
    },
    {
      "artistId": "461688564850360341",
      "fileName": "Leandro_-_Ousado.png"
    },
    {
      "artistId": "466055162465746957",
      "fileName": "Loritta_-_jonatanfelipe10.jpg"
    },
    {
      "artistId": "335193454034419715",
      "fileName": "Loritta_20k_-_N1.png"
    },
    {
      "artistId": "1",
      "fancyName": "Minibyte",
      "fileName": "Loritta_-_Minibyte.jpg"
    },
    {
      "artistId": "385479127656038412",
      "fileName": "Loritta_-_Anenha.jpg"
    },
    {
      "artistId": "381185416038055947",
      "fileName": "Loritta_-_Dudita.png"
    },
    {
      "artistId": "329846902231400458",
      "fileName": "Loritta_-_FireMisteriosa.png"
    },
    {
      "artistId": "492079389928849418",
      "fileName": "Lori_Programadora_-_Raspozaa.jpg"
    },
    {
      "artistId": "492079389928849418",
      "fileName": "Lori_Daily_-_Raspozaa.jpg"
    },
    {
      "artistId": "335193454034419715",
      "fileName": "Loritta_2_-_N1.png"
    },
    {
      "artistId": "414841675794481155",
      "fileName": "Loritta_-_M3.png"
    },
    {
      "artistId": "414841675794481155",
      "fileName": "Loritta_2_-_M3.png"
    },
    {
      "artistId": "390976882998509569",
      "fileName": "Loritta_-_NatanDeSa.jpg"
    },
    {
      "artistId": "463036675245735940",
      "fileName": "Loritta_-_AWebNamoradaDeAlguem.png"
    },
    {
      "artistId": "463036675245735940",
      "fileName": "Loritta_2_-_AWebNamoradaDeAlguem.png"
    },
    {
      "artistId": "463036675245735940",
      "fileName": "Loritta_3_-_AWebNamoradaDeAlguem.png"
    },
    {
      "artistId": "463036675245735940",
      "fileName": "Loritta_4_-_AWebNamoradaDeAlguem.jpg"
    },
    {
      "artistId": "304292804216225793",
      "fileName": "Loritta_Generations_-_Rickinho3.png"
    },
    {
      "artistId": "503313642603937793",
      "fileName": "Loritta_-_Kunata.jpg"
    },
    {
      "artistId": "196798877725229057",
      "fileName": "Loritta_-_Brenoplays2.png"
    },
    {
      "artistId": "414841675794481155",
      "fileName": "Loritta_3_-_M3.png"
    },
    {
      "artistId": "416056545051279370",
      "fileName": "Loritta_2_-_DokyoTV.png"
    },
    {
      "artistId": "320338866940936194",
      "fileName": "Loritta_-_Hataoh.png"
    },
    {
      "artistId": "440516809201025024",
      "fileName": "Loritta_-_Nya.png"
    },
    {
      "artistId": "440516809201025024",
      "fileName": "Loritta_2_-_Nya.png"
    },
    {
      "artistId": "440516809201025024",
      "fileName": "Loritta_3_-_Nya.png"
    },
    {
      "artistId": "442030704730046485",
      "fileName": "Loritta_-_RAE.jpg"
    },
    {
      "artistId": "531301391268839449",
      "fileName": "Loritta_-_gabrielasakura.png"
    },
    {
      "artistId": "460465516469813260",
      "fileName": "Loritta_Naruto_-_Strange.png"
    },
    {
      "artistId": "366405124702339072",
      "fileName": "Loritta_3_-_Aniih.png"
    },
    {
      "artistId": "301624425047130112",
      "fileName": "Loritta_4_-_Factory_Girl.png"
    },
    {
      "artistId": "419125869475397633",
      "fileName": "Loritta_Faca_-_Allouette.jpg"
    },
    {
      "artistId": "531301391268839449",
      "fileName": "Loritta_Comandos_-_gabrielasakura.png"
    },
    {
      "artistId": "366405124702339072",
      "fileName": "Loritta_Heart_-_Aniih.png"
    },
    {
      "artistId": "366405124702339072",
      "fileName": "Loritta_Clap_-_Aniih.png"
    },
    {
      "artistId": "366405124702339072",
      "fileName": "Loritta_Ship_-_Aniih.png"
    },
    {
      "artistId": "419125869475397633",
      "fileName": "Loritta_Bust_-_Allouette.png"
    },
    {
      "artistId": "419125869475397633",
      "fileName": "Loritta_Thumbs_Up_-_Allouette.png"
    },
    {
      "artistId": "513485895454949396",
      "fileName": "Loritta_-_NanaDoki.png"
    },
    {
      "artistId": "196798877725229057",
      "fileName": "Loritta_Pantufa_Gabriela_-_Brenoplays2.png"
    },
    {
      "artistId": "469151022800175106",
      "fileName": "Loritta_-_KitaNagarashii.jpg"
    },
    {
      "artistId": "305787476021215232",
      "fileName": "Loritta_-_NasciLagado.png"
    },
    {
      "artistId": "373593646072856577",
      "fileName": "Loritta_-_Crep.png"
    },
    {
      "artistId": "361977144445763585",
      "fileName": "Loritta_Minecraft_-_PeterStark000.png"
    },
    {
      "artistId": "555519251587399715",
      "fileName": "Loritta_-_NIKEEH.jpg"
    },
    {
      "artistId": "468138773432107010",
      "fileName": "Loritta_-_Delly1000.jpg"
    },
    {
      "artistId": "302924991169560577",
      "fileName": "Loritta_-_Blob.png"
    },
    {
      "artistId": "460465516469813260",
      "fileName": "Loritta_Anniversary_2019_-_Strange.png"
    },
    {
      "artistId": "419125869475397633",
      "fileName": "Loritta_Anniversary_2019_-_Allouette.png"
    },
    {
      "artistId": "440516809201025024",
      "fileName": "Loritta_Anniversary_2019_-_Hollow.png"
    },
    {
      "artistId": "203199927986159616",
      "fileName": "Loritta_Anniversary_2019_-_Its_Gabi.png"
    },
    {
      "artistId": "245711839491522561",
      "fileName": "Loritta_-_JuMaria.png"
    },
    {
      "artistId": "458758166361145375",
      "fileName": "Loritta_-_Alce.jpg"
    },
    {
      "artistId": "464850798631976965",
      "fileName": "Loritta_Anniversary_2019_-_Lott.jpg"
    },
    {
      "artistId": "361977144445763585",
      "fileName": "Loritta_Anniversary_2019_-_PeterStark000.png"
    },
    {
      "artistId": "366405124702339072",
      "fileName": "Loritta_Anniversary_2019_-_Aniih.png"
    },
    {
      "artistId": "162571626477518848",
      "fileName": "Loritta_Anniversary_2019_-_Heathecliff.gif"
    },
    {
      "artistId": "531301391268839449",
      "fileName": "Loritta_Anniversary_2019_-_gabrielasakura.png"
    },
    {
      "artistId": "305787476021215232",
      "fileName": "Loritta_Anniversary_2019_-_NasciLagado.jpg"
    },
    {
      "artistId": "512397127427686400",
      "fileName": "Loritta_Anniversary_2019_-_lia.png"
    },
    {
      "artistId": "343897778709200898",
      "fileName": "Loritta_-_S2Inner.jpg"
    },
    {
      "artistId": "302924991169560577",
      "fileName": "Loritta_Minecraft_-_Blob.png"
    },
    {
      "artistId": "451415020769902615",
      "fileName": "Loritta_Anniversary_2019_-_snowl.png"
    },
    {
      "artistId": "196798877725229057",
      "fileName": "Loritta_Anniversary_2019_-_Brenoplays2.png"
    },
    {
      "artistId": "372470709961621520",
      "fileName": "Loritta_Anniversary_2019_-_Mandy.png"
    },
    {
      "artistId": "162571626477518848",
      "fileName": "Loritta_Anniversary_2019_2_-_Heathecliff.png"
    },
    {
      "artistId": "419125869475397633",
      "fileName": "Loritta_Anniversary_2019_2_-_Allouette.png"
    },
    {
      "artistId": "461688564850360341",
      "fileName": "Loritta_Anniversary_2019_-_Ousado.png"
    },
    {
      "artistId": "196798877725229057",
      "fileName": "Loritta_Brad_-_Brenoplays2.png"
    },
    {
      "artistId": "457614348169248768",
      "fileName": "Loritta_-_Flageni.png"
    },
    {
      "artistId": "430384932389650442",
      "fileName": "Loritta_-_Samekichi.png"
    },
    {
      "artistId": "397829942349660163",
      "fileName": "Loritta_-_Kanna.png"
    },
    {
      "artistId": "470336604326592512",
      "fileName": "Loritta_-_Caramela.png"
    },
    {
      "artistId": "551778747213414407",
      "fileName": "Loritta_-_IsabellaCute.png"
    },
    {
      "artistId": "551778747213414407",
      "fileName": "Loritta_2_-_IsabellaCute.jpg"
    },
    {
      "artistId": "501079068113109004",
      "fileName": "Loritta_-_ming.png"
    },
    {
      "artistId": "501079068113109004",
      "fileName": "Loritta_2_-_ming.png"
    },
    {
      "artistId": "306915894246768640",
      "fileName": "Loritta_-_Saiyan.png"
    },
    {
      "artistId": "2",
      "fancyName": "NyahKun",
      "fileName": "Loritta_Comission_-_NyahKun.png"
    },
    {
      "artistId": "470336604326592512",
      "fileName": "Loritta_Anniversary_2019_-_Caramela.png"
    },
    {
      "artistId": "419125869475397633",
      "fileName": "Loritta_Sign_-_Allouette.png"
    },
    {
      "artistId": "433700054726082560",
      "fileName": "Loritta_-_SirPoke.jpg"
    },
    {
      "artistId": "304292804216225793",
      "fileName": "Loritta_Anniversary_2019_-_Rickinho3.png"
    },
    {
      "artistId": "414841675794481155",
      "fileName": "Loritta_Anniversary_2019_-_Mel.png"
    },
    {
      "artistId": "433700054726082560",
      "fileName": "Loritta_Thinking_-_SirPoke.jpg"
    },
    {
      "artistId": "361977144445763585",
      "fileName": "Loritta_Paper_-_PeterStark000.png"
    },
    {
      "artistId": "460465516469813260",
      "fileName": "Loritta_Boy_-_Strange.jpg"
    },
    {
      "artistId": "162571626477518848",
      "fileName": "Loritta_Derp_-_Heathecliff.png"
    },
    {
      "artistId": "196798877725229057",
      "fileName": "Loritta_Smug_-_Brenoplays2.png"
    },
    {
      "artistId": "433700054726082560",
      "fileName": "Loritta_Anniversary_2019_-_SirPoke.jpg"
    },
    {
      "artistId": "463036675245735940",
      "fileName": "Loritta_5_-_AWebNamoradaDeAlguem.png"
    },
    {
      "artistId": "335193454034419715",
      "fileName": "Loritta_Anniversary_2019_-_N1.png"
    },
    {
      "artistId": "302924991169560577",
      "fileName": "Loritta_Anniversary_2019_-_Blob.png"
    },
    {
      "artistId": "341757341458366464",
      "fileName": "Loritta_-_ItzXandee.jpg"
    },
    {
      "artistId": "409845061694783528",
      "fileName": "Loritta_Cat_-_Scrr.png"
    },
    {
      "artistId": "497932167884832768",
      "fileName": "Loritta_-_erwatowo.jpg"
    },
    {
      "artistId": "497932167884832768",
      "fileName": "Loritta_Reference_-_Maza.jpg"
    },
    {
      "artistId": "416056545051279370",
      "fileName": "Loritta_Selfie_-_DokyoTV.png"
    },
    {
      "artistId": "416056545051279370",
      "fileName": "Loritta_Anniversary_2019_-_DokyoTV.png"
    },
    {
      "artistId": "358747114911694860",
      "fileName": "Loritta_-_Tuti25.png"
    },
    {
      "artistId": "384397682145492994",
      "fileName": "Loritta_-_CarnotauroDoAcre.png"
    },
    {
      "artistId": "301624425047130112",
      "fileName": "Loritta_Anniversary_2019_-_Miela.png"
    },
    {
      "artistId": "392641259254317066",
      "fileName": "Loritta_Anniversary_2019_-_Yeonjun.png"
    },
    {
      "artistId": "451415020769902615",
      "fileName": "Loritta_Rebelde_-_snowl.png"
    },
    {
      "artistId": "451415020769902615",
      "fileName": "Loritta_Cat_-_snowl.png"
    },
    {
      "artistId": "451415020769902615",
      "fileName": "Loritta_Paisagem_-_snowl.png"
    },
    {
      "artistId": "451415020769902615",
      "fileName": "Loritta_Tea_-_snowl.png"
    },
    {
      "artistId": "422839753923362827",
      "fileName": "Loritta_-_InkSans.png"
    },
    {
      "artistId": "531301391268839449",
      "fileName": "Loritta_Anniversary_2019_2_-_gabrielasakura.png"
    },
    {
      "artistId": "492079389928849418",
      "fileName": "Loritta_Anniversary_2019_-_Raspozaa.jpg"
    },
    {
      "artistId": "385479127656038412",
      "fileName": "Loritta_Anniversary_2019_-_Anenha.jpg"
    },
    {
      "artistId": "474648528187162645",
      "fileName": "Loritta_Paper_-_Senpai.jpg"
    },
    {
      "artistId": "551778747213414407",
      "fileName": "Loritta_Paper_-_IsabellaCute.png"
    },
    {
      "artistId": "405065084692463619",
      "fileName": "Loritta_-_ErroGirl.png"
    },
    {
      "artistId": "405065084692463619",
      "fileName": "Loritta_Baby_-_ErroGirl.png"
    },
    {
      "artistId": "475024047676063755",
      "fileName": "Loritta_Anniversary_2019_-_SKULL.png"
    },
    {
      "artistId": "475024047676063755",
      "fileName": "Loritta_2_-_SKULL.png"
    },
    {
      "artistId": "558990504264728586",
      "fileName": "Loritta_-_Raspberry.png"
    },
    {
      "artistId": "535666173761683466",
      "fileName": "Loritta_-_Lolizinha.png"
    },
    {
      "artistId": "530860657579524126",
      "fileName": "Loritta_-_bune.png"
    },
    {
      "artistId": "252169618209243138",
      "fileName": "Loritta_-_Tiff.png"
    },
    {
      "artistId": "527856148293091329",
      "fileName": "Loritta_-_CherryTea.png"
    },
    {
      "artistId": "554356558364934148",
      "fileName": "Loritta_-_Jakeking.jpg"
    },
    {
      "artistId": "521058068432420885",
      "fileName": "Loritta_-_Flagelo.png"
    },
    {
      "artistId": "520288399035334660",
      "fileName": "Loritta_-_Josue.png"
    },
    {
      "artistId": "520286515385860136",
      "fileName": "Loritta_-_David.png"
    },
    {
      "artistId": "302690446390591502",
      "fileName": "Loritta_-_Yu.jpg"
    },
    {
      "artistId": "224167422519803904",
      "fileName": "Loritta_Pickaxe_-_Calop34.png"
    },
    {
      "artistId": "312277173404696587",
      "fileName": "Loritta_-_Vleek83.png"
    },
    {
      "artistId": "492723804020408320",
      "fileName": "Loritta_-_DemonBitey.jpg"
    },
    {
      "artistId": "198492771437248512",
      "fileName": "Loritta_-_VTA7991.png"
    },
    {
      "artistId": "477849121013891073",
      "fileName": "Loritta_-_Rafael.png"
    },
    {
      "artistId": "350051805411147786",
      "fileName": "Loritta_-_Reidosgames13YT.png"
    },
    {
      "artistId": "506195163710750720",
      "fileName": "Loritta_-_Kedavan.png"
    },
    {
      "artistId": "288511919382593536",
      "fileName": "Loritta_-_DodaOz.jpg"
    },
    {
      "artistId": "431109695361777665",
      "fileName": "Loritta_-_Breno.png"
    },
    {
      "artistId": "439948714967302155",
      "fileName": "Loritta_-_NakasoNatsuko.png"
    },
    {
      "artistId": "405911077642174464",
      "fileName": "Loritta_-_MagnumsEremita.png"
    },
    {
      "artistId": "528791941316608003",
      "fileName": "Loritta_-_Billy.png"
    },
    {
      "artistId": "463869368447664128",
      "fileName": "Loritta_-_Kouhay.png"
    },
    {
      "artistId": "502823817387966475",
      "fileName": "Loritta_-_Hater_Aleatorio.png"
    },
    {
      "artistId": "397433439227478019",
      "fileName": "Loritta_-_Blank.png"
    },
    {
      "artistId": "514392162008563714",
      "fileName": "Loritta_-_Nikki.png"
    },
    {
      "artistId": "311636187758592001",
      "fileName": "Loritta_-_bunnii.png"
    },
    {
      "artistId": "471788194363342878",
      "fileName": "Loritta_-_TheToxic.png"
    },
    {
      "artistId": "476188016457809932",
      "fileName": "Loritta_-_CloudGirl.png"
    },
    {
      "artistId": "465491229161947145",
      "fileName": "Loritta_-_TiaPeri.png"
    }
  ]
}"""

    val json = jsonParser.parse(str)

    // Gerar artist webSocketSessions
    val artistIds = mutableSetOf<String>()

    json["fan-arts"].array.forEach {
        val artistId = it.obj["artistId"].nullString
        if (artistId == null) {
            println("Broken! $it")
            return@forEach
        }
        artistIds.add(artistId)
    }

    val options = ConfigRenderOptions.defaults()
        .setJson(false)
        .setOriginComments(false)

    val existingArtistIds = mutableSetOf<String>()

    for (artistId in artistIds) {
        val fanArts = mutableListOf<FanArt>()

        var fancyName: String? = null

        // Adicionar fan arts
        json["fan-arts"].array.forEach {
            if (it.obj["artistId"].nullString == artistId) {
                val tags = mutableSetOf<String>()
                if (it["fileName"].string.contains("Anniversary")) {
                    tags.add("anniversary-2019")
                }
                if (it.obj["fancyName"].nullString != null)
                    fancyName = it.obj["fancyName"].nullString

                fanArts.add(
                    FanArt(
                        it["fileName"].string,
                        LocalDate.ofInstant(Files.readAttributes(File("/home/mrpowergamerbr/Imagens/Loritta/fanarts/${it["fileName"].string}").toPath(), BasicFileAttributes::class.java).creationTime().toInstant(), ZoneId.of("America/Sao_Paulo")),
                        tags
                    )
                )
            }
        }

        val realArtistId = fanArts.last().fileName.split("_-_").last().split(".")[0].toLowerCase()
        val artistName = fanArts.last().fileName.split("_-_").last().split(".")[0].replace("_", " ")
        println(realArtistId)

        if (existingArtistIds.contains(realArtistId)) {
            throw RuntimeException("Duplicate artist ID! $realArtistId")
        }

        existingArtistIds.add(realArtistId)

        fanArts.forEach {
            println(it.fileName)
            println(it.createdAt)
            println(it.tags)
        }

        // Generate HOCON
        var conf = """
            id = "$realArtistId"

            info {
                name = "$artistName"
        """.trimIndent()

        if (fancyName != null) {
            conf += """

    override {
        name = "$fancyName"
    }"""
        }

        conf += """
}
        """

        conf += """
fan-arts = [
"""
        fanArts.forEach {
            conf += """    {
        file-name = "${it.fileName}"
        created-at = "${it.createdAt}"
        tags = [${it.tags.joinToString(transform = { "\"${it}\"" })}]
    }
"""
        }

        conf += """]
        """

        if (artistId.toLongOrNull() ?: 0 > 10000) {
            conf += """
networks = [
    {
        type = "discord"
        id = "$artistId"
    }
]
        """
        }

        println(conf)
        /* val artist = FanArtArtist(
            artistId,
            artistId,
            "???",
            fanArts,
            listOf()
        ) */

        val mapper = Constants.HOCON_MAPPER
        val r = mapper.readValue<FanArtArtist>(conf)

        println("Okay! ${r.id}...")
        println("r.info.name: " + r.info.name)
        println("r.info.avatarUrl: " + r.info.avatarUrl)
        println("Has override? ${r.info.override != null}")
        println("Overriden name: ${r.info.override?.name}")
        println("Overriden avatar: ${r.info.override?.avatarUrl}")
        println("Fan Arts (${r.fanArts.size})")
        r.fanArts.forEach {
            println("${it.fileName}...")
            println("Created at ${it.createdAt}")
            println("Tags: ${it.tags}")
        }

        r.socialNetworks?.forEach {
            println(it)
        }
        println("\n\n")

        val f = File("/home/mrpowergamerbr/Documentos/LorittaAssets/website/fan_arts/${realArtistId}.conf")

        if (f.exists()) {
            println("File already exists!")

            if (f.readText() != conf) {
                println("OLD:")
                println(f.readText())
                println("NEW:")
                println(conf)
                println("CONTENTS ARE NOT EQUAL!!! k to keep, o to overwrite")
                /* val a = readLine()!!

                if (a == "o")
                    f.writeText(conf) */
            }
        } else {
            println("File doesn't exist yet")
            f.writeText(conf)
        }

        // val text = artist.toConfig("artist").root().render(options).replace("=", " = ")
        // File("/home/mrpowergamerbr/Documentos/LorittaAssets/website/fan_arts/${artist.discordId}.conf").writeText(text.lines().drop(1).dropLast(2).joinToString("\n"))
    }
}