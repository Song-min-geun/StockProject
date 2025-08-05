package com.example.mediaservice.domain;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "media_files")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MediaFile {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String fileName;
    
    @Column(nullable = false)
    private String originalFileName;
    
    @Column(nullable = false)
    private String filePath;
    
    @Column(nullable = false)
    private String fileType; // VIDEO, AUDIO, IMAGE
    
    @Column(nullable = false)
    private String mimeType;
    
    @Column(nullable = false)
    private Long fileSize;
    
    // 비디오/오디오 메타데이터
    private String codec;
    private String container;
    private Integer duration; // 초 단위
    private Integer bitrate;
    private Integer width; // 비디오인 경우
    private Integer height; // 비디오인 경우
    private Integer sampleRate; // 오디오인 경우
    private Integer channels; // 오디오인 경우
    
    // 스트리밍 관련
    private String hlsUrl;
    private String dashUrl;
    private Boolean isStreamingReady;
    
    // DDEX 관련
    private String ddexId;
    private String isrc; // International Standard Recording Code
    private String iswc; // International Standard Musical Work Code
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    private LocalDateTime processedAt;
    
    @Enumerated(EnumType.STRING)
    private ProcessingStatus status;
    
    public enum ProcessingStatus {
        UPLOADED,
        PROCESSING,
        COMPLETED,
        FAILED
    }
} 