package com.example.busbookingmanagement.DTO.request;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleRequestDTO {
    private String roleName;
    private String description;
}
