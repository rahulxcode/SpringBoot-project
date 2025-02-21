package onevz.cxp.acctmaint.resolvers;

import java.util.concurrent.CompletionStage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import onevz.cxp.acctmaint.resolvers.customer.CustomerResolver;

@Component
public class RootQueryResolver implements CXPRootQueryResolver{
	
	@Autowired
	CustomerResolver customerResolver;
	
	public CompletionStage<ByodValidationInfo> validateBYOD(String deviceId, DataFetchingEnvironment environment) {
		DataLoader<String, ByodValidationInfo> dataLoader = environment.getDataLoader("byodValidationDataLoader");
		return dataLoader.load(deviceId);
	}
	
	public CompletionStage<Customer> customer(String customerId, DataFetchingEnvironment environment) throws CXPException {
		isValidCustomer(customerId);
		CustomerRequestType customerRequestType = new CustomerRequestType();
		customerRequestType.setCustomerId(customerId);
		return customerResolver.getCustomer(customerRequestType, environment);
	}
}
