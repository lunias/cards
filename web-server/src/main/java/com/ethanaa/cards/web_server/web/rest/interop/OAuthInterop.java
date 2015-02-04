package com.ethanaa.cards.web_server.web.rest.interop;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

public class OAuthInterop extends RestTemplate {

	private String baseUrl;

	public OAuthInterop(String baseUrl) {

		super.setErrorHandler(new OAuthResponseErrorHandler());
		
		this.baseUrl = baseUrl;
	}

	/**
	 * 
	 * @param url
	 * @param method
	 * @param requestEntity
	 * @param responseType
	 * @param httpServletRequest
	 * @param uriVariables
	 * @return
	 * @throws RestClientException
	 */
	public <T> ResponseEntity<T> exchange(String url, HttpMethod method,
			HttpEntity<?> requestEntity, Class<T> responseType,
			HttpServletRequest httpServletRequest, Object... uriVariables) throws RestClientException {

		HttpHeaders headers = getHeaders(httpServletRequest);

		HttpEntity<?> newRequest = new HttpEntity<>(requestEntity.getBody(), headers);

		return super.exchange(baseUrl + url, method, newRequest, responseType, uriVariables);
	}
	
	/**
	 * 
	 * @param url
	 * @param method
	 * @param requestEntity
	 * @param responseType
	 * @param httpServletRequest
	 * @return
	 * @throws RestClientException
	 */
	public <T> ResponseEntity<T> exchange(String url, HttpMethod method,
			HttpEntity<?> requestEntity, Class<T> responseType,
			HttpServletRequest httpServletRequest) throws RestClientException {
		
		return exchange(url, method, requestEntity,
				responseType, httpServletRequest, new Object[] {});		
	}
	
	/**
	 * 
	 * @param url
	 * @param method
	 * @param responseType
	 * @param httpServletRequest
	 * @param uriVariables
	 * @return
	 * @throws RestClientException
	 */
	public <T> ResponseEntity<T> exchange(String url, HttpMethod method,
			Class<T> responseType, final HttpServletRequest httpServletRequest, Object... uriVariables)
			throws RestClientException {

		return exchange(url, method, new HttpEntity<Void>(null, null),
				responseType, httpServletRequest, uriVariables);
	}	

	/**
	 * 
	 * @param url
	 * @param method
	 * @param responseType
	 * @param httpServletRequest
	 * @return
	 * @throws RestClientException
	 */
	public <T> ResponseEntity<T> exchange(String url, HttpMethod method,
			Class<T> responseType, final HttpServletRequest httpServletRequest)
			throws RestClientException {

		return exchange(url, method, new HttpEntity<Void>(null, null),
				responseType, httpServletRequest);
	}	

	private HttpHeaders getHeaders(HttpServletRequest request) {

		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", request.getHeader("Authorization"));

		return headers;
	}
}
