package tlh;

import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

public class Oauth {
	
	protected Logger log = Logger.getLogger(Oauth.class);
	
	protected static final java.io.PrintStream out = System.out;
	
	public Map<String,String> oAuthProperties;
	
	protected OAuth1Parameters params = null;
	
	//protected SessionData sessionData = null;
	
	Oauth controller = null;
	
	@Autowired
	RestTemplate restTemplate;
	
	public void fetchToken() throws InvalidStateException{
		log.error("Invalid State..... ");
		throw new InvalidStateException("Invalid state on fetchToken api");
	}
	public void authorize() {
		log.error("Invalid State.....");
		throw new InvalidStateException("Invalid state on authorize api");
	}
	public SessionData fetchAccessToken()throws InvalidStateException {
		log.error("Invalid State.....fetchAccessToken");
		throw new InvalidStateException("Invalid state on fetchAccessToken api");
		
	}
	public String status()throws InvalidStateException {
		log.error("Invalid State.....fetchAccessToken");
		throw new InvalidStateException("Invalid state on fetchAccessToken api");
		
	}
	/*public SessionData getSessionData() {
		return sessionData;
	}*/
	protected SessionData call(String url,Map<String,String> queryParam, String method, HttpHeaders headers){
		
		UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);
	
		builder.queryParam("oauth_consumer_key", queryParam.get("oauth_consumer_key"));
		builder.queryParam("oauth_nonce", queryParam.get("oauth_nonce"));
		builder.queryParam("oauth_signature_method", queryParam.get("oauth_signature_method"));
		builder.queryParam("oauth_timestamp", queryParam.get("oauth_timestamp"));
		builder.queryParam("oauth_signature", queryParam.get("oauth_signature"));
		if( params.getoAuthToken() != null){
			builder.queryParam("oauth_token", queryParam.get("oauth_token"));
			builder.queryParam("oauth_verifier", queryParam.get("oauth_verifier"));
		}else{
			builder.queryParam("oauth_callback", queryParam.get("oauth_callback"));
			builder.queryParam("oauth_version", "1.0");
		}
		ResponseEntity<LinkedMultiValueMap> response = restTemplate.exchange(builder.buildAndExpand().toString(), HttpMethod.valueOf(method), new HttpEntity<HttpHeaders>(headers), LinkedMultiValueMap.class);
		
		MultiValueMap<String, String> body = response.getBody();
		
		OAuthToken oauthToken = new OAuthToken(body.getFirst("oauth_token"), body.getFirst("oauth_token_secret"));
		
		log.debug("oauth_token"+  body.getFirst("oauth_token"));
		log.debug("oauth_token_secret"+body.getFirst("oauth_token_secret"));
		
		SessionData sessionData = null;
		
		if( params.getoAuthToken() == null ){
			params.setoAuthToken(oauthToken);
			//sessionData.setRequestToken(params.getoAuthToken());
		}else{
			OAuthToken accessToken = new OAuthToken(body.getFirst("oauth_token"), body.getFirst("oauth_token_secret"));
			params.setoAuthToken(oauthToken);
			log.debug("access oauth_token"+  body.getFirst("oauth_token"));
			log.debug("acces oauth_token_secret"+body.getFirst("oauth_token_secret"));
			sessionData = new SessionData();
			sessionData.setAccessToken(accessToken);
		}
		return sessionData;
	}
	public String getHttpMethod() {
		log.error("Invalid HttpMethod.....");
		throw new InvalidStateException(" HttpMethod not initiliazed.....");
	}
	public String getURL() {
		log.error("Invalid HttpMethod.....");
		throw new InvalidStateException(" Url not initiliazed.....");
	}
	
	public void setNextState(Oauth controller) {
		this.controller = controller;
	}
	public void setoAuthProperties(Map<String,String> oAuthProperties) {
		this.oAuthProperties = oAuthProperties;
	}
	public void setParams(OAuth1Parameters params) {
		this.params = params;
	}
}
