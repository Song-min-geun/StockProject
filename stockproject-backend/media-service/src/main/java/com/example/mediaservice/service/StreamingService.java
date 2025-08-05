package com.example.mediaservice.service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class StreamingService {
    
    /**
     * HLS (HTTP Live Streaming) 플레이리스트를 생성합니다.
     */
    public void createHlsPlaylist(String outputDir, String playlistName, int segmentDuration, int targetDuration) {
        try {
            Path playlistPath = Paths.get(outputDir, playlistName);
            StringBuilder playlist = new StringBuilder();
            
            playlist.append("#EXTM3U\n");
            playlist.append("#EXT-X-VERSION:3\n");
            playlist.append("#EXT-X-TARGETDURATION:").append(targetDuration).append("\n");
            playlist.append("#EXT-X-MEDIA-SEQUENCE:0\n");
            
            // 세그먼트 파일들을 찾아서 플레이리스트에 추가
            File dir = new File(outputDir);
            File[] segmentFiles = dir.listFiles((d, name) -> name.endsWith(".ts"));
            
            if (segmentFiles != null) {
                for (File segment : segmentFiles) {
                    playlist.append("#EXTINF:").append(segmentDuration).append(",\n");
                    playlist.append(segment.getName()).append("\n");
                }
            }
            
            playlist.append("#EXT-X-ENDLIST\n");
            
            // 플레이리스트 파일 저장
            try (FileWriter writer = new FileWriter(playlistPath.toFile())) {
                writer.write(playlist.toString());
            }
            
            System.out.println("HLS playlist created: " + playlistPath);
        } catch (IOException e) {
            System.err.println("Failed to create HLS playlist: " + e.getMessage());
        }
    }
    
    /**
     * DASH (Dynamic Adaptive Streaming over HTTP) 매니페스트를 생성합니다.
     */
    public void createDashManifest(String outputDir, String manifestName, List<String> videoSegments, List<String> audioSegments) {
        try {
            Path manifestPath = Paths.get(outputDir, manifestName);
            StringBuilder manifest = new StringBuilder();
            
            manifest.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            manifest.append("<MPD xmlns=\"urn:mpeg:dash:schema:mpd:2011\" ");
            manifest.append("profiles=\"urn:mpeg:dash:profile:isoff-live:2011\" ");
            manifest.append("type=\"static\" ");
            manifest.append("mediaPresentationDuration=\"PT30S\" ");
            manifest.append("minBufferTime=\"PT2S\">\n");
            
            manifest.append("  <Period>\n");
            
            // 비디오 어댑테이션 세트
            manifest.append("    <AdaptationSet mimeType=\"video/mp4\" ");
            manifest.append("segmentAlignment=\"true\" ");
            manifest.append("startWithSAP=\"1\">\n");
            manifest.append("      <SegmentTemplate ");
            manifest.append("timescale=\"1000\" ");
            manifest.append("media=\"video_$Number$.mp4\" ");
            manifest.append("initialization=\"video_init.mp4\"/>\n");
            manifest.append("      <Representation ");
            manifest.append("id=\"video\" ");
            manifest.append("width=\"1920\" ");
            manifest.append("height=\"1080\" ");
            manifest.append("bandwidth=\"5000000\" ");
            manifest.append("codecs=\"avc1.640028\"/>\n");
            manifest.append("    </AdaptationSet>\n");
            
            // 오디오 어댑테이션 세트
            manifest.append("    <AdaptationSet mimeType=\"audio/mp4\" ");
            manifest.append("segmentAlignment=\"true\" ");
            manifest.append("startWithSAP=\"1\">\n");
            manifest.append("      <SegmentTemplate ");
            manifest.append("timescale=\"1000\" ");
            manifest.append("media=\"audio_$Number$.mp4\" ");
            manifest.append("initialization=\"audio_init.mp4\"/>\n");
            manifest.append("      <Representation ");
            manifest.append("id=\"audio\" ");
            manifest.append("bandwidth=\"128000\" ");
            manifest.append("codecs=\"mp4a.40.2\"/>\n");
            manifest.append("    </AdaptationSet>\n");
            
            manifest.append("  </Period>\n");
            manifest.append("</MPD>");
            
            // 매니페스트 파일 저장
            try (FileWriter writer = new FileWriter(manifestPath.toFile())) {
                writer.write(manifest.toString());
            }
            
            System.out.println("DASH manifest created: " + manifestPath);
        } catch (IOException e) {
            System.err.println("Failed to create DASH manifest: " + e.getMessage());
        }
    }
    
    /**
     * 스트리밍 세그먼트를 생성합니다.
     */
    public void createStreamingSegments(String inputPath, String outputDir, String format, int segmentDuration) {
        try {
            // 세그먼트 파일 생성 시뮬레이션
            File outputDirectory = new File(outputDir);
            if (!outputDirectory.exists()) {
                outputDirectory.mkdirs();
            }
            
            // 세그먼트 파일들을 생성
            for (int i = 1; i <= 10; i++) {
                String segmentName = String.format("segment_%03d.%s", i, format);
                Path segmentPath = Paths.get(outputDir, segmentName);
                
                // 더미 세그먼트 파일 생성
                Files.write(segmentPath, ("Segment " + i).getBytes());
            }
            
            System.out.println("Streaming segments created in: " + outputDir);
        } catch (IOException e) {
            System.err.println("Failed to create streaming segments: " + e.getMessage());
        }
    }
    
    /**
     * 스트리밍 품질을 조정합니다.
     */
    public void adjustStreamingQuality(String inputPath, String outputPath, int targetBitrate, int width, int height) {
        System.out.println("Adjusting streaming quality: " + inputPath + " -> " + outputPath);
        System.out.println("Target: " + width + "x" + height + " @ " + targetBitrate + "kbps");
        
        try {
            // 품질 조정 시뮬레이션
            Thread.sleep(500);
            System.out.println("Streaming quality adjustment completed");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Quality adjustment interrupted");
        }
    }
    
    /**
     * 스트리밍 상태를 모니터링합니다.
     */
    public StreamingStatus monitorStreamingStatus(String streamUrl) {
        StreamingStatus status = new StreamingStatus();
        status.setStreamUrl(streamUrl);
        status.setActive(true);
        status.setCurrentViewers(150);
        status.setBandwidthUsage(5000000); // 5Mbps
        status.setLatency(50); // 50ms
        
        System.out.println("Streaming status monitored: " + streamUrl);
        return status;
    }
    
    /**
     * 스트리밍 상태를 나타내는 내부 클래스
     */
    public static class StreamingStatus {
        private String streamUrl;
        private boolean active;
        private int currentViewers;
        private int bandwidthUsage;
        private int latency;
        
        // Getters and Setters
        public String getStreamUrl() { return streamUrl; }
        public void setStreamUrl(String streamUrl) { this.streamUrl = streamUrl; }
        
        public boolean isActive() { return active; }
        public void setActive(boolean active) { this.active = active; }
        
        public int getCurrentViewers() { return currentViewers; }
        public void setCurrentViewers(int currentViewers) { this.currentViewers = currentViewers; }
        
        public int getBandwidthUsage() { return bandwidthUsage; }
        public void setBandwidthUsage(int bandwidthUsage) { this.bandwidthUsage = bandwidthUsage; }
        
        public int getLatency() { return latency; }
        public void setLatency(int latency) { this.latency = latency; }
    }
} 