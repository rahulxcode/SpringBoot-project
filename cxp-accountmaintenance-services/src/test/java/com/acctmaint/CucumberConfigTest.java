package com.acctmaint;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CucumberConfigTest {
	
	@Test
	public void ConfigTest() {
		assertDoesNotThrow(() -> new CucumberConfigTest());
	}
	
	@Bean
	public APIStepDefinitions apiStepDefinitions() {
		return new APIStepDefinitions();
	}
}
