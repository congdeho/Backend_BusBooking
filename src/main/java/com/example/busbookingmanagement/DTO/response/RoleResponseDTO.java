package com.example.busbookingmanagement.DTO.response;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleResponseDTO {
    private Long roleId;
    private String roleName;
    private String description;
}
