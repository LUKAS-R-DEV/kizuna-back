package Kizuna_core_service.productionOrder.service;

import Kizuna_core_service.shared.exception.BusinessException;
import Kizuna_core_service.shared.exception.NotFoundException;
import Kizuna_core_service.shared.integration.IamClient;
import Kizuna_core_service.shared.integration.UserResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Component
@Service
@RequiredArgsConstructor
public class OperatorValidateService {
    private final IamClient iamClient;

    public UserResponseDto validateOperator(String operatorId) {
        UserResponseDto user = iamClient.getUserById(operatorId);
        if(user==null){
            throw new NotFoundException("User not found");

        }
        if (!hasOperatorRole(user.roles())) {
            throw new BusinessException("User is not an operator");
        }
        return user;
    }

    private boolean hasOperatorRole(java.util.List<String> roles) {
        if (roles == null || roles.isEmpty()) {
            return false;
        }
        return roles.stream()
                .map(String::toUpperCase)
                .filter(r -> !r.startsWith("DEFAULT-ROLES"))
                .filter(r -> !r.equals("OFFLINE_ACCESS"))
                .filter(r -> !r.equals("UMA_AUTHORIZATION"))
                .anyMatch(r -> r.equals("OPERATOR"));
    }
}
