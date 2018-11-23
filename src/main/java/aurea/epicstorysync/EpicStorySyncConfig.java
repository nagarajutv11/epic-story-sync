package aurea.epicstorysync;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

@Configuration
public class EpicStorySyncConfig {

    @Bean
    @Profile("production")
    public RestTemplate restTemplate(@Value("${jira.username}") String username,
            @Value("${jira.password}") String password) {
        RestTemplate restTemplate = new RestTemplate();
        if (!StringUtils.isEmpty(username) && !StringUtils.isEmpty(password)) {
            restTemplate.getInterceptors().add(new BasicAuthenticationInterceptor(username, password));
        }
        return restTemplate;
    }
}
