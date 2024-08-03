package br.ce.wcaquino.rest.tests;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import br.ce.wcaquino.rest.core.BaseTest;
import br.ce.wcaquino.rest.tests.entities.Movimentacao;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class BarrigaTest extends BaseTest{

	private String TOKEN = "";
	
	@Before
	public void login() {
		Map<String, String> login = new HashMap<>();
		login.put("email", "ttt@t.com");
		login.put("senha", "123456");
		
		//Pegar Token
		TOKEN = 
			given()
				.body(login)
			.when()
				.post("/signin")
			.then()
				.statusCode(200)
				.extract().path("token")
			;
		
	}
	
	@Test
	public void a_naoDeveAcessarAPISemToken() {
		
		given()
		.when()
			.get("/contas")
		.then()
			.statusCode(401)
		;
	}

	@Test
	public void b_deveIncluirUmaContaComSucesso() {
	
		//Incluir conta com sucesso
		given()
			.header("Authorization", "JWT " + TOKEN)
//			.header("Authorization", "beared" + token)
			.body("{\"nome\" : \"conta qualquer\"}")
		.when()
			.post("/contas")
		.then()
			.statusCode(201)
		;
	}
	
	@Test
	public void c_deveAlterarContaComSucesso() {
		given()
			.header("Authorization", "JWT " + TOKEN)
			.body("{\"nome\" : \"conta alterada 1\"}")
		.when()
			.put("/contas/2204992")
		.then()
			.log().all()
			.statusCode(200)
			.body("nome", is("conta alterada 1"))
		;
	}
	
	@Test
	public void d_naoDeveAlterarContaComNomeRepetido() {
		given()
			.header("Authorization", "JWT " + TOKEN)
			.body("{\"nome\" : \"conta alterada 1\"}")
		.when()
			.post("/contas")
		.then()
			.log().all()
			.statusCode(400)
			.body("error", is("Já existe uma conta com esse nome!"))
		;
	}
	
	@Test
	public void e_deveInserirMovimentacaoComSucesso() {
		
		Movimentacao movimentacao = getMovimentacaoValida();
		
		given()
			.header("Authorization", "JWT " + TOKEN)
			.body(movimentacao)
		.when()
			.post("/transacoes")
		.then()
			.log().all()
			.statusCode(201)
		;
	}

	@Test
	public void f_deveValidarCamposObrigatoriosNaValidacao() {
		
		given()
			.header("Authorization", "JWT " + TOKEN)
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

	@Test
	public void g_naoDeveInserirMovimentacaoComDataFutura() {
		
		Movimentacao movimentacao = getMovimentacaoValida();
		movimentacao.setData_transacao("01/01/2030");
			
		given()
			.header("Authorization", "JWT " + TOKEN)
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
	public void h_naoDeveRemoverContaComMovimentacoes() {
		
		given()
			.header("Authorization", "JWT " + TOKEN)
		.when()
			.delete("/contas/2204992")
		.then()
			.statusCode(500)
			.body("constraint", is("transacoes_conta_id_foreign"))
		;
		
	}
	
	@Test
	public void i_deveCalcularSaldoContas() {
		
		given()
			.header("Authorization", "JWT " + TOKEN)
		.when()
			.get("/saldo")
		.then()
			.statusCode(200)
			.body("find{it.conta_id == 2204992}.saldo", is("569.00"))
		;
		
	}

	@Test
	public void j_deveRemoverMovimentacao() {
		
		given()
			.header("Authorization", "JWT " + TOKEN)
		.when()
			.delete("/transacoes/2079357")
		.then()
			.statusCode(204)
		;
		
	}
	
	private Movimentacao getMovimentacaoValida() {
		Movimentacao movimentacao = new Movimentacao();
		movimentacao.setConta_id(2204992);
		movimentacao.setUsuario_id(52851);
		movimentacao.setDescricao("Descrição da movimentação");
		movimentacao.setEnvolvido("Envolvido na movimentação");
		movimentacao.setTipo("REC");
		movimentacao.setData_transacao("01/01/2010");
		movimentacao.setData_pagamento("10/05/2010");
		movimentacao.setValor(100f);
		movimentacao.setStatus(true);
		return movimentacao;
	}
}
