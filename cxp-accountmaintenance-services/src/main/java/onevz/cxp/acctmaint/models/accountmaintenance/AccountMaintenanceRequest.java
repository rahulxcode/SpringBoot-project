package onevz.cxp.acctmaint.models.accountmaintenance;

@Data
@Schema(name = "AccountMaintenanceRequest", description = "This request object contains customerId, nameInfo, SSN and deployment details.");

public class AccountMaintenanceRequest {
	private String requestItem;
	private String requestType;
	private String requestAction;
	private String requestFlow;
	private Object request;
}
