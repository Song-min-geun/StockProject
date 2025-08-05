package com.example.mediaservice.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.mediaservice.service.DdexService;
import com.example.mediaservice.service.MediaCodecService;
import com.example.mediaservice.service.StreamingService;

@RestController
@RequestMapping("/api/media")
public class MediaController {
    
    @Autowired
    private MediaCodecService mediaCodecService;
    
    @Autowired
    private DdexService ddexService;
    
    @Autowired
    private StreamingService streamingService;
    
    /**
     * 미디어 파일 업로드 및 처리
     */
    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadMedia(@RequestParam("file") MultipartFile file) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 파일 정보 추출
            String fileName = file.getOriginalFilename();
            long fileSize = file.getSize();
            String contentType = file.getContentType();
            
            // 코덱 정보 추출
            Map<String, Object> codecInfo = mediaCodecService.extractCodecInfo(fileName);
            
            response.put("success", true);
            response.put("fileName", fileName);
            response.put("fileSize", fileSize);
            response.put("contentType", contentType);
            response.put("codecInfo", codecInfo);
            
            System.out.println("Media file uploaded: " + fileName);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            System.err.println("Upload failed: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * H.264 비디오 인코딩
     */
    @PostMapping("/encode/h264")
    public ResponseEntity<Map<String, Object>> encodeH264(
            @RequestParam String inputPath,
            @RequestParam String outputPath,
            @RequestParam(defaultValue = "1920") int width,
            @RequestParam(defaultValue = "1080") int height,
            @RequestParam(defaultValue = "5000") int bitrate) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            mediaCodecService.encodeH264Video(inputPath, outputPath, width, height, bitrate);
            
            response.put("success", true);
            response.put("message", "H.264 encoding completed");
            response.put("outputPath", outputPath);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * AAC 오디오 인코딩
     */
    @PostMapping("/encode/aac")
    public ResponseEntity<Map<String, Object>> encodeAac(
            @RequestParam String inputPath,
            @RequestParam String outputPath,
            @RequestParam(defaultValue = "44100") int sampleRate,
            @RequestParam(defaultValue = "2") int channels,
            @RequestParam(defaultValue = "128") int bitrate) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            mediaCodecService.encodeAacAudio(inputPath, outputPath, sampleRate, channels, bitrate);
            
            response.put("success", true);
            response.put("message", "AAC encoding completed");
            response.put("outputPath", outputPath);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * DDEX NewReleaseMessage 생성
     */
    @PostMapping("/ddex/new-release")
    public ResponseEntity<Map<String, Object>> createNewReleaseMessage(
            @RequestParam String isrc,
            @RequestParam String iswc,
            @RequestParam String title,
            @RequestParam String artist,
            @RequestParam String album) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            String xmlContent = ddexService.createNewReleaseMessage(isrc, iswc, title, artist, album);
            
            response.put("success", true);
            response.put("xmlContent", xmlContent);
            response.put("messageType", "NewReleaseMessage");
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * DDEX 메시지 검증
     */
    @PostMapping("/ddex/validate")
    public ResponseEntity<Map<String, Object>> validateDdexMessage(@RequestBody String xmlContent) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            boolean isValid = ddexService.validateDdexMessage(xmlContent);
            
            response.put("success", true);
            response.put("isValid", isValid);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * DDEX 메시지 파싱
     */
    @PostMapping("/ddex/parse")
    public ResponseEntity<Map<String, Object>> parseDdexMessage(@RequestBody String xmlContent) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Map<String, String> metadata = ddexService.parseDdexMessage(xmlContent);
            String messageType = ddexService.identifyMessageType(xmlContent);
            
            response.put("success", true);
            response.put("metadata", metadata);
            response.put("messageType", messageType);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * HLS 스트리밍 생성
     */
    @PostMapping("/streaming/hls")
    public ResponseEntity<Map<String, Object>> createHlsStream(
            @RequestParam String outputDir,
            @RequestParam(defaultValue = "playlist.m3u8") String playlistName,
            @RequestParam(defaultValue = "10") int segmentDuration,
            @RequestParam(defaultValue = "10") int targetDuration) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            streamingService.createHlsPlaylist(outputDir, playlistName, segmentDuration, targetDuration);
            
            response.put("success", true);
            response.put("playlistUrl", outputDir + "/" + playlistName);
            response.put("message", "HLS playlist created");
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * DASH 스트리밍 생성
     */
    @PostMapping("/streaming/dash")
    public ResponseEntity<Map<String, Object>> createDashStream(
            @RequestParam String outputDir,
            @RequestParam(defaultValue = "manifest.mpd") String manifestName) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            streamingService.createDashManifest(outputDir, manifestName, null, null);
            
            response.put("success", true);
            response.put("manifestUrl", outputDir + "/" + manifestName);
            response.put("message", "DASH manifest created");
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 스트리밍 상태 모니터링
     */
    @GetMapping("/streaming/status")
    public ResponseEntity<Map<String, Object>> getStreamingStatus(@RequestParam String streamUrl) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            StreamingService.StreamingStatus status = streamingService.monitorStreamingStatus(streamUrl);
            
            response.put("success", true);
            response.put("status", status);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 비디오 리사이징
     */
    @PostMapping("/resize")
    public ResponseEntity<Map<String, Object>> resizeVideo(
            @RequestParam String inputPath,
            @RequestParam String outputPath,
            @RequestParam int newWidth,
            @RequestParam int newHeight) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            mediaCodecService.resizeVideo(inputPath, outputPath, newWidth, newHeight);
            
            response.put("success", true);
            response.put("message", "Video resizing completed");
            response.put("newDimensions", newWidth + "x" + newHeight);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 비트레이트 조정
     */
    @PostMapping("/adjust-bitrate")
    public ResponseEntity<Map<String, Object>> adjustBitrate(
            @RequestParam String inputPath,
            @RequestParam String outputPath,
            @RequestParam int targetBitrate) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            mediaCodecService.adjustBitrate(inputPath, outputPath, targetBitrate);
            
            response.put("success", true);
            response.put("message", "Bitrate adjustment completed");
            response.put("targetBitrate", targetBitrate);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }
} 