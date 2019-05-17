package tlh;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.util.Map;

public class AccountListClient extends AbstractClient {
	
	public AccountListClient(){}
	
	Map<String,String> apiProperties;
	
	@Override
	public String getHttpMethod(){
		return "GET";
	}

	@Override
	public String getURL() {
		return String.format("%s%s", apiProperties.get("API_BASE_URL"),apiProperties.get("ACCT_LIST_URI"));
	}

	@Override
	public String getURL(String url) {
		return ("");
	}
	public void setApiProperties(Map<String, String> apiProperties) {
		this.apiProperties = apiProperties;
	}

	/*public String getAccountList() throws UnsupportedEncodingException, GeneralSecurityException {
		log.debug(" Starting API call...");
		return super.getAccountList();
	}
*/
	public String getAccountList() throws UnsupportedEncodingException, GeneralSecurityException,ApiException{
		
		log.debug("Calling account list api: ");
		
		
		OAuth1Parameters params = new OAuth1Parameters(getSigner(), oAuthProperties);

		params.setoAuthToken(sessionData.getAccessToken());
		
		log.debug(" Generating Signature of Account list api call");
		params.computeSignature(getHttpMethod(), getURL());

		//final String authHeader = params.getAuthorizationHeader();
		Map<String,String> queryParam = params.getHeaderMap();

		log.debug(" Calling Accountlist API ");
		return apiRestClient.callService(getURL(),  getHttpMethod(), queryParam);
	}
	
}
