package tlh;

import java.security.GeneralSecurityException;

public interface OAuthSigner {
	//Returns oauth signature method, for exmaple HMAC-SHA1
	String getSignatureMethod();
	
	//compute signature based on given signature method
	String computeSignature(String signatureBaseString,OAuth1Parameters params) throws GeneralSecurityException;
}
