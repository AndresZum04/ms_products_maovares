package com.maovares.ms_products.product.infraestructure.http.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.maovares.ms_products.product.infraestructure.http.filter.ClientCertValidationFilter;

@Configuration
public class FilterConfig {

    @Bean
    public FilterRegistrationBean<ClientCertValidationFilter> clientCertValidationFilter() {
        FilterRegistrationBean<ClientCertValidationFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new ClientCertValidationFilter());
        registrationBean.addUrlPatterns("/*");
        registrationBean.setOrder(2);
        registrationBean.setName("clientCertValidationFilter");
        return registrationBean;
    }
}
