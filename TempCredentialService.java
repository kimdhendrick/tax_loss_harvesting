package tlh;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

@Component
public class TempCredentialService extends Oauth {

	@Override
	public String getHttpMethod() {
		return "GET";
	}

	@Override
	public String getURL() {
		return String.format("%s%s",oAuthProperties.get("BASE_URL"),oAuthProperties.get("TOKEN_URL"));
	}
	
	private boolean validateCKeys(){
        return this.oAuthProperties.get("CONSUMER_KEY") != null &&
                this.oAuthProperties.get("CONSUMER_KEY").length() != 0 &&
                this.oAuthProperties.get("CONSUMER_SECRET") != null &&
                this.oAuthProperties.get("CONSUMER_SECRET").length() != 0;

    }
	@Override
	public void fetchToken() throws InvalidStateException{
		log.debug(" Request for temporary credentials....");
		if( validateCKeys() ) {
			HttpHeaders headers = new HttpHeaders();

			final String url = getURL();
			
			log.warn("URL :"+url);

			try {
				params.computeSignature(getHttpMethod(), url);
			} catch (UnsupportedEncodingException e) {
				log.error(" UnsupportedEncodingException ",e);
				throw new RuntimeException("GeneralSecurityException");
			} catch (GeneralSecurityException e) {
				log.error(" GeneralSecurityException ",e);
				throw new RuntimeException("GeneralSecurityException");
			}

			Map<String,String> queryParam = params.getHeaderMap();
			
			call(url, queryParam, getHttpMethod(), headers);
			log.warn(" Setting the controller ");
			controller.setNextState(((OauthController)controller).getAuthorizationService());
			log.warn("Transfering to authorization process... ");
		}else{
			log.error(" Validation with oauth properties failed");
			out.println( " Invalid Keys in Properties file " );
			System.exit(0);
		}
	}
	
}
