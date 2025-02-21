package onevz.cxp.acctmaint.preloader;

import java.util.concurrent.CompletableFuture;
import onevz.cxp.acctmaint.preloader.Preloader;

import org.springframework.beans.factory.annotation.Autowired;

public class PreloaderV3 {
	
	@Autowired
	private CustomerInfoHelper customerInfoHelper;
	
	@Autowired
	private Preloader preloaderV1;
	
	public CompletableFuture<Customer> preloadDomainFromCustomerCacheDomain(CustomerRequestType customerRequestType, DataFetchingEnvironment environment, boolean customerTypeRouterMigrationEnabled) {
		CompletableFuture<Boolean> firstLevelResponse = RequestUtil.b2bRestrictionResponse(customerRequestType, environment, customerTypeRouterMigrationEnabled);
		return firstLevelResponse.thenCompose(firstLevelResp -> {
			CustomerDomainRequest customerDomainRequest = RequestUtil.getCustomerDomainRequest(customerRequestType);
			CustomerCacheDomainRequest customerCacheDomainRequest = getCustomerCacheDomainRequest(customerRequestType, environment);
			DataLoader<CustomerCacheDomainRequest, CXPDomainResponse<onevz.domain.wsclient.model.generated.customerCache.Data>> customerCacheDSDataLoader = environment.getDataLoader(CUSTOMER_CACHE_DS_DATALOADER);
			CompletableFuture<CXPDomainResponse<Data>> customerDomainResponse = customerCacheDSDataLoader.load(RequestUtil.getCustomerDomainRequest(customerRequestType));
			customerCacheDSDataLoader.dispatch();
			
			CompletableFuture<Void> response = preloaderV1.preloadDataLoaders(customerDomainRequest, environment, customerDomainResponse, new CompletableFuture[1]);
			return response.thenApply(v -> {
				CXPDomainResponse<Data> customerResponse = environment.getGraphQlContext().get(CUSTOMER_MANAGEMENT_RESPONSE);
				return customerInfoHelper.getCustomerProfileInfo(customerResponse,customerDomainRequest);
			});
		});
	}
}
