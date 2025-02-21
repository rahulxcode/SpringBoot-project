package onevz.cxp.acctmaint.graphql.vtg;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "AddressValidateResponse", description = "This response object returns either customer present for the given customerId and pin.")
public class ValidateCustomerPinResponse {
	private Boolean customerFound;
	private String customerId;
	private String mtn;
}
