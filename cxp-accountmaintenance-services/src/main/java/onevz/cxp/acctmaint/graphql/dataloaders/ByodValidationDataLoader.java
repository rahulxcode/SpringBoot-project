package onevz.cxp.acctmaint.graphql.dataloaders;

import java.util.List;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class ByodValidationDataLoader extends CXPBatchLoaderWithContext<String, ByodValidationInfo> {
	
	private static final String RETRIEVE_APPLELOCK_DETAILS = "RETRIEVE_APPLELOCK_DETAILS";

	private static final String DEVICE_INFORMATION_LOOKUP = "deviceInformationLookup";

	private static Logger logger = LoggerFactory.getLogger(ByodValidationDataLoader.class);
	
	@Autowired
	private DeviceProductManagementService deviceProductManagementService;
	
	@Autowired
	AccountMaintenanceProperties properties;
	
	@Autowired
	DomainServiceUtil domainServiceUtil;
	
	@Autowired
	CommonUtil commonUtil;
	
	@Override
	protected Mono<List<ByodValidationInfo>> loadWithContext(List<String> keys, CXPGraphQlContext context){
		Flux<ByodValidationInfo> urcFlux = Flux.fromIterable(keys)
				.flatMap(this::retrieveByodValidationInfo);
		return urcFlux.collectList();
	}
	
	private Mono<ByodValidationInfo> retrieveByodValidationInfo(String deviceId) {
		
		if(!properties.isEnableDMDDomainCall()) {
			return retrieveDMDBYODValidation(deviceId);
		}else {
			return retrieveDMDDomainBYODValidation(deviceId);
		}
	}
	
	private Mono<CXPDomainResponse<onevz.domain.wsclient.model.generated.deviceproduct.Data>> retrieveDeviceProductinfo(String deviceId) {
		DeviceProductManagementRequest domainRequest = commonUtil.getDeviceProductDetails(deviceId);
		return deviceProductManagementService.getDeviceProductInfo(domainRequest);
	}
	
	private Mono<ByodValidationInfo> retrieveDMDDomainBYODValidation(String deviceId) {
		Mono<CXPDomainResponse<onevz.domain.wsclient.model.generated.deviceproduct.Data>> dmdDomainResponse = retrieveDeviceProductinfo(deviceId);
		return dmdDomainResponse.flatMap(response -> {
			Mono<Boolean> fmipLocked = Mono.just(Boolean.FALSE);
			Mono<Boolean> appleCarrierLock = Mono.just(Boolean.FALSE);
			
			if(response.getData() != null && response.getData().getDevices() != null && response.getData().getDevices().get(0) != null) {
				fmipLocked = validateFmipLock(deviceId);
				appleCarrierLock = validateAppleLock(deviceId);
			} else if(response.getErrors() != null) {
				return Mono.error(new CXPException("Error while Invoking DMD"));
			}
		});
	}
	
	private Mono<ByodValidationInfo> retrieveDMDBYODValidation(String deviceId) {
		Mono<IDMDUniversalResponseBody> dmdUniversalResponse = invokeRetrieveDeviceSimInfoService(deviceId);
		return dmdUniversalResponse.flatMap(response -> {
			Mono<Boolean> fmipLocked = Mono.just(Boolean.FALSE);
			Mono<Boolean> appleCarrierLock = Mono.just(Boolean.FALSE);
			
			if(response.getDeviceInfo().getMfgCode() != null && "APL".equalsIgnoreCase(response.getDeviceInfo().getMfgCode())) {
				fmipLocked = validateFmipLock(deviceId);
				appleCarrierLock = validateAppleLock(deviceId);
			}
			
			Mono<Data> catalogDeviceSku = lookupByDeviceId(response.getDeviceInfo().getLaunchPackageSku());
			
			Mono<Tuple4<IDMDUniversalResponseBody, Boolean, Boolean, Data>> tup4 = Mono.zip(dmdUniversalResponse, fmipLocked, appleCarrierLock, catalogDeviceSku);
			
			return tup4.map(tuple -> {
				IDMDUniversalResponseBody dMDAPIResponse = tuple.getT1();
				Boolean fmipResponse = tuple.getT2();
				Boolean appleLockResponse = tuple.getT3();
				Data deviceSku = tuple.getT4();
				return mergeByodToolTuple(dMDAPIResponse, fmipResponse, appleLockResponse, deviceSku);
			});
		});
	}
	
	private Mono<Boolean> validateFmipLock(String deviceId) {
		Mono<onevz.cxp.wsclient.posservicesws.interfaces.deviceRecycleProcessing.deviceValidation.IDeviceValidationServiceResponse> fmipLockedServiceResp = invokeFmipLockedService(deviceId);
		return fmipLockedServiceResp.flatMap(fmipLockedResp -> {
			if(fmipLockedResp != null && CollectionUtilities.isNotEmptyOrNull(fmipLockedResp.getDeviceOutInfo())) {
				return Mono.just(fmipLockedResp.getDeviceOutInfo().get(0).isLocked());
			}
			return Mono.just(Boolean.FALSE);
		}).onErrorReturn(Boolean.FALSE);
	}
	
	Predicate<IRetrieveAppleLockDetailsServiceResponse> isAppleDeviceLocked = retrieveAppleLockDetailsResp -> null != retrieveAppleLockDetailsResp && retrieveAppleLockDetailsResp.getDeviceLockResponseData()
			&& "Y".equalsIgnoreCase(retrieveAppleLockDetailsResp.getDeviceLockResponseData().getDeviceLocked());
	
	
	private Mono<Boolean> retrieveVIPAndCompare(RetrieveAppleLockDetailsServiceRequest requestVIP, String cxpCorrelationId,
			Boolean response, String endPoint) {
		try {
			
			return retrieveAppleLockDetailsFromVip(requestVIP).doOnEach(signal -> 
			ReactorUtilities.subscribeAndForget(callComparator(cxpCorrelationId, response, endpoint,
					RETRIEVE_APPLELOCK_DETAILS, DEVICE_INFORMATION_LOOKUP), signal)
					);
		}catch (Exception e) {
			return callComparator(cxpCorrelationId, response, endpoint,
					RETRIEVE_APPLELOCK_DETAILS, DEVICE_INFORMATION_LOOKUP);
		}
	}
	
	private Mono<Boolean> callComparator(String cxpCorrelationId, Boolean response, 
			String functionPath, String newServiceName, String oldServiceName) {
		String channelIdInRequest = RequestAccessContext.getRequestData(String.class, CXPConstants.CHANNEL_ID);
		ServiceRequest serviceRequest = ServiceRequest.builder()
				.newservice_correlationid(cxpCorrelationId)
				.oldservice_correlationid(cxpCorrelationId)
				.function_path(functionPath)
				.newservice_name(newServiceName)
				.oldservice_name(oldServiceName)
				.clientId(channelIdInRequest)
				.new_srv_fparam("")
				.old_srv_fparam("")
				.build();
		return comparatorService.compare(serviceRequest).map(r -> response);
	}
	
}
