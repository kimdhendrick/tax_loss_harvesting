package tlh;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collections;
import java.util.Map;

public class ApiRestClient {

    @Autowired
    RestTemplate restTemplate;

    Logger log = Logger.getLogger(ApiRestClient.class);

    public String callService(final String url, final String method, Map<String, String> queryParam) throws ApiException {
        return callService(url, method, queryParam, "");
    }

    public String callService(final String url, final String method, Map<String, String> queryParam, String body) throws ApiException {
        HttpHeaders headers = new HttpHeaders();

        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);

        for (Map.Entry<String, String> entry : queryParam.entrySet()) {
            builder.queryParam(entry.getKey(), entry.getValue());
        }
        String rep = "";
        try {
            if (HttpMethod.GET.name().equalsIgnoreCase(method)) {
                rep = restTemplate.getForObject(builder.buildAndExpand().toString(), String.class);
            } else if (HttpMethod.POST.name().equalsIgnoreCase(method)) {
                log.debug("POST request " + body);
                rep = restTemplate.postForObject(builder.buildAndExpand().toString(), body, String.class);
            }
        } catch (ResourceAccessException e) {
            log.error(" Error Calling service api ", e);
            log.error("Exception class name " + e.getCause().getClass().getSimpleName());

            if (ApiException.class.isAssignableFrom(e.getCause().getClass())) {
                log.error(" ApiException found ");
                throw (ApiException) (e.getCause());
            } else
                throw new ApiException(500, "500", "Internal Failure");
        } catch (Exception e) {
            log.error(" Error Calling service api ", e);
            log.error("Exception class name " + e.getClass().getName());
            if (ApiException.class.isAssignableFrom(e.getCause().getClass())) {
                log.error(" ApiException found ");
                throw ((ApiException) e);
            } else
                throw new ApiException(500, "500", "Internal Failure");
        }
        return rep;
    }

}
