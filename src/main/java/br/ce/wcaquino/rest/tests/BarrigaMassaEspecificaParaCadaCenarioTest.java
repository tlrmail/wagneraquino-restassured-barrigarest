package br.ce.wcaquino.rest.tests;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

import java.util.HashMap;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import br.ce.wcaquino.rest.core.BaseTest;
import br.ce.wcaquino.rest.tests.entities.Movimentacao;
import br.ce.wcaquino.rest.utils.DataUtils;
import io.restassured.RestAssured;
import io.restassured.specification.FilterableRequestSpecification;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class BarrigaMassaEspecificaParaCadaCenarioTest extends BaseTest{

	private static String CONTA_NAME = "Conta - " + System.nanoTime();
	
	private static String CONTA_ID;

	private static String MOVIMENTACAO_ID;
	
	@BeforeClass
	public static void login() {
		Map<String, String> login = new HashMap<>();
		login.put("email", "ttt@t.com");
		login.put("senha", "123456");
		
		//Pegar Token
		String TOKEN = 
			given()
				.body(login)
			.when()
				.post("/signin")
			.then()
				.statusCode(200)
				.extract().path("token")
			;
		
		//Configura o Header no token
		RestAssured.requestSpecification.header("Authorization", "JWT " + TOKEN);
	}
	
	@Test
	public void b_deveIncluirUmaContaComSucesso() {
	
		//Incluir conta com sucesso
		String json =  
			given()
	//			.header("Authorization", "beared" + token)
				.body("{\"nome\" : \"" + CONTA_NAME + "\"}")
			.when()
				.post("/contas")
			.then()
				.statusCode(201)
				.extract().asString()
			;
		
		CONTA_ID = json.split(",")[0].split(":")[1];
		
	}
	
	@Test
	public void c_deveAlterarContaComSucesso() {
		given()
			.body("{\"nome\" : \"" + CONTA_NAME + " alterada\"}")
			.pathParam("id", CONTA_ID)
		.when()
			.put("/contas/{id}")
		.then()
			.log().all()
			.statusCode(200)
			.body("nome", is(CONTA_NAME + " alterada"))
		;
	}
	
	@Test
	public void d_naoDeveAlterarContaComNomeRepetido() {
		given()
			.body("\"nome\" : \"" + CONTA_NAME + " alterada\"")
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
		
		String json = 
			given()
				.body(movimentacao)
			.when()
				.post("/transacoes")
			.then()
				.log().all()
				.statusCode(201)
				.extract().asString()
			;
		
		MOVIMENTACAO_ID = json.split(",")[0].split(":")[1];
		
	}

	@Test
	public void f_deveValidarCamposObrigatoriosNaValidacao() {
		
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

	@Test
	public void g_naoDeveInserirMovimentacaoComDataFutura() {
		
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
	public void h_naoDeveRemoverContaComMovimentacoes() {
		
		given()
			.pathParam("id", CONTA_ID)
		.when()
			.delete("/contas/{id}")
		.then()
			.statusCode(500)
			.body("constraint", is("transacoes_conta_id_foreign"))
		;
		
	}
	
	@Test
	public void i_deveCalcularSaldoContas() {
		
		given()
		.when()
			.get("/saldo")
		.then()
			.statusCode(200)
			.body("find{it.conta_id == " + CONTA_ID + "}.saldo", is("100.00"))
		;
		
	}
	
	@Test
	public void j_deveRemoverMovimentacao() {
		
		given()
			.pathParam("id", MOVIMENTACAO_ID)
		.when()
			.delete("/transacoes/{id}")
		.then()
			.statusCode(204)
		;
		
	}

	@Test
	public void l_naoDeveAcessarAPISemToken() {
		
		FilterableRequestSpecification req = (FilterableRequestSpecification) RestAssured.requestSpecification;
		req.removeHeader("Authorization");
		
		given()
		.when()
			.get("/contas")
		.then()
			.statusCode(401)
		;
	}

	
	private Movimentacao getMovimentacaoValida() {
		Movimentacao movimentacao = new Movimentacao();
		movimentacao.setConta_id(Integer.valueOf(CONTA_ID));
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
