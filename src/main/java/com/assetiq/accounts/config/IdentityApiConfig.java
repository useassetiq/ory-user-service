package com.assetiq.accounts.config;

import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import sh.ory.kratos.ApiClient;
import sh.ory.kratos.api.IdentityApi;

@Configuration
public class IdentityApiConfig {

    @Bean
    public IdentityApi identityApi(ApiClient apiClient) {
        return new IdentityApi(apiClient);
    }

    @Bean
    public ApiClient apiClient(OkHttpClient okHttpClient,
                               @Value("${kratos.url}") String basePath) {
        return new ApiClient(okHttpClient)
                .setBasePath(basePath);
    }
}
