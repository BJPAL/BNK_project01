package com.example.fund.admin;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name="tbl_admin")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class Admin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer admin_id;

    @Column(length = 20)
    private String adminname;

    @Column(length = 20)
    private String password;

    private String name;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private AdminRole role;  // ← 여기에 enum 사용!

}
