package br.ce.wcaquino.rest.tests.refact;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

import org.junit.Test;

import br.ce.wcaquino.rest.core.BaseTest;
import br.ce.wcaquino.rest.utils.BarrigaUtils;

public class ContasTest extends BaseTest{
	
	@Test
	public void deveIncluirUmaContaComSucesso() {
	
		System.out.println("Incluir");
		//Incluir conta com sucesso
		given()
			.body("{\"nome\" : \"Conta inserida\"}")
		.when()
			.post("/contas")
		.then()
			.statusCode(201)
		;
		
	}

	@Test
	public void deveAlterarContaComSucesso() {
		System.out.println("Alterar");
		given()
			.body("{\"nome\" : \"Conta alterada\"}")
			.pathParam("id", BarrigaUtils.getIdDaContaPeloNome("Conta para alterar"))
		.when()
			.put("/contas/{id}")
		.then()
			.log().all()
			.statusCode(200)
			.body("nome", is("Conta alterada"))
		;
	}

	@Test
	public void naoDeveAlterarContaComNomeRepetido() {
		System.out.println("Não deve alterar Conta Com Nome Repetido");
		given()
			.body("\"nome\" : \"Conta mesmo nome\"")
		.when()
			.post("/contas")
		.then()
			.log().all()
			.statusCode(400)
//			.body("error", is("Já existe uma conta com esse nome!"))
		;
	}

}
