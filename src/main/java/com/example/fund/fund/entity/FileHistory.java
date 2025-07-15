package com.example.fund.fund.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class FileHistory {

    @Id @GeneratedValue
    private Long id;

    private String originalFilename;
    private String storedFilename;
    private String fileType;     // manual | prospectus | terms   
    private String filePath;
    private LocalDateTime uploadedAt;
}