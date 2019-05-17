package tlh;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.util.Map;

public class PortfolioClient extends AbstractClient {

    public PortfolioClient(){}

    Map<String,String> apiProperties;

    @Override
    public String getHttpMethod(){
        return "GET";
    }

    @Override
    public String getURL(String accountIdKey) {
        String url = String.format("%s%s%s%s", apiProperties.get("API_BASE_URL"),apiProperties.get("PORTFOLIO_URI"),accountIdKey,"/portfolio");
        log.debug("Portfolio URL "+url);
        return url;
    }

    @Override
    public String getURL() {
        return ("");
    }
    public void setApiProperties(Map<String, String> apiProperties) {
        this.apiProperties = apiProperties;
    }


    public String getPortfolio(String accountIdKey) throws UnsupportedEncodingException, GeneralSecurityException, ApiException{

        log.debug(" Portfolio client...  ");

        OAuth1Parameters params = new OAuth1Parameters(getSigner(), oAuthProperties);
        
        params.setoAuthToken(sessionData.getAccessToken());

        log.debug(" Generating Signature for portfolio api call");
        params.computeSignature(getHttpMethod(), getURL(accountIdKey));


        Map<String,String> queryParam = params.getHeaderMap();
        
        log.debug(" Calling GetPortfolio API ");
        return apiRestClient.callService(getURL(accountIdKey),  getHttpMethod(), queryParam);
    }

}
