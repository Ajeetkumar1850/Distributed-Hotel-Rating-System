package com.micro.hotel.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document("hotels")
public class Hotel {

    @Id
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String hotelId;

    private String name;

    private String location;

    private String about;
}
