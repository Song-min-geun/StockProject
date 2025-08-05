package com.example.mediaservice.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.mediaservice.domain.DdexMessage;

@Repository
public interface DdexMessageRepository extends JpaRepository<DdexMessage, Long> {
    
    /**
     * 메시지 타입별로 DDEX 메시지를 조회합니다.
     */
    List<DdexMessage> findByMessageType(String messageType);
    
    /**
     * 상태별로 DDEX 메시지를 조회합니다.
     */
    List<DdexMessage> findByStatus(DdexMessage.DdexStatus status);
    
    /**
     * 발신자별로 DDEX 메시지를 조회합니다.
     */
    List<DdexMessage> findBySenderId(String senderId);
    
    /**
     * 수신자별로 DDEX 메시지를 조회합니다.
     */
    List<DdexMessage> findByRecipientId(String recipientId);
    
    /**
     * 메시지 ID로 DDEX 메시지를 조회합니다.
     */
    Optional<DdexMessage> findByMessageId(String messageId);
    
    /**
     * 특정 기간 동안의 DDEX 메시지를 조회합니다.
     */
    List<DdexMessage> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * 실패한 DDEX 메시지를 조회합니다.
     */
    List<DdexMessage> findByStatusAndErrorMessageIsNotNull(DdexMessage.DdexStatus status);
} 