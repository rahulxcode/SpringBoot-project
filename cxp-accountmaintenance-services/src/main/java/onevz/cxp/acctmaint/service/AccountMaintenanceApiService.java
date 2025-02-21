package onevz.cxp.acctmaint.service;

public class AccountMaintenanceApiService {
	public Mono<AccountMaintenanceResponse<IAccountMaintenanceResponse>> getResponse(CXPRequest<AccountMaintenanceApiRequest> request);
}
