package tlh;

public class SessionData  {
	
	private OAuthToken requestToken;
	
	private OAuthToken accessToken;

	public OAuthToken getRequestToken() {
		return requestToken;
	}

	public OAuthToken getAccessToken() {
		return accessToken;
	}

	public void setRequestToken(OAuthToken requestToken) {
		this.requestToken = requestToken;
	}

	public void setAccessToken(OAuthToken accessToken) {
		this.accessToken = accessToken;
	}

}
