package tlh;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.net.URLCodec;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.*;

public class OAuth1Parameters {
	public final static PrintStream out = System.out;	//plh added this line

	Logger log = Logger.getLogger(OAuth1Parameters.class);

	private static final SecureRandom secureRand = new SecureRandom();

	private OAuthSigner oauthsigner = null;

	private String baseUrl;

	public String callback = "oob";

	/**
	 * Required identifier portion of the client credentials (equivalent to a username).
	 */
	public String consumerKey = "";

	/** Required nonce value. Should be computed using {@link #computeNonce()}. */
	public String oauth_nonce;

	/** Realm. */
	public String realm="";

	/** Signature. Required but normally computed using {@link #computeSignature}. */
	public String signature;

	/**
	 * Name of the signature method used by the client to sign the request. Required, but normally
	 * computed using 
	 */
	public String signatureMethod;

	/**
	 * Required timestamp value. Should be computed using {@link #computeTimestamp()}.
	 */
	public String timestamp;

	public String secret;

	/** The verification code received from the server. */
	public String verifier;

	private OAuthToken oAuthToken = null;

	Map<String,String> oAuthProperties;

	String queryString = "";

	public OAuth1Parameters(OAuthSigner signer, Map<String,String> oAuthProperties){
		this.oauthsigner = signer;
		this.oAuthProperties = oAuthProperties;
		setConsumerKey(oAuthProperties.get("CONSUMER_KEY"));
		setSecret(oAuthProperties.get("CONSUMER_SECRET"));
		setSignatureMethod(this.oauthsigner.getSignatureMethod());
		setBaseUrl(oAuthProperties.get("BASE_URL"));
	}

	public void setOauthsigner(OAuthSigner oauthsigner) {
		this.oauthsigner = oauthsigner;
	}

	public void setoAuthProperties(Map<String, String> oAuthProperties) {
		this.oAuthProperties = oAuthProperties;
	}

	public String getQueryString() {
		return queryString;
	}

	public void setQueryString(String queryString) {
		this.queryString = queryString;
	}

	public String getVerifier() {
		return verifier;
	}
	public void setVerifier(String verifier) {
		this.verifier = verifier;
	}
	public OAuthToken getoAuthToken() {
		return oAuthToken;
	}
	public void setoAuthToken(OAuthToken oAuthToken) {
		this.oAuthToken = oAuthToken;
	}

