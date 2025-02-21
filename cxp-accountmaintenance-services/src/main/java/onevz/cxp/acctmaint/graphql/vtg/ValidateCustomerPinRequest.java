package onevz.cxp.acctmaint.graphql.vtg;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "ValidateCustomerPinRequest", description = "This request object contains Customer acctNo, CustomerId...")
public class ValidateCustomerPinRequest {
	private String customerId;
	private String accountNo;
	private String mtn;
	private String spiEncryptedData;
	private String spiEncryptedFormat;
	private String phaseId;
	private String keyId;
}
