package com.karasov.file_service.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
@Table(name = "files")
public class FileEntity {
    @Id
    @Column(name = "name", nullable = false)
    private String name;
    @Lob
    @Column(name = "bytes", nullable = false)
    private byte[] bytes;
    @Column(name = "size", nullable = false)
    private Long size;
}
