package tlh;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;

public class OauthErrorHandler implements ResponseErrorHandler{
	private List acceptableStatus  =  new ArrayList();
	
	public OauthErrorHandler(){
		acceptableStatus.add(HttpStatus.SC_OK);
	}
	
	@Override
	  public void handleError(ClientHttpResponse response) throws IOException {
		System.out.println("Response error: {} {}"+ response.getStatusCode() + " "+ response.getStatusText());
	  }

	  @Override
	  public boolean hasError(ClientHttpResponse response) throws IOException {
		  return !acceptableStatus.contains(response.getStatusCode());
	  }
}
