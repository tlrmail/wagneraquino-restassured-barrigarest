package br.ce.wcaquino.rest.tests.refact;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

import org.junit.Test;

import br.ce.wcaquino.rest.core.BaseTest;
import br.ce.wcaquino.rest.utils.BarrigaUtils;
import io.restassured.RestAssured;

public class AuthTest extends BaseTest{
	
	@Test
	public void deveCalcularSaldoContas() {
		
		given()
		.when()
			.get("/saldo")
		.then()
			.statusCode(200)
			.body("find{it.conta_id == " + BarrigaUtils.getIdDaContaPeloNome("Conta para saldo") + "}.saldo", is("534.00"))
		;
		
	}

}
