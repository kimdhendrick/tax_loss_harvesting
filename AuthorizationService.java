package tlh;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.springframework.stereotype.Component;

@Component
public class AuthorizationService extends Oauth {

	@Override
	public String getHttpMethod() {
		return "GET";
	}

	@Override
	public String getURL() {
		return oAuthProperties.get("AUTH_URL");
	}
	
	@Override
	public void authorize() {
		log.debug(" Starting authorize page request");
		if (Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
			try {

				String url = String.format("%s?key=%s&token=%s",getURL() ,params.getConsumerKey(),params.getoAuthToken().getOauth_token());

				Desktop.getDesktop().browse(new URI(url));
				
			} catch (IOException e) {
				e.printStackTrace();
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
		}else{
			log.error(" Launching default browser is not supported on current platform ");
		}
		System.out.print( "Enter Verifier Code : " );

		String code = KeyIn.getKeyInString();

		log.debug("set code on to params "+code);
		params.setVerifier(code);
		controller.setNextState(((OauthController)controller).getTokenService());
	}

}
