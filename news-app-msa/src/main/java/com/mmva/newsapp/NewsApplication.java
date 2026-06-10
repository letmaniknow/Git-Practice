package com.mmva.newsapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.http.client.HttpClientAutoConfiguration;
import org.springframework.boot.autoconfigure.web.client.RestClientAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = { "com.mmva.newsapp" }, exclude = {
		HttpClientAutoConfiguration.class, // Use our custom HttpClientConfig instead
		RestClientAutoConfiguration.class // Use our custom RestClient configuration
})
@EnableCaching
@EnableScheduling
public class NewsApplication {

	public static void main(String[] args) {
		SpringApplication.run(NewsApplication.class, args);
	}

}
