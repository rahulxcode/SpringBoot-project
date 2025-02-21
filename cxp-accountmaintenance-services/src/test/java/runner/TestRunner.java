package runner;

import io.cucumber.junit.platform.engine.Constants;
import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.*;

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(key = "stepDefinitions", value = "onevz.cxp.customercare")
@ConfigurationParameter(key = Constants.EXECUTION_DRY_RUN_PROPERTY_NAME, value = "false")

public class TestRunner {
	
}
