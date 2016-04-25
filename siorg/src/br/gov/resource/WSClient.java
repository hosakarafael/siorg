package br.gov.resource;


import java.net.URI;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;

public class WSClient {
	public void getTodosServidores() {
		ClientConfig config = new DefaultClientConfig();
		Client client = Client.create(config);
		WebResource service = client.resource(getBaseURI());
		service.path("rest").path("service").path("todosServidores").accept(MediaType.TEXT_XML).get(String.class);
	}
	
	private static URI getBaseURI() {
		return UriBuilder.fromUri("http://localhost:8080/siorgdf").build();
	}

}
