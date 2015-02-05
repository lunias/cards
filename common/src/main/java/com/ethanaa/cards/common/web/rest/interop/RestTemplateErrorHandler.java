package com.ethanaa.cards.common.web.rest.interop;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;

import com.ethanaa.cards.common.web.rest.util.RestUtil;

public class RestTemplateErrorHandler implements ResponseErrorHandler {

	private static final Logger log = LoggerFactory.getLogger(RestTemplateErrorHandler.class);
	
	@Override
	public boolean hasError(ClientHttpResponse response) throws IOException {
		
		return RestUtil.isError(response.getStatusCode());		
	}

	@Override
	public void handleError(ClientHttpResponse response) throws IOException {
		
		log.error("Response error: {} {}", response.getStatusCode(), response.getStatusText());		
	}

}
