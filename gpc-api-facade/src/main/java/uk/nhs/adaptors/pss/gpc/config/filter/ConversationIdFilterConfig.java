package uk.nhs.adaptors.pss.gpc.config.filter;


import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConversationIdFilterConfig {

    @Bean
    public FilterRegistrationBean<ConversationIdFilter> servletRegistrationBean(ConversationIdFilter filter) {
        final FilterRegistrationBean<ConversationIdFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(filter);
        return registrationBean;
    }
}
