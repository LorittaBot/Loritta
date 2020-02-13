package com.mrpowergamerbr.loritta.utils.correios

data class CorreiosResponse(
		val versao: String,
		val quantidade: Int,
		val pesquisa: String,
		val resultado: String,
		val objeto: List<CorreiosObjeto>
)

data class CorreiosObjeto(
		val numero: String,
		val sigla: String,
		val nome: String,
		val categoria: String,
		val evento: List<CorreiosEvento>
)

data class CorreiosEvento(
		val tipo: String,
		val status: String,
		val data: String,
		val hora: String,
		val criacao: String,
		val descricao: String,
		val recebedor: Any,
		val unidade: CorreiosUnidade
)

data class CorreiosUnidade(
		val local: String,
		val codigo: String,
		val cidade: String,
		val uf: String,
		val sto: String,
		val tipounidade: String,
		val cepDestino: String,
		val prazoGuarda: String,
		val diasUteis: String,
		val dataPostagem: String,
		val endereco: CorreiosEndereco
)

data class CorreiosEndereco(
		val codigo: String,
		val cep: String,
		val logradouro: String,
		val numero: String,
		val localidade: String,
		val uf: String,
		val bairro: String,
		val latitude: String,
		val longitude: String
)