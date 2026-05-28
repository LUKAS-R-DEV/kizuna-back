package com.kizuna.data_service.integration.apiKey.service;

import com.kizuna.data_service.integration.apiKey.domain.ApiKey;
import com.kizuna.data_service.integration.apiKey.repository.ApiKeyRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApiKeyServiceTest {

    @Mock
    private ApiKeyRepository repository;

    @InjectMocks
    private ApiKeyService apiKeyService;

    @Test
    void isValidReturnsTrueForActiveKey() {
        when(repository.findByKeyAndActiveTrue("kz_live_test"))
                .thenReturn(Optional.of(ApiKey.builder().key("kz_live_test").active(true).build()));

        assertThat(apiKeyService.isValid("kz_live_test")).isTrue();
    }

    @Test
    void isValidRejectsBlankKey() {
        assertThat(apiKeyService.isValid(" ")).isFalse();
    }
}
