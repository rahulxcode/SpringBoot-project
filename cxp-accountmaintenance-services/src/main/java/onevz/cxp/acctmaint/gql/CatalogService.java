package onevz.cxp.acctmaint.gql;

@CXPGQLDomain("catalog")
public interface CatalogService extends CXPGQLService{
	
	@GQLQueryName("deviceSku")
	@CXPGQL(service = DeviceSku.class)
	@Cachable(maxItems=10000)
	public Mono<GQLQueryResponse<DeviceSkuData>> queryDeviceSku(@GQLQueryValue("skuId") String skuId, CacheOverride cacheOverride);
}
