package Kizuna_core_service.shared.integration;

import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Component
@Service
public class KeycloakValidation {
    private RestTemplate restTemplate = new RestTemplate();

    public boolean isValidSignature(String Username, String password){
        String url= "http://localhost:8081/realms/Kizuna/protocol/openid-connect/token";


        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String,String> map = new LinkedMultiValueMap<>();
        map.add("grant_type","password");
        map.add("client_id","kizuna-app");
        map.add("username",Username);
        map.add("password",password);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

        try{
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

            return response.getStatusCode() == HttpStatus.OK;

        }catch (Exception e){
            return false;
        }


    }




}
