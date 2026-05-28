package kizuna_iam_service.user.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.UUID;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "users",schema = "core")
@Data
@NoArgsConstructor
public class User {
    @Id
    private String keycloakId;
    private String username;
    private String fullName;
    private String email;
    @ElementCollection
    private List<String> roles;



    public User(String keycloakId,String username,String email ,String fullName, List<String> roles) {
        this.email = email;
        this.keycloakId = keycloakId;
        this.username = username;
        this.fullName = fullName;
        this.roles = roles;
    }
}


