package onevz.cxp.acctmaint.services.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ClnrServiceImpl implements ClnrService {
	private static final Logger logger = LoggerFactory.getLogger(ClnrServiceImpl.class);
	
	public Mono<CXPResponse<HistoryAndAppointmentsResponse>> getHistoryAppointmentsAndCancelReasons(String mtn) {
		CXPResponse<HistoryAndAppointmentsResponse> cxpResp = new CXPResponse<>();
		
		if(clnrHistoryVipResponseMono != null  && sdrApptVipResponseMono != null) {
			return Mono.zip(sdrApptVipResponseMono, clnrHistoryVipResponseMono).flatMap(vipResp -> {
				IRetrieveSdrAppointmentsServiceResponse sdrApptVipResp = vipResp.getT1();
				IRetrieveClnrHistoryServiceResponse clnrHistoryVipResp = vipResp.getT2();
				
				HistoryAndAppointmentsResponse historyAndApptCxpResp = new HistoryAndAppointmentsResponse();
				if(clnrHistoryVipResp != null && CollectionUtilities.isNotEmptyOrNull(clnrHistoryVipResp.getClnrOrderHistory())) {
					historyAndApptCxpResp.setClnrOrderHistory(clnrHistoryVipResp.getClnrOrderHistory());
				}
				cxpResp.setData(historyAndApptCxpResp);
				return Mono.just(cxpResp);
			}).doOnError(onError -> Mono.error(getCxpError(4395, onError.getMessage())));
		}
		else {
			return Mono.error(getCxpError(4395, CLNR_HISTORY_SDR_APPOINTMENTS_RETRIEVE_FAILURE));
		}
	}
	
	private SdrAppointmentManagementServiceRequest prepareAndGetCancelRescheduleApptRequest(cancelOrRescheduleAppointmentsRequest request) {
		SdrAppointmentManagementServiceRequest vipRequest = new SdrAppointmentManagementServiceRequest();
		List<CancelRescheduleList> cancelRescheduleList = new ArrayList<>();
		
		request.getCancelRescheduleList().stream().forEach(cxpRequestData -> {
			CancelRescheduleData cancelRescheduleData = new CancelRescheduleData();
			cancelRescheduleData.setLocationCode(cxpRequestData.getLocationCode());
			cancelRescheduleData.setOrdNum(StringUtilities.safeParsingInt(cxpRequestData.getOrderNumber()));
			cancelRescheduleData.setAppointmentDate(cxpRequestData.getAppointmentDate());
			cancelRescheduleData.setCancelReason(cxpRequestData.getCancelReason());
			cancelRescheduleList.add(cancelRescheduleData);
		});
		vipRequest.getCancelRescheduleList().addAll(cancelRescheduleList);
		return vipRequest;
	}
	
	private CXPBusinessException getCxpError(int errorCode, String errorMessage) {
		return new CXPBusinessException(errorCode, errorMessage);
	}
	
	public Mono<CXPResponse<DeviceRepairValidationResponse>> deviceRepairValidation(
			DeviceRepairValidationRequest request) {
		return retrieveCart(request.getCartId()).flatMap(cartResp -> {
			String[] acctNumArr = cartResp.getCartHeader().getFullAccountNumber().split("-");
			String customerId = acctNumArr[0];
			Optional<cartLine> cartLine = Optional.empty();
			return commonUtil.rerouteTypeCheck(customerId, properties.isEnableRouter()).flatMap(fn -> {
				retrieveDeviceProductInfoForCLNR(cartResp, request)
					.flatMap(dmdResponse -> deviceInventoryForCLNR(cartResp, request)
							.flatMap(deviceInventory -> executeDeviceRepairValidation(cartResp, dmdResponse, request, deviceInventory)));
					});
		});
	}
	
	public Mono<CXPDomainResponse<onevz.domain.wsclient.model.generated.device.Data>> deviceInventoryForCLNR(Cart cart, DeviceRepairValidationRequest request) {
		Optional<CartLine> cartLine = Optional.empty();
		if(null != cart && null != cart.getLineDetails() && CollectionUtilities.isNotEmptyOrNull(cart.getLineDetails().getLineInfo())) {
			cartLine = cart.getLineDetails().getLineInfo().stream().filter(line -> null != line.getMtnDetails()
					&& null != line.getMtnDetails().getMobileNumber()
					&& line.getMtnDetails().getMobileNumber().equalsIgnoreCase(request.getMtn())).findAny();
		}
	}
	
	private void setRepairEligibleValue BasedonCondition (DeviceRepairValidationRequest request,
			DeviceRepairValidationResponse response, String warrantyCode, String protectionCode,
			String sdrEligibilityInd, Warranty warranty) {
		
			if (null != warranty && warranty.isDeviceEligibiltyIndicator() && StringUtilities.isNotEmptyOrNulL(sdrEligibilityInd)
			&& "y".equalsIgnoreCase(sdrEligibilityInd)) {
			
				if (Arrays.asList ("N", "I", "W", "T", "B").contains(protectionCode)
						&& Arrays.asList ("Y1", "Y2").contains (warrantyCode) && warranty. isOemIndicator().booleanValue()) {
				
				setRepairEligible (warranty, response, request);
			}else if (Arrays.asList ( "N", "I", "W","T", "B").contains(protectionCode)
					&& Arrays.asList("Y3", "Y4").contains (warrantyCode) && warranty.isVerizonIndicator().booleanValue()) {
				setRepairEligible (warranty, response, request);
			}else if (Arrays.asList("W", "T", "B").contains(protectionCode)
					&& Arrays.asList("N1", "N2","N3","N4", "N5", "N6", "N8").contains (warrantyCode)
					&& warranty.isExtendedIndicator().booleanValue()) {
					
					setRepairEligible (warranty, response, request);
			}else if (Arrays .asList("N", "I", "W","T", "B").contains (protectionCode)
					&& Arrays.asList("N7").contains (warrantyCode) && warranty.isAssurion Indicator().booleanValue()) {
					setRepairEligible (warranty, response, request);
			I
			}else if (Arrays.asList("N", "I").contains(protectionCode)
					&& Arrays.asList ("N1" , "N2", "N3", "N4", "N5", "N6", "N8").contains(warrantyCode) && warranty.isOutofIndicator().booleanValue()) {
				setRepairEligible (warranty, response, request);
			}else {
				response.setRepairEligible (false);
			}
		}else {
			response.setRepairEligible (false);
		}
	}
	
	
	@Override
	public Mono<CXPResponse<ActiveSwitchResponse>> getSimSwapInfo(CXPRequest<ActiveSwitchRequest> activeSwitchRequest) throws CXPException{
		CXPResponse<ActiveSwitchResponse> cxpResp = new CXPResponse<>();
		ActiveSwitchResponse activeSwitchResponse = new ActiveSwitchResponse();
		List<ActiveSwitch> activeSwitchList = new ArrayList<>();
		cxpResp.setData(activeSwitchResponse);
		
		Mono<CXPDomainResponse<onevz.domain.wsclient.model.generated.account.Data>> acctMgmt = accountManagement.getAccountByAccountNumber(activeSwitchRequest.getData().getAccountNo(), CacheOverride.PREFER_CACHE_VALUE)
				.switchIfEmpty(Mono.just(new CXPDomainResponse<onevz.domain.wsclient.model.generated.account.Data>()));
		return acctMgmt.flatMap(acctMgmtResponse -> {
			Optional<Account> fraudAccount = (acctMgmtResponse != null && acctMgmtResponse.getData() != null) ? acctMgmtResponse.getData().getAccounts().stream().filter(acc -> acc !=null && acc.getAccountAttribute() != null) : Optional.empty();
			HashMap<String,String> deviceIdsMap = new HashMap<>();
			List<String> skuIds = new ArrayList<>();
			HashMap<String,String> sorIdsMaps = new HashMap<>();
			Mono<Boolean> retrieveFulFillmentOrderStatus = Mono.just(false);
			if(fraudAccount.isPresent()) {
				activeSwitchResponse.setFraudIndicatorCode(fraudAccount.get().getAccountAttribute().getFraudInfo().getFraudIndicatorCode());
				return Mono.just(cxpResp);
			}
		});
	}
	
	private void settingDisplayNameToActiveSwitchResp(HashMap<String,String> sorIdsMaps, DeviceSkuItem sku,
			ActiveSwitch activeSwitch) {
		String deviceId = null;
		for(Entry<String, String> entry : sorIdsMaps.entrySet()) {
			if(sku.getSorId().equals(entry.getValue())) {
				deviceId = entry.getKey();
				if(deviceId != null & deviceId.equals(activeSwitch.getMtn())) {
					activeSwitch.setDeviceName(sku.getSkuDisplayName());
				}
			}
		}
	}
	
	
}
