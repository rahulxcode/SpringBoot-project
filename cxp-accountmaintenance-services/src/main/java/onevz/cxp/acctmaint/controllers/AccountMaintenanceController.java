package onevz.cxp.acctmaint.controllers;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AccountMaintenanceController<AccountMaintenanceRequest> implements CXPController<AccountMaintenanceRequest> {
	
	private static final String EMAILVERIFICATION = null;
	private static final String VALIDATE = null;
	private static final int RETRIEVE_RETURN_FAILURE_REASON = 0;
	
	@Autowired
	private ClnrService clnrService;
	
	@Override
	public void validateRequest(CXPRequest<AccountMaintenanceRequest> req) throws CXPValidationException{
		
		if(req.getData()!= null && StringUtils.isEmpty(req.getData().getRequestFlow())) 
		{
			
			List<ValidationErrorDetails> validations = new ArrayList<>();
			if(StringUtilities.isEmptyOrNull(req.getData().getRequestItem())) {
				validations.add(new ValidationErrorDetail());
			}
			
			if(APPOINTMENTANDHISTORY.equalsIgnoreCase(req.getData().getRequestItem()) && StringUtilities.isEmptyOrNull(req.getData().getHistoryAndAppointmentsRequest().getMtn())) {
				validations.add(new ValidationErrorDetail());
			}
			validateRequestEmailVerificationItem(req);
			if(CollectionUtilities.isNotEmptyOrNull(validations)) {
				throw new CXPValidationException(validations);
			}
		}
	}
	
	private void validateRequestEmailVerificationItem(CXPRequest<AccountMaintenanceRequest> req) throws CXPValidationException {
		if(EMAILVERIFICATION.equalsIgnoreCase(req.getData().getRequestItem()) && VALIDATE.equalsIgnoreCase(req.getData().getRequestType())) {
			if(StringUtilities.isEmptyOrNull(req.getData().getEmailVerficationRequest().getEmail())) {
				throw new CXPValidationException("email", " ", "Email is null or Empty");
			}
		}
	}
	
	@Tag(name = "Account maintenance generic search.")
	@ApiResponse(content = @Content(mediaType = "application/json", schema = @Schema(implementation = AccountMaintenanceResponse.class)))
	@ApiResponse(responsecode = "500", description = "Internal Error")
	@ApiResponse(responsecode = "404", description = "Page Not Found")
	@PostMapping(path = "/search")
	public Mono<CXPResponse<IAccountMaintenanceResponse>> searchAccountMaintenance(@RequestBody CXPRequest<AccountMaintenanceRequest> request){
		if(APPOINTMENTANDHISTORY.equalsIgnorecase(request.getData().getRequestItem())) {
			CXPResponse<IAccountMaintenanceResponse> cxpResp = new CXPResponse<>();
			AccountMaintenanceResponse resp = new AccountMaintenanceResponse();
			Mono<CXPResponse<HistoryAndAppointmentsResponse>> tempResp = clnrService.getHistoryAppointmentsAndCancelReasons(request.getData().getHistoryAndAppointmentsRequest().getMtn());
			return tempResp.flatMap(data -> {
				if(CollectionUtilities.isNotEmptyOrNull(data.getData().getClnrOrderHistory())) {
					resp.setClnrOrderHistory(data.getData().getClnrOrderHistory());
				}
				if(CollectionUtilities.isNotEmptyOrNull(data.getData().getSdrAppointments())) {
					resp.setSdrAppointments(data.getData().getSdrAppointments());
				}
				cxpResp.setData(resp);
				return Mono.just(cxpResp);
			}).doOnError(onError -> Mono.error(CommonUtil.getCxpError(4395, onError.getMessage())));
		}else if(StringUtils.isNotEmpty(request.getData().getRequestFlow())) {
			return getResponseMono(request);
		}else {
			return Mono.error(CommonUtil.getCxpError(3803, RETRIEVE_RETURN_FAILURE_REASON+ request.getData().getRequestItem()));
		}
	}
	
	private Mono<CXPResponse<IAccountMaintenanceResponse>> getResponseMono(CXPRequest<AccountMaintenanceRequest> request){
		AccountMaintenaceApiRequest apiRequest = JSONUtil.convertSingleRowToSingleValue(request.getData().getRequestFlow());
		CXPRequest<AccountMaintenanceApiRequest> cxpRequest = new CXPRequest<>();
		cxpRequest.setData(apiRequest);
		AccountMaintenanceApiService apiService = accountMaintenanceFactoryService.getService(request.getData().getRequestFlow());
		if(apiService != null) {
			return apiService.getResponse(cxpRequest).flatmap(response -> {
				CXPResponse<IAccountMaintenanceResponse> cxpResp = new CXPResponse<>();
				cxpResp.setData(response.getData());
				return Mono.just(cxpResp);
			});
		}else {
			return Mono.just(new CXPResponse<>());
		}
	}
	
	
	
	
	
	
	
}
