package uk.gov.hmcts.reform.dev.controllers;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import uk.gov.hmcts.reform.dev.security.JwtAuthFilter;
import uk.gov.hmcts.reform.dev.security.JwtUtil;
import uk.gov.hmcts.reform.dev.services.CaseService;
import uk.gov.hmcts.reform.dev.repositories.CaseRepository;

import static org.mockito.Mockito.mock;

@TestConfiguration
public class CaseControllerTestConfig {

    @Bean
    @Primary
    public JwtAuthFilter jwtAuthFilter() {
        return mock(JwtAuthFilter.class);
    }

    @Bean
    @Primary
    public CaseService caseService() {
        return mock(CaseService.class);
    }

    @Bean
    @Primary
    public CaseRepository caseRepository() {
        return mock(CaseRepository.class);
    }

    @Bean
    @Primary
    public JwtUtil jwtUtil() {
        return mock(JwtUtil.class);
    }

    @Bean
    @Primary
    public UserDetailsService userDetailsService() {
        return mock(UserDetailsService.class);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
