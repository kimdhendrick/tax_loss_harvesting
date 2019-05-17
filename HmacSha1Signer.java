package tlh;

import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

public class HmacSha1Signer implements OAuthSigner {

	Logger log = Logger.getLogger(HmacSha1Signer.class);

	private static String HMAC_SHA1_ALGORITHM = "HmacSHA1";

	public String getSignatureMethod() {
		return "HMAC-SHA1";
	}

	public String computeSignature(String signatureBaseString, OAuth1Parameters params) throws GeneralSecurityException {

		String key = "";
		if( params.getoAuthToken() != null){
			 key = StringUtils.isEmpty(params.getoAuthToken().getOauth_token_secret()) ? params.getSecret()+"&" : 
						params.getSecret()+"&" + OAuth1Parameters.encode(params.getoAuthToken().getOauth_token_secret());
		}else{
			key =  params.getSecret()+"&";
		}
		/*log.debug(" signing key "+ key);

		log.debug(" computeSignature "+ signatureBaseString);

		log.debug(" the generated url "+ signatureBaseString);*/
		
		SecretKeySpec signingKey = new SecretKeySpec(key.getBytes(),HMAC_SHA1_ALGORITHM);
		// get an hmac_sha1 Mac instance and initialize with the signing key
		Mac mac = null;
		try {
			mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalArgumentException(e);
		}

		try {
			mac.init(signingKey);
		} catch (InvalidKeyException e) {
			throw new IllegalArgumentException(e);
		}

		// compute the hmac on the signatureBaseString
		byte[] raw = mac.doFinal(signatureBaseString.getBytes());
		// base64-encode the hmac
		String result = new String(Base64.encodeBase64(raw));
		//I added this plh
		log.debug(" computeSignature from HMAC "+result);
		return result;
	}
}
