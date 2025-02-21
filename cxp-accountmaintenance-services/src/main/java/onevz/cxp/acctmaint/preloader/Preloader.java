package onevz.cxp.acctmaint.preloader;

import java.util.concurrent.CompletableFuture;

public class Preloader {
	
	
	public CompletableFuture<Void> preloadDataLoaders(CustomerDomainRequest customerDomainRequest, final DataFetchingEnvironment environment, 
			CompletableFuture<CXPDomainResponse<onevz.domain.wsclient.model.generated.customer.Data>> customer,
			CompletableFuture[] wirelineProfiles) {
		
		CXPGraphQLContext context = ((CXPGraphQLContext)environment.getGraphQlContext().get(CXPGQLConstants.CXP_GRAPHQL_CONTEXT));
		CompletableFuture<Void> baseLevelResponse = CompletableFuture.allOf(customer,mtn,address);
		return baseLevelResponse.thenCompose(baseResp -> {
			RequestAccessContext.updateRequestData(context.getReactorContext().get(FeatureFlagConstants.CXP_REQUEST));
			CXPDomainResponse<onevz.domain.wsclient.model.generated.customer.Data> customerManagementResponse = new CXPDomainResponse<>();
			List<MobileTelephoneNumber> mtns = null;
			try {
				customerManagementResponse = customer.get();
				environment.getGraphQlContext().put(CUSTOMER_MANAGEMENT_RESPONSE, customerManagementResponse);
				if(mtn != null && mtn.get() != null && mtn.get().getData() != null && CollectionUtilities.isNotEmptyOrNull(mtn.get().getData().getMtns())) {
					mtns = mtn.get().getData().getMtns();
				}
			}catch(InterruptedException | ExecutionException e) {
				Thread.currentThread().interrupt();
				log.error(e.getMessage());
			}
		});
	}
}
