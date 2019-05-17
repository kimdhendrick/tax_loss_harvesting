package tlh;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class OauthController extends Oauth{

	@Autowired
	TempCredentialService tempCredService;

	@Autowired
	AuthorizationService authorizationService;

	@Autowired
	TokenService tokenService;

	public void fetchToken() throws InvalidStateException{
		controller = tempCredService;
		tempCredService.setNextState(this);
		controller.fetchToken();
	}
	public void authorize() {
		authorizationService.setNextState(this);
		controller.authorize();
	}
	public SessionData fetchAccessToken() {
		tokenService.setNextState(this);
		return controller.fetchAccessToken();
	}

	public TempCredentialService getTempCredService() {
		return tempCredService;
	}
	public AuthorizationService getAuthorizationService() {
		return authorizationService;
	}
	public TokenService getTokenService() {
		return tokenService;
	}
	public void setoAuthProperties(Map<String,String> oAuthProperties) {
		this.oAuthProperties = oAuthProperties;
	}
}
