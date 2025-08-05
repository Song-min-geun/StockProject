package com.example.mediaservice.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DdexService {
    
    /**
     * DDEX NewReleaseMessage를 생성합니다.
     */
    public String createNewReleaseMessage(String isrc, String iswc, String title, String artist, String album) {
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<ernm:NewReleaseMessage xmlns:ernm=\"http://ddex.net/xml/ern/41\" ");
        xml.append("xmlns:ernc=\"http://ddex.net/xml/ern/41/ernc\" ");
        xml.append("MessageSchemaVersionId=\"41\" LanguageAndScriptCode=\"en\">\n");
        
        xml.append("  <MessageHeader>\n");
        xml.append("    <MessageId>").append(UUID.randomUUID().toString()).append("</MessageId>\n");
        xml.append("    <Sender>\n");
        xml.append("      <PartyId>").append("SENDER001").append("</PartyId>\n");
        xml.append("      <PartyName>").append("Media Service").append("</PartyName>\n");
        xml.append("    </Sender>\n");
        xml.append("    <Recipient>\n");
        xml.append("      <PartyId>").append("RECIPIENT001").append("</PartyId>\n");
        xml.append("      <PartyName>").append("Digital Service Provider").append("</PartyName>\n");
        xml.append("    </Recipient>\n");
        xml.append("    <MessageDateTime>").append(LocalDateTime.now()).append("</MessageDateTime>\n");
        xml.append("  </MessageHeader>\n");
        
        xml.append("  <ReleaseList>\n");
        xml.append("    <Release>\n");
        xml.append("      <ReleaseId>\n");
        xml.append("        <ISRC>").append(isrc).append("</ISRC>\n");
        xml.append("      </ReleaseId>\n");
        xml.append("      <ReleaseTitle>\n");
        xml.append("        <TitleText>").append(title).append("</TitleText>\n");
        xml.append("      </ReleaseTitle>\n");
        xml.append("      <ReleaseType>Single</ReleaseType>\n");
        xml.append("      <ReleaseList>\n");
        xml.append("        <Release>\n");
        xml.append("          <ReleaseId>\n");
        xml.append("            <ISWC>").append(iswc).append("</ISWC>\n");
        xml.append("          </ReleaseId>\n");
        xml.append("          <Title>\n");
        xml.append("            <TitleText>").append(title).append("</TitleText>\n");
        xml.append("          </Title>\n");
        xml.append("          <Artist>\n");
        xml.append("            <PartyName>").append(artist).append("</PartyName>\n");
        xml.append("          </Artist>\n");
        xml.append("        </Release>\n");
        xml.append("      </ReleaseList>\n");
        xml.append("    </Release>\n");
        xml.append("  </ReleaseList>\n");
        
        xml.append("</ernm:NewReleaseMessage>");
        
        String xmlContent = xml.toString();
        log.info("DDEX NewReleaseMessage created for ISRC: {}", isrc);
        return xmlContent;
    }
    
    /**
     * DDEX ReleaseUpdateMessage를 생성합니다.
     */
    public String createReleaseUpdateMessage(String isrc, String iswc, String title, String artist) {
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<ernm:ReleaseUpdateMessage xmlns:ernm=\"http://ddex.net/xml/ern/41\" ");
        xml.append("xmlns:ernc=\"http://ddex.net/xml/ern/41/ernc\" ");
        xml.append("MessageSchemaVersionId=\"41\" LanguageAndScriptCode=\"en\">\n");
        
        xml.append("  <MessageHeader>\n");
        xml.append("    <MessageId>").append(UUID.randomUUID().toString()).append("</MessageId>\n");
        xml.append("    <Sender>\n");
        xml.append("      <PartyId>").append("SENDER001").append("</PartyId>\n");
        xml.append("      <PartyName>").append("Media Service").append("</PartyName>\n");
        xml.append("    </Sender>\n");
        xml.append("    <Recipient>\n");
        xml.append("      <PartyId>").append("RECIPIENT001").append("</PartyId>\n");
        xml.append("      <PartyName>").append("Digital Service Provider").append("</PartyName>\n");
        xml.append("    </Recipient>\n");
        xml.append("    <MessageDateTime>").append(LocalDateTime.now()).append("</MessageDateTime>\n");
        xml.append("  </MessageHeader>\n");
        
        xml.append("  <ReleaseList>\n");
        xml.append("    <Release>\n");
        xml.append("      <ReleaseId>\n");
        xml.append("        <ISRC>").append(isrc).append("</ISRC>\n");
        xml.append("      </ReleaseId>\n");
        xml.append("      <ReleaseTitle>\n");
        xml.append("        <TitleText>").append(title).append("</TitleText>\n");
        xml.append("      </ReleaseTitle>\n");
        xml.append("      <Artist>\n");
        xml.append("        <PartyName>").append(artist).append("</PartyName>\n");
        xml.append("      </Artist>\n");
        xml.append("    </Release>\n");
        xml.append("  </ReleaseList>\n");
        
        xml.append("</ernm:ReleaseUpdateMessage>");
        
        String xmlContent = xml.toString();
        log.info("DDEX ReleaseUpdateMessage created for ISRC: {}", isrc);
        return xmlContent;
    }
    
    /**
     * DDEX 메시지를 검증합니다.
     */
    public boolean validateDdexMessage(String xmlContent) {
        try {
            // 기본적인 XML 구조 검증
            if (!xmlContent.contains("<?xml") || 
                !xmlContent.contains("MessageHeader") || 
                !xmlContent.contains("ReleaseList")) {
                log.error("Invalid DDEX message structure");
                return false;
            }
            
            // ISRC 또는 ISWC 포함 여부 확인
            if (!xmlContent.contains("ISRC") && !xmlContent.contains("ISWC")) {
                log.error("DDEX message must contain ISRC or ISWC");
                return false;
            }
            
            log.info("DDEX message validation successful");
            return true;
        } catch (Exception e) {
            log.error("DDEX message validation failed", e);
            return false;
        }
    }
    
    /**
     * DDEX 메시지를 파싱하여 메타데이터를 추출합니다.
     */
    public Map<String, String> parseDdexMessage(String xmlContent) {
        Map<String, String> metadata = new HashMap<>();
        
        try {
            // 간단한 XML 파싱 (실제로는 JAXB를 사용해야 함)
            if (xmlContent.contains("<ISRC>")) {
                int start = xmlContent.indexOf("<ISRC>") + 6;
                int end = xmlContent.indexOf("</ISRC>");
                if (start > 5 && end > start) {
                    metadata.put("isrc", xmlContent.substring(start, end));
                }
            }
            
            if (xmlContent.contains("<ISWC>")) {
                int start = xmlContent.indexOf("<ISWC>") + 6;
                int end = xmlContent.indexOf("</ISWC>");
                if (start > 5 && end > start) {
                    metadata.put("iswc", xmlContent.substring(start, end));
                }
            }
            
            if (xmlContent.contains("<TitleText>")) {
                int start = xmlContent.indexOf("<TitleText>") + 11;
                int end = xmlContent.indexOf("</TitleText>");
                if (start > 10 && end > start) {
                    metadata.put("title", xmlContent.substring(start, end));
                }
            }
            
            if (xmlContent.contains("<PartyName>")) {
                int start = xmlContent.indexOf("<PartyName>") + 11;
                int end = xmlContent.indexOf("</PartyName>");
                if (start > 10 && end > start) {
                    metadata.put("artist", xmlContent.substring(start, end));
                }
            }
            
            log.info("DDEX message parsed successfully");
            return metadata;
        } catch (Exception e) {
            log.error("Failed to parse DDEX message", e);
            return metadata;
        }
    }
    
    /**
     * DDEX 메시지를 전송합니다.
     */
    public boolean sendDdexMessage(String xmlContent, String recipientId) {
        try {
            // 실제 구현에서는 HTTP POST나 메시지 큐를 사용합니다
            log.info("Sending DDEX message to recipient: {}", recipientId);
            
            // 전송 시뮬레이션
            Thread.sleep(100);
            
            log.info("DDEX message sent successfully");
            return true;
        } catch (Exception e) {
            log.error("Failed to send DDEX message", e);
            return false;
        }
    }
    
    /**
     * DDEX 메시지 타입을 식별합니다.
     */
    public String identifyMessageType(String xmlContent) {
        if (xmlContent.contains("NewReleaseMessage")) {
            return "NewReleaseMessage";
        } else if (xmlContent.contains("ReleaseUpdateMessage")) {
            return "ReleaseUpdateMessage";
        } else if (xmlContent.contains("ReleaseDeleteMessage")) {
            return "ReleaseDeleteMessage";
        } else {
            return "Unknown";
        }
    }
} 