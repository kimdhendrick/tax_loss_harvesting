package tlh;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.util.Map;

import org.springframework.http.HttpHeaders;

public class TokenService extends Oauth {

	@Override
	public String getHttpMethod() {
		return "GET";
	}

	@Override
	public String getURL() {
		return String.format("%s%s",oAuthProperties.get("BASE_URL"),oAuthProperties.get("ACCESS_TOKEN_URL"));
	}
	@Override
	public SessionData fetchAccessToken()throws InvalidStateException {
		HttpHeaders headers = new HttpHeaders();

		log.debug( "isParam Null : "+(params==null?"YES":"NOT"));

		try {
			params.computeSignature(getHttpMethod(), getURL());
		} catch (UnsupportedEncodingException e) {
			log.error("UnsupportedEncodingException" ,e);
			throw new RuntimeException("UnsupportedEncodingException");
		} catch (GeneralSecurityException e) {
			log.error("GeneralSecurityException" ,e);
			throw new RuntimeException("GeneralSecurityException");
		}

		Map<String,String> queryParam = params.getHeaderMap();

		return call(getURL(),queryParam, getHttpMethod(), headers);
	}
}
