package br.ce.wcaquino.rest.tests.refact;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

import org.junit.Test;

import br.ce.wcaquino.rest.core.BaseTest;
import br.ce.wcaquino.rest.tests.entities.Movimentacao;
import br.ce.wcaquino.rest.utils.BarrigaUtils;
import br.ce.wcaquino.rest.utils.DataUtils;

public class MovimentacaoTest extends BaseTest{
	
	@Test
	public void deveInserirMovimentacaoComSucesso() {
		
		Movimentacao movimentacao = getMovimentacaoValida();
		
		given()
			.body(movimentacao)
		.when()
			.post("/transacoes")
		.then()
			.log().all()
		;
		
	}
	
	@Test
	public void naoDeveInserirMovimentacaoComDataFutura() {
		
		Movimentacao movimentacao = getMovimentacaoValida();
		movimentacao.setData_transacao(DataUtils.getDataDiferencaDias(2));
			
		given()
			.body(movimentacao)
		.when()
			.post("/transacoes")
		.then()
			.log().all()
			.statusCode(400)
			.body("$", hasSize(1)) // Quantidade de mensagens = 1
			.body("msg", hasItem("Data da Movimentação deve ser menor ou igual à data atual"))
		;
	}
	
	@Test
	public void naoDeveRemoverContaComMovimentacoes() {
		
		given()
			.pathParam("id", BarrigaUtils.getIdDaContaPeloNome("Conta com movimentacao"))
		.when()
			.delete("/contas/{id}")
		.then()
			.statusCode(500)
			.body("constraint", is("transacoes_conta_id_foreign"))
		;
		
	}
	
	@Test
	public void deveRemoverMovimentacao() {
		
		given()
			.pathParam("id", BarrigaUtils.getIdDaMovimentacaoPelaDescricao("Movimentacao para exclusao"))
		.when()
			.delete("/transacoes/{id}")
		.then()
			.statusCode(204)
		;
		
	}

	@Test
	public void deveValidarCamposObrigatoriosNaValidacao() {
		given()
			.body("{}")
		.when()
			.post("/transacoes")
		.then()
			.log().all()
			.statusCode(400)
			.body("$", hasSize(8)) // Quantidade de mensagens = 8
			.body("msg", hasItems(
					"Data da Movimentação é obrigatório",
					"Data do pagamento é obrigatório",
					"Descrição é obrigatório",
					"Interessado é obrigatório",
					"Valor é obrigatório",
					"Valor deve ser um número",
					"Conta é obrigatório",
					"Situação é obrigatório"
				))
		;
	}
	
	private Movimentacao getMovimentacaoValida() {
		Movimentacao movimentacao = new Movimentacao();
		movimentacao.setConta_id(BarrigaUtils.getIdDaContaPeloNome("Conta para movimentacoes"));
		movimentacao.setUsuario_id(52851);
		movimentacao.setDescricao("Descrição da movimentação");
		movimentacao.setEnvolvido("Envolvido na movimentação");
		movimentacao.setTipo("REC");
		movimentacao.setData_transacao(DataUtils.getDataDiferencaDias(-1));
		movimentacao.setData_pagamento(DataUtils.getDataDiferencaDias(5));
		movimentacao.setValor(100f);
		movimentacao.setStatus(true);
		return movimentacao;
	}
	
}
