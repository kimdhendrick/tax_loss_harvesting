package tlh;

import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractClient {
	
	protected Logger log = Logger.getLogger(AbstractClient.class);

	protected GetTokenService oauthService;

	@Autowired
	protected ApiRestClient apiRestClient;

	protected Map<String,String> oAuthProperties;

	protected OAuthSigner signer;

	protected boolean isInitialized  = false;
	
	protected OAuth1Parameters params = null;
	
	protected SessionData sessionData = null;
	
/*	@Autowired
	protected OauthController controller;*/
	
	public AbstractClient(){}
	
	public void setParams(OAuth1Parameters params) {
		this.params = params;
	}

/*	public void init(){
		try {
			log.debug("Current Thread :"+ Thread.currentThread().getName() + ", Id : "+Thread.currentThread().getId() );
			
			controller.fetchToken();
			
			controller.authorize();
			
			controller.fetchAccessToken();
			
			isInitialized = true;
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (GeneralSecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
*/


	public void setSessionData(SessionData sessionData) {
		this.sessionData = sessionData;
	}

	public void setOauthService(GetTokenService oauthService) {
		this.oauthService = oauthService;
	}

	public void setoAuthProperties(Map<String, String> oAuthProperties) {
		this.oAuthProperties = oAuthProperties;
	}
	
	public void setSigner(OAuthSigner signer) {
		this.signer = signer;
	}
	public OAuthSigner getSigner(){
		return signer;
	}
	public abstract String getHttpMethod();
	public abstract String getURL();
	public abstract String getURL(String acctIdKey);
	
}
