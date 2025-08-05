package com.example.mediaservice.service;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MediaCodecServiceTest {
    
    @InjectMocks
    private MediaCodecService mediaCodecService;
    
    @BeforeEach
    void setUp() {
        // 테스트 설정
    }
    
    @Test
    void testEncodeH264Video() {
        // Given
        String inputPath = "test-input.mp4";
        String outputPath = "test-output.mp4";
        int width = 1920;
        int height = 1080;
        int bitrate = 5000;
        
        // When & Then
        assertDoesNotThrow(() -> {
            mediaCodecService.encodeH264Video(inputPath, outputPath, width, height, bitrate);
        });
    }
    
    @Test
    void testEncodeAacAudio() {
        // Given
        String inputPath = "test-input.wav";
        String outputPath = "test-output.aac";
        int sampleRate = 44100;
        int channels = 2;
        int bitrate = 128;
        
        // When & Then
        assertDoesNotThrow(() -> {
            mediaCodecService.encodeAacAudio(inputPath, outputPath, sampleRate, channels, bitrate);
        });
    }
    
    @Test
    void testExtractCodecInfo() {
        // Given
        String mp4File = "test.mp4";
        String aviFile = "test.avi";
        String mp3File = "test.mp3";
        
        // When
        Map<String, Object> mp4Info = mediaCodecService.extractCodecInfo(mp4File);
        Map<String, Object> aviInfo = mediaCodecService.extractCodecInfo(aviFile);
        Map<String, Object> mp3Info = mediaCodecService.extractCodecInfo(mp3File);
        
        // Then
        assertNotNull(mp4Info);
        assertEquals("MP4", mp4Info.get("container"));
        assertEquals("H.264", mp4Info.get("videoCodec"));
        assertEquals("AAC", mp4Info.get("audioCodec"));
        
        assertNotNull(aviInfo);
        assertEquals("AVI", aviInfo.get("container"));
        assertEquals("MPEG-4", aviInfo.get("videoCodec"));
        assertEquals("MP3", aviInfo.get("audioCodec"));
        
        assertNotNull(mp3Info);
        assertEquals("MP3", mp3Info.get("container"));
        assertEquals("MP3", mp3Info.get("audioCodec"));
    }
    
    @Test
    void testAdjustBitrate() {
        // Given
        String inputPath = "test-input.mp4";
        String outputPath = "test-output.mp4";
        int targetBitrate = 2000;
        
        // When & Then
        assertDoesNotThrow(() -> {
            mediaCodecService.adjustBitrate(inputPath, outputPath, targetBitrate);
        });
    }
    
    @Test
    void testResizeVideo() {
        // Given
        String inputPath = "test-input.mp4";
        String outputPath = "test-output.mp4";
        int newWidth = 1280;
        int newHeight = 720;
        
        // When & Then
        assertDoesNotThrow(() -> {
            mediaCodecService.resizeVideo(inputPath, outputPath, newWidth, newHeight);
        });
    }
} 