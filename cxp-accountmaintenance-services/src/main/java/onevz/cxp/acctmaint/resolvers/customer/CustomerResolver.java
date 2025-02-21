package onevz.cxp.acctmaint.resolvers.customer;

import org.apache.catalina.util.RequestUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import onevz.cxp.acctmaint.preloader.PreloaderV3;
public class CustomerResolver implements CXPGQLResolver<Customer> {
	
	@Autowired
	private PreloaderV3 preloaderV3;
	
	private static final Logger logger = LoggerFactory.getLogger(CustomerResolver.class);
	
	public CompletionStage<Customer> getCustomer(CustomerRequestType customerRequestType, DataFetchingEnvironment environment) {
		Map<String, Object> externalResponseMap = new LinkedHashMap<>();
		GraphQLContext.class.cast(environment.getGraphQLContext()).putAll(externalResponseMap);
		CXPGraphQLContext context = ((CXPGraphQLContext)environment.getGraphQLContext().get(CXPGQLConstants.CXP_GRAPHQL_CONTEXT));
		if(flowHelper.usePreloaderV3(customerRequestType)) {
			return preloaderV3.preloadDomainFromCustomerCacheDomain(customerRequestType, environment, isCustomerTypeRouterMigrationEnabled());
		}
		
		CustomerDomainRequest request = RequestUtil.getCustomerDomainRequest(customerRequestType);
	}
}