	public String getBaseUrl() {
		return baseUrl;
	}

	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}

	public String getCallback() {
		return callback;
	}

	public void setCallback(String callback) {
		this.callback = callback;
	}

	public String getConsumerKey() {
		return consumerKey;
	}

	public void setConsumerKey(String consumerKey) {
		this.consumerKey = consumerKey;
	}

	public String getOauth_nonce() {
		return oauth_nonce;
	}

	public void setOauth_nonce(String oauth_nonce) {
		this.oauth_nonce = oauth_nonce;
	}

	public String getRealm() {
		return realm;
	}

	public void setRealm(String realm) {
		this.realm = realm;
	}

	public String getSignature() {
		return signature;
	}

	public void setSignature(String signature) {
		this.signature = signature;
	}

	public String getSignatureMethod() {
		return signatureMethod;
	}

	public void setSignatureMethod(String signatureMethod) {
		this.signatureMethod = signatureMethod;
	}

	public String getSecret() {
		return secret;
	}

	public void setSecret(String secret) {
		this.secret = secret;
	}

	public String computeNonce() {
		long generatedNo = secureRand.nextLong();
		return (oauth_nonce = new String(Base64.encodeBase64((String.valueOf(generatedNo)).getBytes())));
	}

	public String computeTimestamp() {
		return (timestamp = Long.toString(System.currentTimeMillis() / 1000));
	}

	public void computeSignature(String method, String uri) throws UnsupportedEncodingException, GeneralSecurityException{

		this.oauth_nonce = computeNonce();

		this.signatureMethod = getSignatureMethod();

		this.timestamp = computeTimestamp();

		Map<String, String[]> requestMap = new TreeMap<String, String[]>(String.CASE_INSENSITIVE_ORDER);

		requestMap.put("oauth_consumer_key",new String[] { getConsumerKey() });
		requestMap.put("oauth_timestamp", new String[] { timestamp });
		requestMap.put("oauth_nonce", new String[] { oauth_nonce });
		requestMap.put("oauth_signature_method",new String[] { signatureMethod });

		if( oAuthToken != null ){
			requestMap.put("oauth_token", new String[]{encode(oAuthToken.getOauth_token())}); 
			if( StringUtils.isNotBlank(getVerifier())){
				requestMap.put("oauth_verifier", new String[]{encode(getVerifier())});
			}
		}else{
			requestMap.put("oauth_callback", new String[] {getCallback()});
			requestMap.put("oauth_version", new String[] {"1.0"});
		}

		method = method.toUpperCase();

		Map<String,String[]> qParamMap = getQueryStringMap();

		if( !qParamMap.isEmpty()){
			requestMap.putAll(qParamMap);
		}

		String encodedParams = encode(getNormalizedParams(requestMap));

		log.debug("Normalized string after encoding "+ encodedParams);
		//out.println("Computed String " + encodedParams);	//plh added this line

		String encodedUri = encode(uri);

		String baseString = method+"&"+encodedUri+"&"+encodedParams;

		log.debug("signature base string "+baseString );

		this.signature = oauthsigner.computeSignature(baseString, this);

		log.debug( " signature "+ signature);

	}

	private Map<String,String[]> getQueryStringMap(){
		Map<String,String[]> queryParamMap = new HashMap<String,String[]>();
		if( queryString != null && queryString.length() > 0){
			for(String keyValue : queryString.split("&"))
			{
				String[] p = keyValue.split("=");
				queryParamMap.put(p[0],new String[] {p[1]});
			}
		}
		return queryParamMap;
	}

	public String getNormalizedParams(Map<String,String[]> paramMap){
		StringBuilder combinedParams = new StringBuilder();
		TreeMap<String,String[]> sortedMap = new TreeMap<String,String[]>();
		for(String key:paramMap.keySet()){
			//the value can be null, in which case we do not want any associated array here
			String[] encodedValues = (paramMap.get(key)!=null) ? new String[paramMap.get(key).length] : new String[0];
			//encode keys, and sort the array
			for(int i=0; i< encodedValues.length;i++){
				encodedValues[i] = encode(paramMap.get(key)[i]);
			}
			Arrays.sort(encodedValues);
			sortedMap.put(encode(key), encodedValues);
		}
		//now we generate a string for the map in key=value&key1=value1 format by concatenating the values
		StringBuilder normalizedRequest = new StringBuilder();
		for(String key: sortedMap.keySet()){
			//we need to handle the case if the value is null 
			if(sortedMap.get(key)==null || sortedMap.get(key).length==0){
				normalizedRequest.append(key+"=&");
			}
			for(String value: sortedMap.get(key)){
				//this for loop will not execute if the value is null or empty
				normalizedRequest.append(key+"="+value+"&");
			}
		}
		return normalizedRequest.toString().substring(0,normalizedRequest.length()-1);

	}

	public Map<String,String> getHeaderMap(){
		Map<String, String> requestMap = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER);
		requestMap.put("oauth_consumer_key", getConsumerKey());
		requestMap.put("oauth_timestamp",  timestamp );
		requestMap.put("oauth_nonce",oauth_nonce );
		requestMap.put("oauth_signature_method",getSignatureMethod());
		requestMap.put("oauth_signature", signature); 
		if( oAuthToken != null ){
			requestMap.put("oauth_token", encode(oAuthToken.getOauth_token()));
			if( StringUtils.isNotBlank(getVerifier())){
				requestMap.put("oauth_verifier", encode(getVerifier()));
			}
		}else{
			requestMap.put("oauth_callback", getCallback());
			requestMap.put("oauth_version", "1.0");
		}
			
		Map<String,String[]> queryParamMap = getQueryStringMap();
		
		if( queryParamMap != null &&  !queryParamMap.isEmpty()){

			for (Map.Entry<String, String[]> entry : queryParamMap.entrySet()) {
				if( entry != null ){
					String[] v = entry.getValue();
					if(v != null && v.length > 0){
						requestMap.put(entry.getKey(), v[0]);
					}
				}
			}
		}
		return requestMap;
	}
	public String getAuthorizationHeader() {
		StringBuilder buf = new StringBuilder("OAuth");
		append(buf,"realm", "");
		append(buf, "oauth_consumer_key", getConsumerKey());
		append(buf, "oauth_timestamp", getTimestamp());
		append(buf, "oauth_nonce", getOauth_nonce());
		append(buf, "oauth_signature_method", getSignatureMethod());
		append(buf, "oauth_signature", getSignature());

		if( oAuthToken != null ){
			append(buf,"oauth_token", encode(oAuthToken.getOauth_token()));
		}
		return buf.substring(0, buf.length() - 1);
	}

	public void append(StringBuilder buf, final String key, final String value){
		buf.append(' ').append(key).append("=\"").append(value).append("\",");
		//out.println( "Buffer= " + buf);	//plh added this line
	}


	public String getTimestamp() {
		return timestamp;
	}


	public static String encode(String value) {
		if (value == null) {
			return "";
		}
        return new String(URLCodec.encodeUrl(WWW_FORM_URL_SAFE, value.getBytes(StandardCharsets.UTF_8)), StandardCharsets.US_ASCII);
    }
	public static String oauthDecode(String value) throws DecoderException {
		if (value == null) {
			return "";
		}

        return new String(URLCodec.decodeUrl(value.getBytes(StandardCharsets.US_ASCII)),
StandardCharsets.UTF_8);
    }

	BitSet unreserved = null;

	private static final BitSet WWW_FORM_URL_SAFE = new BitSet(256);
	static {
		// alpha characters
		for (int i = 'a'; i <= 'z'; i++) {
			WWW_FORM_URL_SAFE.set(i);
		}
		for (int i = 'A'; i <= 'Z'; i++) {
			WWW_FORM_URL_SAFE.set(i);
		}
		// numeric characters
		for (int i = '0'; i <= '9'; i++) {
			WWW_FORM_URL_SAFE.set(i);
		}
		// special chars
		WWW_FORM_URL_SAFE.set('-');
		WWW_FORM_URL_SAFE.set('_');
		WWW_FORM_URL_SAFE.set('.');
		//WWW_FORM_URL_SAFE.set('*');
		// blank to be replaced with +
		//WWW_FORM_URL_SAFE.set(' ');
		WWW_FORM_URL_SAFE.set('~');

	}
}
