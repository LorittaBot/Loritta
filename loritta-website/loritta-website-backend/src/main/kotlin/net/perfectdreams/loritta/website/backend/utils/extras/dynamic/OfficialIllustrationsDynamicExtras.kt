package net.perfectdreams.loritta.website.backend.utils.extras.dynamic

import kotlinx.html.DIV
import kotlinx.html.a
import kotlinx.html.div
import kotlinx.html.h2
import kotlinx.html.hr
import kotlinx.html.id
import kotlinx.html.img
import kotlinx.html.p
import kotlinx.html.style

class OfficialIllustrationsDynamicExtras : DynamicExtras(
    "official-artworks",
    "Ilustrações Oficiais da Loritta",
    listOf("mrpowergamerbr")
) {
    override fun generateContent(tag: DIV) {
        tag.div {
            p {
                + "Aqui você irá encontrar todas as ilustrações oficiais feitas da Loritta!"
            }

            p {
                + "Enquanto nós também utilizamos ilustrações feitas por outros usuários em nosso website, aqui você apenas irá encontrar ilustrações feitas pelo MrPowerGamerBR, criador da Loritta! Se uma ilustração não está aqui, significa que ela não foi feita pelo MrPowerGamerBR."
            }

            p {
                + "Aqui é o lugar perfeito para você ter referências de como a Loritta é para a sua fan art da Loritta, como também é ótimo para ver como a nossa querida Morenitta evoluiu no design e estilo dela com o passar do tempo!"
            }

            hr {}

            div {
                id = "fan-art-gallery"

                for (section in artSections) {
                    h2(classes = "left-horizontal-line uppercase") {
                        +section.name
                    }

                    div(classes = "fan-arts-wrapper") {
                        for (art in section.arts) {
                            div(classes = "fan-art-wrapper") {
                                style = "position: relative; line-height: 0; margin: 1em;"

                                a(href = "/v3/assets/img/official${art.path}", target = "_blank") {
                                    img(classes = "fan-art", src = "/v3/assets/img/official${art.path}") {
                                        attributes["loading"] = "lazy"
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    data class ArtSection(
        val name: String,
        val arts: List<Art>
    )

    data class Art(
        val path: String
    )

    companion object {
        private val artSections = listOf(
            ArtSection(
                "Loritta v5 - 01/01/2021",
                listOf(
                    Art("/avatar_v5/avatar_without_text.png"),
                    Art("/avatar_v5/original.png"),
                )
            ),
            ArtSection(
                "Loritta Natal - 09/12/2020",
                listOf(
                    Art("/christmas_2020/owo_2.png"),
                    Art("/christmas_2020/lori_avatar_natal.png"),
                    Art("/christmas_2020/lori_avatar_natal_easter_egg.png")
                )
            ),
            ArtSection(
                "Loritta Chibi - 23/07/2020",
                listOf(
                    Art("/chibi/lori_owo_v2_crop.png"),
                    Art("/chibi/lori_snapdog.png"),
                    Art("/chibi/lori_avatar_v5_v2.png"),
                    Art("/chibi/lori_avatar_v5_dog.png"),
                )
            ),
            ArtSection(
                "Loritta Brasil - 17/01/2020",
                listOf(
                    Art("/brazil_flag/lori_bandeira4.png"),
                    Art("/brazil_flag/lori_bandeira4_moletom_preto.png"),
                )
            ),
            ArtSection(
                "Loritta Yay - 07/12/2019",
                listOf(
                    Art("/yay/lori_yay_test9.png"),
                    Art("/yay/lori_yay_test9_dark_sweater.png"),
                    Art("/yay/lori_github_logo.png"),
                    Art("/yay/lori_update_generic.png"),
                )
            ),
            ArtSection(
                "Outage - 07/12/2019",
                listOf(
                    Art("/outage/outage.png"),
                )
            ),
            ArtSection(
                "Loritta & Pantufa - 07/12/2019",
                listOf(
                    Art("/lori_and_pantufa_show/lori_e_pantufa_show.png"),
                )
            ),
            ArtSection(
                "Loritta & Pantufa - 01/10/2019",
                listOf(
                    Art("/lori_and_pantufa/lori_e_pantufa.jpg"),
                )
            ),
            ArtSection(
                "Lori Dynamic - 30/04/2019",
                listOf(
                    Art("/dynamic/JPEG_20190429_170004.jpg"),
                    Art("/dynamic/lori_dynamic_poster.png"),
                    Art("/dynamic/lori_dynamic_v2.png"),
                    Art("/dynamic/lori_dynamic_v2_dark_sweater.png"),
                )
            ),
            ArtSection(
                "Neymar - 20/04/2019",
                listOf(
                    Art("/neymar/lori_menino_ney_crop.png"),
                )
            ),
            ArtSection(
                "Aniversário do Drawn Mask - 05/04/2019",
                listOf(
                    Art("/drawn_mask_birthday/drawn_mask_birthday.png"),
                )
            ),
            ArtSection(
                "Primeiro de Abril MrBeast - 01/04/2019",
                listOf(
                    Art("/april_2019/lori_april_beast.png"),
                    Art("/april_2019/lori_april_v5.png"),
                )
            ),
            ArtSection(
                "Rolêzinho - 22/02/2019",
                listOf(
                    Art("/rolezinho/rolezinho.png"),
                )
            ),
            ArtSection(
                "Loritta Vroom Vroom - 22/02/2019",
                listOf(
                    Art("/vroom/lori_vroom_vroom.png"),
                )
            ),
            ArtSection(
                "Loritta Pickaxe - 08/12/2018",
                listOf(
                    Art("/pickaxe/loritta_sparkly_logo.png"),
                )
            ),
            ArtSection(
                "Loritta Shrug - 31/11/2018",
                listOf(
                    Art("/shrug/IMG_20181031_121045766.jpg"),
                    Art("/shrug/lori_shrug_v3.png"),
                )
            ),
            ArtSection(
                "Loritta v4 \"2019\" - 15/11/2018",
                listOf(
                    Art("/avatar_v4/loritta_2019_pose.png"),
                    Art("/avatar_v4/lori_avatar_v3.png"),
                )
            ),
            ArtSection(
                "Loritta v3 - 14/11/2018",
                listOf(
                    Art("/avatar_v3/loritta_pose_v4.png"),
                )
            ),
            ArtSection(
                "Loritta Cellphone - 03/11/2018",
                listOf(
                    Art("/cellphone/lori_celular.png")
                )
            ),
            ArtSection(
                "Loritta Deltarune - 01/11/2018",
                listOf(
                    Art("/deltarune/lori_delta_rune_frame1.png"),
                    Art("/deltarune/lori_delta_rune_frame2.png"),
                )
            ),
            ArtSection(
                "Lori Dormindo - 30/10/2018",
                listOf(
                    Art("/sleeping/JPEG_20181030_220750.jpg"),
                )
            ),
            ArtSection(
                "Lori com cara de trouxa - 30/10/2018",
                listOf(
                    Art("/lenny/lori_com_cara_de_trouxa.png"),
                )
            ),
            ArtSection(
                "Smol Lori - 29/10/2018",
                listOf(
                    Art("/smol/smol_lori.png"),
                )
            ),
            ArtSection(
                "Loritta Selfie - 25/10/2018",
                listOf(
                    Art("/selfie/lori_selfie_v2.png"),
                    Art("/selfie/lori_avatar_v2.png"),
                    Art("/selfie/JPEG_20181025_102938.jpg"),
                )
            ),
            ArtSection(
                "Loritta & Gessy - 23/10/2018",
                listOf(
                    Art("/lori_and_gessy/lori_e_gessy_kneez.png"),
                    Art("/lori_and_gessy/lori_e_gessy_crop.png"),
                )
            ),
            ArtSection(
                "Lori com Protetor Solar - 21/10/2018",
                listOf(
                    Art("/sunscreen/lori_praia_v3.png"),
                    Art("/sunscreen/lori_praia_v3_crop.png"),
                )
            ),
            ArtSection(
                "Loritta Neko - 21/10/2018",
                listOf(
                    Art("/neko/lori_neko_crop.png"),
                )
            ),
            ArtSection(
                "Loritta & Pollux - 18/06/2018",
                listOf(
                    Art("/lori_and_pollux/lori_welcomer_v2.png"),
                    Art("/lori_and_pollux/pollux_door.png"),
                )
            ),
            ArtSection(
                "Loritta Autorole - 16/06/2018",
                listOf(
                    Art("/autorole/lori_autorole_hand.png"),
                )
            ),
            ArtSection(
                "Loritta Megaphone - 06/06/2018",
                listOf(
                    Art("/megaphone/lori_megaphone.png"),
                )
            ),
            ArtSection(
                "Loritta Mendigagem - 07/03/2018",
                listOf(
                    Art("/mendigagem/loritta_mendigagem_cover.png"),
                    Art("/mendigagem/lori_mendigagem_v2.png"),
                    Art("/mendigagem/lori_mendigagem_v2_final_Crop.png"),
                )
            ),
            ArtSection(
                "Loritta Headphones - 04/08/2017",
                listOf(
                    Art("/headphones/IMG_20170804_145714483.jpg"),
                    Art("/headphones/loritta_information_desk.png"),
                    Art("/headphones/loritta_password_v2.png"),
                    Art("/headphones/loritta_money.png"),
                    Art("/headphones/loritta_password_money.png")
                )
            ),
            ArtSection(
                "Loritta Banner - 18/06/2017",
                listOf(
                    Art("/banner/Loritta_Banner_-_2017_06_18.png")
                )
            ),
            ArtSection(
                "Loritta v2 - 18/06/2017",
                listOf(
                    Art("/avatar_v2/avatar_final.png"),
                    Art("/avatar_v2/cropped_blue.png"),
                    Art("/avatar_v2/cropped_kondzilla.png"),
                    Art("/avatar_v2/cropped_wow.png"),
                    Art("/avatar_v2/LorittaDiscordAvatar.png"),
                    Art("/avatar_v2/LorittaDiscordAvatar_Funk.png"),
                    Art("/avatar_v2/Loritta_Avatar_Dog.png"),
                    Art("/avatar_v2/Loritta_eu_te_moido.png"),
                    Art("/avatar_v2/loritta_loira.png"),
                    Art("/avatar_v2/loritta_quebrada.png"),
                    Art("/avatar_v2/loritta_quebrada_descolorido.png"),
                )
            ),
            ArtSection(
                "Loritta - 15/06/2017",
                listOf(
                    Art("/avatar/Loritta_-_2017_06_15.png"),
                    Art("/avatar/LorittaDiscordAvatar2.png"),
                    Art("/avatar/LorittaTemporary3.png"),
                )
            )
        )
    }
}