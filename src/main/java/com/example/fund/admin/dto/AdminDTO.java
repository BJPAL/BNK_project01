package com.example.fund.admin.dto;


import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class AdminDTO {

    private Integer admin_id;
    private String adminname;
    private String password;
    private String name;
    private String role;
}
