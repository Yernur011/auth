package kz.dev.api.adapter.rest.config;

import kz.dev.api.adapter.rest.client.AuthClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

public class RestClientConfig {
    @Value("${app.clients.auth-api.url}")
    public String baseUrl;

    @Bean
    public AuthClient workflowClient() {
        return getFactory().createClient(AuthClient.class);
    }

    public HttpServiceProxyFactory getFactory() {
        RestClient webClient = RestClient.builder().baseUrl(baseUrl).build();
        RestClientAdapter adapter = RestClientAdapter.create(webClient);
        return HttpServiceProxyFactory.builderFor(adapter).build();
    }
}
