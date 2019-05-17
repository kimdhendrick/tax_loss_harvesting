package tlh;

import java.awt.Desktop;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

public abstract class OAuthService {

	@Autowired
	RestTemplate restTemplate;

	Map<String,String> oAuthProperties;

	OAuthSigner signer;
	
	SessionData sessionData = null;
	
	OAuth1Parameters params = null;

	public Map<String, String> getoAuthProperties() {
		return oAuthProperties;
	}

	public void setoAuthProperties(Map<String, String> oAuthProperties) {
		this.oAuthProperties = oAuthProperties;
	}

	Logger log = Logger.getLogger(OAuthService.class);
	
	public OAuthService(){}

	public void  fetchToken() throws UnsupportedEncodingException, GeneralSecurityException {
		HttpHeaders headers = new HttpHeaders();

		final String url = buildTokenUrl();
		log.error("URL :"+url);

		params.computeSignature(getTokenMethod(), url);

		Map<String,String> queryParam = params.getHeaderMap();
		
		sessionData = new SessionData();
		
		call(url, queryParam, getTokenMethod(), headers);
		
	}
	
	public void authorize(){
		
		if (Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
			try {

				String url = String.format("%s?key=%s&token=%s",buildAuthorizeUrl() ,params.getConsumerKey(),params.getoAuthToken().getOauth_token());

				Desktop.getDesktop().browse(new URI(url));
				
			} catch (IOException e) {
				e.printStackTrace();
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
		}else{
			log.error(" Launching default browser is not supported on current platform ");
		}
	}
	
	public void fetchAccessToken() throws UnsupportedEncodingException, GeneralSecurityException{
		
		
		HttpHeaders headers = new HttpHeaders();
		
		log.debug( "isParam Null : "+(params==null?"YES":"NOT"));
		
		params.computeSignature(getAccessTokenMethod(), buildAccessTokenUrl());

		Map<String,String> queryParam = params.getHeaderMap();
		
		call(buildAccessTokenUrl(),queryParam, getAccessTokenMethod(), headers);
	}
	
	
	private void call(String url,Map<String,String> queryParam, String method, HttpHeaders headers){
		
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
		ResponseEntity<MultiValueMap> response = restTemplate.exchange(builder.buildAndExpand().toString(), HttpMethod.valueOf(method), new HttpEntity<HttpHeaders>(headers), MultiValueMap.class);
		MultiValueMap<String, String> body = response.getBody();
		
		OAuthToken oauthToken = new OAuthToken(body.getFirst("oauth_token"), body.getFirst("oauth_token_secret"));
		
		log.debug("oauth_token"+  body.getFirst("oauth_token"));
		log.debug("oauth_token_secret"+body.getFirst("oauth_token_secret"));
		
		if( params.getoAuthToken() == null ){
			params.setoAuthToken(oauthToken);
			sessionData.setRequestToken(params.getoAuthToken());
		}else{
			OAuthToken accessToken = new OAuthToken(body.getFirst("oauth_token"), body.getFirst("oauth_token_secret"));
			params.setoAuthToken(oauthToken);
			sessionData.setAccessToken(accessToken);
			
		}
	
	}
	
	public String getTokenMethod(){
		return "GET";
	}
	
	public String getAuthorizeMethod(){
		return "GET";
	}
	public String getAccessTokenMethod(){
		return "GET";
	}
	
	public SessionData getSessionData() {
		return sessionData;
	}

	public void setParams(OAuth1Parameters params) {
		this.params = params;
	}

	public abstract OAuthSigner getSigner();
	public abstract String buildTokenUrl();
	public abstract String buildAuthorizeUrl();
	public abstract String buildAccessTokenUrl();

}
