package tlh;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.*;

@Configuration
@PropertySource("classpath:oauth.properties")
@ComponentScan(basePackages = { "etrade.example.*" })

public class OOauthConfig {

	@Value("${oauth.baseUrl}")
	private String baseUrl;

	@Value("${oauth.authorizeUrl}")
	private String authorizeUrl;

	@Value("${oauth.accessUrl}")
	private String accessUrl;

	@Value("${oauth.tokenUrl}")
	private String tokenUrl;

	@Value("${oauth.consumerKey}")
	private String consumerKey;

	@Value("${oauth.secretKey}")
	private String secretKey;

	@Value("${api.baseUrl}")
	private String apiBaseUrl;

	@Value("${api.accountListUri}")
	private String acctListUri;

	@Value("${api.balanceUri}")
	private String balanceUri;
	
	@Value("${api.balanceUri.queryParam}")
	private String queryParam;

	@Value("${api.portfolioUri}")
	private String portfolioUri;

    @Value("${api.quoteUri}")
    private String quoteUri;
    
    @Value("${api.accountsUri}")
    private String accountsUri;
    
	@Value("${oauth.sandboxBaseUrl}")
	protected String sandboxBaseUrl;
	
	@Value("${oauth.sandboxConsumerKey}")
	protected String sandboxConsumerKey;

	@Value("${oauth.sandboxSecretKey}")
	protected String sandboxSecretKey;

	@Bean
	public Map<String,String> oAuthProperties(){
		Map<String, String> oauthParams = new HashMap<String, String>();

		oauthParams.put("CONSUMER_KEY", consumerKey);
		oauthParams.put("CONSUMER_SECRET", secretKey);
		oauthParams.put("BASE_URL", baseUrl);
		oauthParams.put("AUTH_URL", authorizeUrl);
		oauthParams.put("ACCESS_TOKEN_URL", accessUrl);
		oauthParams.put("TOKEN_URL", tokenUrl);

		return oauthParams;
	}

	public Map<String,String> apiProperties(){
		Map<String, String> apiParams = new HashMap<String, String>();

		apiParams.put("API_BASE_URL", apiBaseUrl);
		apiParams.put("ACCT_LIST_URI", acctListUri);
		apiParams.put("BALANCE_URI", balanceUri);
		apiParams.put("BALANCE_QUERY_PARAM", queryParam);
		apiParams.put("PORTFOLIO_URI", portfolioUri);
		apiParams.put("QUOTE_URI", quoteUri);
		apiParams.put("ACCOUNTS_URI", accountsUri);
		return apiParams;
	}
	@Bean
	public GetTokenService getTokenService(){
		GetTokenService getTokenService = new GetTokenService();
		getTokenService.setoAuthProperties(oAuthProperties());
		getTokenService.setParams(oAuth1Parameters());
		return getTokenService;
	}
	@Bean
	public ApiRestClient apiRestClient(){
		ApiRestClient apiRestClient = new ApiRestClient();
		return apiRestClient;
	}

	@Bean
	public RestTemplate restTemplate(){
		List<HttpMessageConverter<?>> converters = new ArrayList<HttpMessageConverter<?>>(2);
		converters.add(new FormHttpMessageConverter() {
			public boolean canRead(Class<?> clazz, MediaType mediaType) {
				// always read MultiValueMaps as x-www-url-formencoded			
				return MultiValueMap.class.isAssignableFrom(clazz);
			}
		});
		converters.add(new StringHttpMessageConverter());
		converters.add(mappingJackson2HttpMessageConverter());
		

		RestTemplate oauthTemplate = new RestTemplate(getClientHttpRequestFactory()); 
		List interceptorList =  new ArrayList();
		interceptorList.add(new MimeInterceptor());
		oauthTemplate.setMessageConverters(converters);
		oauthTemplate.setInterceptors(interceptorList);
		oauthTemplate.setErrorHandler(new RestTemplateResponseErrorHandler());
		return oauthTemplate;
	}
	@Bean
	public MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter() {
		MappingJackson2HttpMessageConverter jsonConverter = new MappingJackson2HttpMessageConverter();
		List<MediaType> mediaTypeList = new ArrayList<MediaType>();
		mediaTypeList.add(MediaType.APPLICATION_JSON);
		jsonConverter.setSupportedMediaTypes(mediaTypeList);

		ObjectMapper mapper = new ObjectMapper();

		mapper.configure(DeserializationFeature.UNWRAP_ROOT_VALUE, true);
		jsonConverter.setObjectMapper(mapper);
		return jsonConverter;
	}
	private ClientHttpRequestFactory getClientHttpRequestFactory() {
		int timeout = 3000;
		RequestConfig config = RequestConfig.custom()
				.setConnectTimeout(timeout)
				.setConnectionRequestTimeout(timeout)
				.setSocketTimeout(timeout)
				.setConnectionRequestTimeout(3000)
				.setRedirectsEnabled(true)
				.build();

		TrustStrategy acceptingTrustStrategy = new TrustStrategy() {
			public boolean isTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
				return true;
			}
		};

