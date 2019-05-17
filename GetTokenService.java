package tlh;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.util.Map;
import java.util.Scanner;

@Deprecated
public class GetTokenService extends OAuthService {

	BufferedReader br = null;
	private static final java.io.PrintStream out = System.out;
	

	public void setoAuthProperties(Map<String,String> oAuthProperties) {
		this.oAuthProperties = oAuthProperties;
	}

	public GetTokenService(){
		super();
		br = new BufferedReader(new InputStreamReader(System.in));
	}

	@Override
	public void fetchToken() throws UnsupportedEncodingException,GeneralSecurityException{
		if( validateCKeys() )
		   super.fetchToken();
		else{
		   out.println( " Invalid Keys in Properties file" );
		   System.exit(0);
		}
		   
	}

	@Override
	public void authorize() {
		log.debug(" Starting authorize page request");
		super.authorize();
		System.out.println( "Enter Verifier Code" );

		String code = "";

		// code = br.readLine();
		Scanner scanner = new Scanner(System.in);
		code = scanner.next();
		System.out.println("The code "+code);
		params.setVerifier(code);
		//br.close();

	}


	@Override
	public void fetchAccessToken()
			throws UnsupportedEncodingException, GeneralSecurityException {
		super.fetchAccessToken();
	}


	@Override
	public SessionData getSessionData() {
		return super.getSessionData();
	}

	@Override
	public  OAuthSigner getSigner(){
		return new HmacSha1Signer();
	}


	@Override
	public String getTokenMethod(){
		return "GET";
	}
	@Override
	public String getAuthorizeMethod(){
		return "GET";
	}
	@Override
	public String getAccessTokenMethod(){
		return "GET";
	}
	@Override
	public String buildAuthorizeUrl() {
		return oAuthProperties.get("AUTH_URL");
	}
	@Override
	public String buildTokenUrl(){
		return String.format("%s%s",oAuthProperties.get("BASE_URL"),oAuthProperties.get("TOKEN_URL"));
	}
	@Override
	public String buildAccessTokenUrl(){
		return String.format("%s%s",oAuthProperties.get("BASE_URL"),oAuthProperties.get("ACCESS_TOKEN_URL"));
	}
	private boolean validateCKeys(){
        return this.oAuthProperties.get("CONSUMER_KEY") != null &&
                this.oAuthProperties.get("CONSUMER_KEY").length() != 0 &&
                this.oAuthProperties.get("CONSUMER_SECRET") != null &&
                this.oAuthProperties.get("CONSUMER_SECRET").length() != 0;

    }
}
