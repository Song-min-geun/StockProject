package com.example.mediaservice.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.mediaservice.domain.MediaFile;

@Repository
public interface MediaFileRepository extends JpaRepository<MediaFile, Long> {
    
    /**
     * 파일 타입별로 미디어 파일을 조회합니다.
     */
    List<MediaFile> findByFileType(String fileType);
    
    /**
     * 코덱별로 미디어 파일을 조회합니다.
     */
    List<MediaFile> findByCodec(String codec);
    
    /**
     * 스트리밍 준비가 완료된 파일들을 조회합니다.
     */
    List<MediaFile> findByIsStreamingReadyTrue();
    
    /**
     * ISRC로 미디어 파일을 조회합니다.
     */
    Optional<MediaFile> findByIsrc(String isrc);
    
    /**
     * DDEX ID로 미디어 파일을 조회합니다.
     */
    Optional<MediaFile> findByDdexId(String ddexId);
    
    /**
     * 파일명으로 미디어 파일을 조회합니다.
     */
    Optional<MediaFile> findByFileName(String fileName);
} 