		CloseableHttpClient client = null;
		try {

			SSLContext sslContext = org.apache.http.ssl.SSLContexts.custom().loadTrustMaterial(null, acceptingTrustStrategy).build();
			SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext, new NoopHostnameVerifier());

			client = HttpClientBuilder
					.create()
					.setDefaultRequestConfig(config)
					.setRedirectStrategy(new LaxRedirectStrategy())
					.setSSLSocketFactory(csf)
					.build();
		}catch(Exception e){}
		return new HttpComponentsClientHttpRequestFactory(client);
	}

	@Bean
	public static PropertySourcesPlaceholderConfigurer propertyPlaceholderConfigurer() {
		return new PropertySourcesPlaceholderConfigurer();
	}

	@Bean
	public AccountListClient accountListClient() {
		AccountListClient accountListClient = new AccountListClient();
		accountListClient.setApiProperties(apiProperties());
		accountListClient.setoAuthProperties(oAuthProperties());
		accountListClient.setOauthService(getTokenService());
		accountListClient.setParams(oAuth1Parameters());
		accountListClient.setSigner(getSigner());
		return accountListClient;
	}

	@Bean
	public BalanceClient balanceClient() {
		BalanceClient balanceClient = new BalanceClient();
		balanceClient.setApiProperties(apiProperties());
		balanceClient.setoAuthProperties(oAuthProperties());
		balanceClient.setOauthService(getTokenService());
		balanceClient.setParams(oAuth1Parameters());
		balanceClient.setSigner(getSigner());
		return balanceClient;
	}

	@Bean
	public PortfolioClient portfolioClient() {
		PortfolioClient portfolioClient = new PortfolioClient();
		portfolioClient.setApiProperties(apiProperties());
		portfolioClient.setoAuthProperties(oAuthProperties());
		portfolioClient.setOauthService(getTokenService());
		portfolioClient.setParams(oAuth1Parameters());
		portfolioClient.setSigner(getSigner());
		return portfolioClient;
	}

    @Bean
    public QuotesClient quotesClient() {
        QuotesClient quotesClient = new QuotesClient();
        quotesClient.setApiProperties(apiProperties());
        quotesClient.setoAuthProperties(oAuthProperties());
        quotesClient.setOauthService(getTokenService());
        quotesClient.setParams(oAuth1Parameters());
        quotesClient.setSigner(getSigner());
        return quotesClient;
    }

    @Bean
    public OrderClient orderClient() {
    	OrderClient orderClient = new OrderClient();
    	orderClient.setApiProperties(apiProperties());
    	orderClient.setoAuthProperties(oAuthProperties());
    	orderClient.setOauthService(getTokenService());
    	orderClient.setParams(oAuth1Parameters());
    	orderClient.setSigner(getSigner());
        return orderClient;
    }
    @Bean
    public OrderPreview orderPreview() {
    	OrderPreview orderPreview = new OrderPreview();
    	orderPreview.setApiProperties(apiProperties());
    	orderPreview.setoAuthProperties(oAuthProperties());
    	orderPreview.setOauthService(getTokenService());
    	orderPreview.setParams(oAuth1Parameters());
    	orderPreview.setSigner(getSigner());
        return orderPreview;
    }
	@Bean
	public OAuth1Parameters oAuth1Parameters(){
		return new OAuth1Parameters( getSigner(),oAuthProperties());
	}
	@Bean
	public TempCredentialService tempCredentialService(){
		TempCredentialService credService = new TempCredentialService();
		credService.setoAuthProperties(oAuthProperties());
		credService.setParams(oAuth1Parameters());
		return credService;
	}
	@Bean
	public AuthorizationService authorizationService(){
		AuthorizationService authService = new AuthorizationService();
		authService.setoAuthProperties(oAuthProperties());
		authService.setParams(oAuth1Parameters());
		return authService;
	}
	@Bean
	public TokenService tokenService(){
		TokenService tokenService = new TokenService();
		tokenService.setoAuthProperties(oAuthProperties());
		tokenService.setParams(oAuth1Parameters());
		return tokenService;
	}
	@Bean
	public OauthController oauthController(){
		OauthController controller = new OauthController();
		controller.setoAuthProperties(oAuthProperties());
		return controller;
	}
	@Bean
	public  OAuthSigner getSigner(){
		return new HmacSha1Signer();
	}
	@Bean
	public  VelocityEngine velocityEngineFactoryBean(){
		VelocityEngine vEngine = new VelocityEngine();
		Properties props = new Properties();
			props.put(RuntimeConstants.RESOURCE_LOADER, "classpath");
			props.put("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
			props.put("runtime.log.logsystem.class", "org.apache.velocity.runtime.log.NullLogChute");
			vEngine.init(props);
		return vEngine;
	}
}
