package com.kizuna.data_service.integration.apiKey.service;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
@Component
public class ApiKeyGeneratorService {
    private static final String CHARACTERS =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    private static final SecureRandom random =
            new SecureRandom();

    public static String generate() {

        StringBuilder key =
                new StringBuilder("kz_live_");

        for (int i = 0; i < 32; i++) {

            int index =
                    random.nextInt(CHARACTERS.length());

            key.append(
                    CHARACTERS.charAt(index)
            );
        }

        return key.toString();
    }
}
