package br.ce.wcaquino.rest.tests.refact.suite;

import static io.restassured.RestAssured.given;

import java.util.HashMap;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import br.ce.wcaquino.rest.core.BaseTest;
import br.ce.wcaquino.rest.tests.refact.AuthTest;
import br.ce.wcaquino.rest.tests.refact.ContasTest;
import br.ce.wcaquino.rest.tests.refact.MovimentacaoTest;
import br.ce.wcaquino.rest.tests.refact.SaldoTest;
import io.restassured.RestAssured;

@RunWith(Suite.class)
@SuiteClasses({
	AuthTest.class,
	ContasTest.class,
	MovimentacaoTest.class,
	SaldoTest.class
})
public class BarrigaRestSuite extends BaseTest{

	@BeforeClass
	public static void login() {
		System.out.println("Before conta");
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
		
		RestAssured.get("/reset").then().statusCode(200);
	}

}
