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
@Table(name = "ddex_messages")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DdexMessage {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String messageId;
    
    @Column(nullable = false)
    private String messageType; // NewReleaseMessage, ReleaseUpdateMessage 등
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String xmlContent;
    
    @Column(nullable = false)
    private String senderId;
    
    @Column(nullable = false)
    private String recipientId;
    
    @Column(nullable = false)
    private LocalDateTime messageDateTime;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Enumerated(EnumType.STRING)
    private DdexStatus status;
    
    private String errorMessage;
    
    public enum DdexStatus {
        PENDING,
        SENT,
        FAILED,
        ACKNOWLEDGED
    }
} 