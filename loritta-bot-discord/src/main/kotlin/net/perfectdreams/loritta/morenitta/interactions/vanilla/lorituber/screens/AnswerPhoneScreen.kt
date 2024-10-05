package net.perfectdreams.loritta.morenitta.interactions.vanilla.lorituber.screens

import dev.minn.jda.ktx.messages.MessageEdit
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.interactions.InteractionHook
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import net.perfectdreams.loritta.lorituber.PhoneCall
import net.perfectdreams.loritta.lorituber.rpc.packets.CharacterUseItemResponse
import net.perfectdreams.loritta.morenitta.interactions.vanilla.lorituber.LoriTuberCommand
import net.perfectdreams.loritta.morenitta.utils.extensions.await

class AnswerPhoneScreen(
    command: LoriTuberCommand,
    user: User,
    hook: InteractionHook,
    val character: LoriTuberCommand.PlayerCharacter,
    val answerPhoneResponse: CharacterUseItemResponse.Success.AnswerCall,
) : LoriTuberScreen(command, user, hook) {
    override suspend fun render() {
        if (command.checkMail(user, hook, character, this))
            return

        val viewMotivesButton = loritta.interactivityManager.buttonForUser(
            user,
            ButtonStyle.PRIMARY,
            "Voltar ao cafofo",
            {
                emoji = Emoji.fromUnicode("\uD83C\uDFE0")
            }
        ) {
            command.switchScreen(
                ViewMotivesScreen(
                    command,
                    user,
                    it.deferEdit(),
                    character
                )
            )
        }

        when (answerPhoneResponse) {
            is CharacterUseItemResponse.Success.AnswerCall.Success -> {
                hook.editOriginal(
                    MessageEdit {
                        embed {
                            title = "Seu Celular"

                            description = when (val call = answerPhoneResponse.call) {
                                is PhoneCall.OddCall0 -> buildString {
                                    appendLine("Seu clarividente está sentindo fortes vibrações vindo de seu bolso. Planeje cuidadosamente seus próximos investimentos financeiros.")
                                }
                                is PhoneCall.OddCall1 -> buildString {
                                    appendLine("O nosso sismógrafo está capturando vibrações intensas vindo da sua coluna. Você pode ter algum problema estrutural.")
                                }
                                is PhoneCall.OddCall2 -> buildString {
                                    appendLine("O seu destino está sendo escrito. É melhor ler as letras com atenção para não perder o rumo certo.")
                                }
                                is PhoneCall.OddCall3 -> buildString {
                                    appendLine("Sua presença foi notada. Eles estão a caminho.")
                                }
                                is PhoneCall.OddCall4 -> buildString {
                                    appendLine("Eles estão chegando. É melhor pensar duas vezes antes de abrir a porta...")
                                }
                                is PhoneCall.OddCall5 -> buildString {
                                    appendLine("A conexão foi estabelecida. Não tente desfazer o que foi iniciado.")
                                }
                                is PhoneCall.OddCall6 -> buildString {
                                    appendLine("Os seus pesadelos estão mais pertos do que você imagina. Feche as cortinas antes de dormir.")
                                }
                                is PhoneCall.OddCall7 -> buildString {
                                    appendLine("Você sonha como eu sonho?")
                                }
                                is PhoneCall.OddCall8 -> buildString {
                                    appendLine("Você está sendo observado. Eles sabem o que você fez em 1999.")
                                }
                                is PhoneCall.OddCall9 -> buildString {
                                    appendLine("Seu astrólogo sugeriu que você evite qualquer tipo de espelho hoje.")
                                }
                                is PhoneCall.OddCall10 -> buildString {
                                    appendLine("Você deseja que humanos fossem reais?")
                                }
                                is PhoneCall.OddCall11 -> buildString {
                                    appendLine("Você já testou suas habilidades em nadar no escuro?")
                                }
                                is PhoneCall.OddCall12 -> buildString {
                                    appendLine("O vento mudou de direção. Seus planos para o fim de semana também mudarão.")
                                }
                                is PhoneCall.OddCall13 -> buildString {
                                    appendLine("A ligação foi interrompida, mas eles já sabem o seu endereço.")
                                }
                                is PhoneCall.OddCall14 -> buildString {
                                    appendLine("Há algo embaixo do capacho da sua porta. Não abra até o amanhecer.")
                                }
                                is PhoneCall.OddCall15 -> buildString {
                                    appendLine("Número errado. Desculpe.")
                                }
                                is PhoneCall.OddCall16 -> buildString {
                                    appendLine("Eles estão ouvindo suas conversas. Melhor mudar de assunto.")
                                }
                                is PhoneCall.OddCall17 -> buildString {
                                    appendLine("O saiyajin chegou a terra. Faça os preparativos.")
                                }
                                is PhoneCall.OddCall18 -> buildString {
                                    appendLine("A Loritta não é um ser humano. Melhor tratar ela com carinho para você não virar saudade.")
                                }
                                is PhoneCall.OddCall19 -> buildString {
                                    appendLine("Seus sonhos estão se tornando realidade, mas talvez não da maneira que você espera.")
                                }
                                is PhoneCall.OddCall20 -> buildString {
                                    appendLine("Seu próximo passo será crucial. O destino está de olho.")
                                }
                                is PhoneCall.OddCall21 -> buildString {
                                    appendLine("Seus embustes falharam. Diga a Schrödinger que estou vivo.")
                                }
                                is PhoneCall.OddCall22 -> buildString {
                                    appendLine("Se a porta da sua casa estiver aberta nesta noite, o seu gato amanhecerá calvo.")
                                }
                                is PhoneCall.OddCall23 -> buildString {
                                    appendLine("Você joga papel higiênico na privada?")
                                }
                                is PhoneCall.OddCall24 -> buildString {
                                    appendLine("O demônio azul de garruncha está por perto. Melhor olhar para a parede.")
                                }
                                is PhoneCall.OddCall25 -> buildString {
                                    appendLine("O que é isso em cima da sua cabeça?")
                                }
                                is PhoneCall.OddCall26 -> buildString {
                                    appendLine("Quantos anos tem a água que você está bebendo?")
                                }
                                is PhoneCall.OddCall27 -> buildString {
                                    appendLine("Você é a Antonymph da Internet?")
                                }

                                is PhoneCall.SonhosReward.SonhosRewardCall0 -> buildString {
                                    appendLine("Aquela empresa de criptomoedas que você investiu não estava blefando sobre os lucros. Você ganhou ${call.sonhosReward} sonhos.")
                                }
                                is PhoneCall.SonhosReward.SonhosRewardCall1 -> buildString {
                                    appendLine("Um vídeo mostrando você realizando atitudes deselegantes durante a sua festa de formatura viralizou na internet. Ninguém te reconheceu no vídeo, mas uma empresa conseguiu garantir seus royalties. Você ganhou ${call.sonhosReward} sonhos.")
                                }
                                is PhoneCall.SonhosReward.SonhosRewardCall2 -> buildString {
                                    appendLine("A empresa de transporte finalmente te retornou sobre o reembolso da sua carteira de estudante. Você ganhou ${call.sonhosReward} sonhos.")
                                }
                                is PhoneCall.SonhosReward.SonhosRewardCall3 -> buildString {
                                    appendLine("O bug do coin flip que você denunciou era realmente real. Você ganhou ${call.sonhosReward} sonhos por ter reportado o bug.")
                                }
                                is PhoneCall.SonhosReward.SonhosRewardCall4 -> buildString {
                                    appendLine("As fotos da sua viagem para Marrocos foram escolhidas para serem publicadas na revista \"Viajantes\". Você ganhou ${call.sonhosReward} sonhos.")
                                }
                                is PhoneCall.SonhosReward.SonhosRewardCall5 -> buildString {
                                    appendLine("Seu contador achou uma brecha na legislação e conseguiu uma restituição no seu imposto de renda. Você ganhou ${call.sonhosReward} sonhos.")
                                }
                                is PhoneCall.SonhosReward.SonhosRewardCall6 -> buildString {
                                    appendLine("Aquele processo judicial que você esqueceu finalmente foi resolvido a seu favor. Você ganhou ${call.sonhosReward} sonhos em indenizações.")
                                }
                                is PhoneCall.SonhosReward.SonhosRewardCall7 -> buildString {
                                    appendLine("Um patrocinador misterioso gostou dos seus talentos. Você ganhou ${call.sonhosReward} sonhos.")
                                }
                                is PhoneCall.SonhosReward.SonhosRewardCall8 -> buildString {
                                    appendLine("Aquele projeto freelance do website para o \"Curso de Magia para Gatos\" que você fez há anos finalmente foi pago, e o melhor de tudo... com juros! Você ganhou ${call.sonhosReward} sonhos.")
                                }
                                is PhoneCall.SonhosReward.SonhosRewardCall9 -> buildString {
                                    appendLine("O seu aplicativo de cashback deu um cupom de dinheiro grátis. Você ganhou ${call.sonhosReward} sonhos.")
                                }
                                is PhoneCall.SonhosReward.SonhosRewardCall10 -> buildString {
                                    appendLine("O consultório do seu psicólogo fez um sorteio com os clientes, e você foi o vencedor! Você ganhou ${call.sonhosReward} sonhos.")
                                }
                                is PhoneCall.SonhosReward.SonhosRewardCall11 -> buildString {
                                    appendLine("A sua amiga de infância se deu bem na vida e decidiu ajudar a plebe. Você ganhou ${call.sonhosReward} sonhos.")
                                }
                                is PhoneCall.SonhosReward.SonhosRewardCall12 -> buildString {
                                    appendLine("O seu amigo de infância acredita no seu potencial e decidiu te ajudar. Você ganhou ${call.sonhosReward} sonhos.")
                                }
                                is PhoneCall.SonhosReward.SonhosRewardCall13 -> buildString {
                                    appendLine("Um dos serviços de streaming que você assina fechou as portas. Você ganhou ${call.sonhosReward} sonhos pela inconveniência.")
                                }
                                is PhoneCall.SonhosReward.SonhosRewardCall14 -> buildString {
                                    appendLine("A sua postagem sobre \"Eu amo o jato gelado de bidês\" teve uma repercussão tão grande na internet que uma fabricante decidiu te dar um mimo. Você ganhou ${call.sonhosReward} sonhos.")
                                }
                                is PhoneCall.SonhosReward.SonhosRewardCall15 -> buildString {
                                    appendLine("Olá ${character.name}, você está no ar! Parabéns, pois você acaba de ganhar ${call.sonhosReward} sonhos da rádio Starry Shores!")
                                }
                                is PhoneCall.SonhosReward.SonhosRewardCall16 -> buildString {
                                    appendLine("O gatinho que você resgatou de cima da árvore era de uma pessoa famosa. Após ela ter sido cobrada pela imprensa, ela decidiu fazer bonito e te deu ${call.sonhosReward} sonhos por ter resgatado o bichano.")
                                }
                                is PhoneCall.SonhosReward.SonhosRewardCall17 -> buildString {
                                    appendLine("Aquele furry daquela Furcon que você foi, que você ajudou com o zíper emperrado da fursuit dele, decidiu retribuir a sua ajuda. Você ganhou ${call.sonhosReward} sonhos.")
                                }
                                is PhoneCall.SonhosReward.SonhosRewardCall18 -> buildString {
                                    appendLine("O contato que você deu para o seu amigo fez ele conseguir um emprego em uma das empresas mais cobiçadas de Starry Shores, e agora ele quer te ajudar também. Você ganhou ${call.sonhosReward} sonhos.")
                                }
                                is PhoneCall.SonhosReward.SonhosRewardCall19 -> buildString {
                                    appendLine("A dica que você deu para a polícia que o meliante morava em \"Xíque-Xíque, Bahia\" realmente estava correta. Você ganhou ${call.sonhosReward} sonhos pela ajuda.")
                                }
                                is PhoneCall.SonhosReward.SonhosRewardCall20 -> buildString {
                                    appendLine("O cancelamento que você fez de um influencer famoso te rendeu entrevistas com vários jornais famosos. Como recompensa pelo tempo gasto, você ganhou ${call.sonhosReward} sonhos.")
                                }
                                is PhoneCall.SonhosReward.SonhosRewardCall21 -> buildString {
                                    appendLine("Sua querida tia, aquela muito rica mesmo, precisa se desfazer de parte de sua fortuna rapidamente. Você ganhou ${call.sonhosReward} sonhos.")
                                }
                                is PhoneCall.SonhosReward.SonhosRewardCall22 -> buildString {
                                    appendLine("Você ganhou o concurso do Cereal do Gessy™! Você ganhou ${call.sonhosReward} sonhos.")
                                }
                            }

                            thumbnail = "https://cdn.discordapp.com/attachments/739823666891849729/1291095586015285268/phone_test.png?ex=66fed9f7&is=66fd8877&hm=04157021b599f10f099d5d4d8b829632f1d723e4611225ced93a642378ef35c3&"
                        }

                        actionRow(viewMotivesButton)
                    }
                ).setReplace(true).await()
            }
            CharacterUseItemResponse.Success.AnswerCall.NoCall -> {
                hook.editOriginal(
                    MessageEdit {
                        embed {
                            title = "Seu Celular"

                            description = buildString {
                                appendLine("Você demorou demais para atender a ligação...")
                            }
                        }

                        actionRow(viewMotivesButton)
                    }
                ).setReplace(true).await()
            }

            CharacterUseItemResponse.Success.AnswerCall.YouCantAnswerThePhoneWhileSleeping -> {
                hook.editOriginal(
                    MessageEdit {
                        embed {
                            title = "Seu Celular"

                            description = buildString {
                                appendLine("Você não pode atender o seu telefone enquanto está dormindo!")
                            }
                        }

                        actionRow(viewMotivesButton)
                    }
                ).setReplace(true).await()
            }
        }
    }
}