package com.micro.user.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document("users")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String userId;

    private String name;

    @Email(message = "Email not valid")
    @Indexed(unique = true)
    private String email;

    @Indexed(unique = true)
    @Size(min = 10, max = 10, message = "Phone Number should be of 10 digits")
    private String phone;

    @Transient
    private List<Rating> ratings;
}
