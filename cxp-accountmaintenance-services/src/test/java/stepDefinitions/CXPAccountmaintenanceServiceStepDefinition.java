package stepDefinitions;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.java.en.And;
import io.cucumber.spring.CucumberContextConfiguration;
import com.acctmaint.CucumberConfigTest;

@CucumberContextConfiguration
@SpringBootTest(classes = CucumberConfigTest.class)
public class CXPAccountmaintenanceServiceStepDefinition {
	
	@Autowired
	public APIStepDefinitions apiStepDefinitions;
	
	// Building request
	@Given("request builder creation for {string}")
	public void request_builder_creation(String apiName) throws IOException {
		apiStepDefinitions.request_builder_creation(apiName);
	}
	
	// Adding headers without token header
	@Given("add headers to {string} request")
	public void add_headers_to_request(String apiName) {
		apiStepDefinitions.add_headers_to_request(apiName);
	}
	
	//removing specific headers from required header list without token header
	@Given("add headers to {string} request without {string}")
	public void add_headers_to_request_without_header(String apiName, String headerName) {
		apiStepDefinitions.add_headers_to_request_without_header(apiName, headerName);
	}
	
	// Adding headers with sso_jwt token
	@Then("add required headers along with token header to {string} request")
	public void add_required_headers_along_with_token_header_to_request(String apiName) {
		apiStepDefinitions.add_required_headers_along_with_token_header_to_request(apiName);
	}
	
	// adding request for graphql API
	@When("{string} is added to request for graphql {string} API")
	public void is_added_to_request_for_graphql_api(String payloadFilePath, String apiName)
		throws IOException, ProcessingException, InterruptedException {
		apiStepDefinitions.is_added_to_request_for_graphql_api(payloadFilePath, apiName);
	}
	
}
