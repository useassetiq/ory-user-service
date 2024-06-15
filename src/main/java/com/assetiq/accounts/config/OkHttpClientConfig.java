package com.assetiq.accounts.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.okhttp3.OkHttpMetricsEventListener;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class OkHttpClientConfig {

    @Bean
    public OkHttpClient okHttpClient(MeterRegistry meterRegistry,
                                     @Value("${okhttp.connect-timeout:10}") int connectTimeout,
                                     @Value("${okhttp.read-timeout:30}") int readTimeout,
                                     @Value("${okhttp.write-timeout:15}") int writeTimeout) {
        return new OkHttpClient.Builder()
                .connectTimeout(connectTimeout, TimeUnit.SECONDS)
                .readTimeout(readTimeout, TimeUnit.SECONDS)
                .writeTimeout(writeTimeout, TimeUnit.SECONDS)
                .eventListener(OkHttpMetricsEventListener.builder(meterRegistry, "okhttp.requests").build())
                .build();
    }

}
