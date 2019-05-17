package tlh;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.util.Map;

public class BalanceClient extends AbstractClient {

    public BalanceClient(){}

    Map<String,String> apiProperties;

    @Override
    public String getHttpMethod(){
        return "GET";
    }

    @Override
    public String getURL(String accountIdKey) {
        String url = String.format("%s%s%s%s", apiProperties.get("API_BASE_URL"),apiProperties.get("BALANCE_URI"),accountIdKey,"/balance");
        log.error("Balance URL "+url);
        return url;
    }

    @Override
    public String getURL() {
        return ("");
    }
    public void setApiProperties(Map<String, String> apiProperties) {
        this.apiProperties = apiProperties;
    }


    public String getBalance(String accountIdKey) throws UnsupportedEncodingException, GeneralSecurityException, ApiException{

        log.debug("Value of isInitialized in BalanceClient : "+isInitialized);

       
        OAuth1Parameters params = new OAuth1Parameters(getSigner(), oAuthProperties);
        
        params.setQueryString(apiProperties.get("BALANCE_QUERY_PARAM"));

        params.setoAuthToken(sessionData.getAccessToken());
        
        log.debug(" Balance Client : access token "+sessionData.getAccessToken().getOauth_token());
        log.debug(" Balance Client : access secret "+sessionData.getAccessToken().getOauth_token_secret());
        
        log.debug(" Generating Signature of get Balance api call");
        
        params.computeSignature(getHttpMethod(), getURL(accountIdKey));

       
        Map<String,String> queryParam = params.getHeaderMap();
        
        log.debug("Query param " +  apiProperties.get("BALANCE_QUERY_PARAM"));
        log.debug(" Calling GetBalance API ");
        return apiRestClient.callService(getURL(accountIdKey),  getHttpMethod(), queryParam);
    }

}
