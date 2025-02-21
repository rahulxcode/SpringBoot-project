package onevz.cxp.acctmaint.graphql.services.impl;

import java.util.Objects;

import onevz.cxp.acctmaint.preloader.CXPDomainResponse;

public class CustomerInfoHelper {
	
	
	@Override
	public Customer getCustomerProfileInfo(CXPDomainResponse<onevz.domain.wsclient.model.generated.customer.Data> customerresponse, CustomerDomainRequest request) {
		if(customerresponse != null && customerresponse.getData() != null && customerresponse.getData().getCustomer() !=null) {
			Customer customer = new Customer();
			onevz.domain.wsclient.model.generated.customer.Customer domainCustomerRes = customerresponse.getData().getCustomer();
			populateCustomerInfo(request, customer, domainCustomerRes);
			return customer;
		}
		return null;
	}
	
	private Customer populateCustomerInfo(CustomerDomainRequest request, Customer customer, onevz.domain.wsclient.model.generated.customer.Customer domainCustomerRes) {
		if(Objects.nonNull(customer) && Objects.nonNull(domainCustomerRes)) {
			if(featureFlag.isFeatureFlagEnabled(onevz.cxp.customer.constants.FeatureFlagConstants.MARKETING_HOME_APPLICATION,
					FeatureFlagConstants.CONFIG_PROFILE_SALES, FeatureFlagConstants.EVOLU_ATTRIBUTE_USER_AGE_BUCKET, false)) {
				if(domainCustomerRes.getUserAgeBucket() != null) {
					customer.setUserAgeBucket(domainCustomerRes.getUserAgeBucket()); 
				}else {
					customer.setUserAgeBucket("Undefined");
				}
			}
			customer.setCustomerId(domainCustomerRes.getId());
			customer.setBirthdate(domainCustomerRes.getBirthDate());
			customer.setSsn(domainCustomerRes.getSsn());
			customer.setCustomerPin(domainCustomerRes.getPin());
			customer.setFederalTaxId(domainCustomerRes.getFederalTaxId());
			customer.setOriginalEffDate(domainCustomerRes.getOriginalEffDate());
			customer.setStartDate(domainCustomerRes.getStartDate());
			StatusCode statusCode = new StatusCode();
			customer.setStatusCode(statusCode);
			customer.setActiveMTNCountForSSN(domainCustomerRes.getActiveMtnCount());
			customer.setSubAccountDetails(getSubAccountDetails(domainCustomerRes));
			customer.setPinLastModifiedDate(domainCustomerRes.getPinLastModifiedDate());
			return customer;
		}
		return customer;
	}
}
