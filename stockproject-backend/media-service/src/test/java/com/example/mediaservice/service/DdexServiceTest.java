package com.example.mediaservice.service;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DdexServiceTest {
    
    @InjectMocks
    private DdexService ddexService;
    
    @BeforeEach
    void setUp() {
        // 테스트 설정
    }
    
    @Test
    void testCreateNewReleaseMessage() {
        // Given
        String isrc = "ISRC123456789";
        String iswc = "ISWC123456789";
        String title = "Test Song";
        String artist = "Test Artist";
        String album = "Test Album";
        
        // When
        String xmlContent = ddexService.createNewReleaseMessage(isrc, iswc, title, artist, album);
        
        // Then
        assertNotNull(xmlContent);
        assertTrue(xmlContent.contains("NewReleaseMessage"));
        assertTrue(xmlContent.contains(isrc));
        assertTrue(xmlContent.contains(iswc));
        assertTrue(xmlContent.contains(title));
        assertTrue(xmlContent.contains(artist));
        assertTrue(xmlContent.contains(album));
    }
    
    @Test
    void testCreateReleaseUpdateMessage() {
        // Given
        String isrc = "ISRC123456789";
        String iswc = "ISWC123456789";
        String title = "Updated Song";
        String artist = "Updated Artist";
        
        // When
        String xmlContent = ddexService.createReleaseUpdateMessage(isrc, iswc, title, artist);
        
        // Then
        assertNotNull(xmlContent);
        assertTrue(xmlContent.contains("ReleaseUpdateMessage"));
        assertTrue(xmlContent.contains(isrc));
        assertTrue(xmlContent.contains(iswc));
        assertTrue(xmlContent.contains(title));
        assertTrue(xmlContent.contains(artist));
    }
    
    @Test
    void testValidateDdexMessage_Valid() {
        // Given
        String validXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<ernm:NewReleaseMessage xmlns:ernm=\"http://ddex.net/xml/ern/41\">\n" +
                "  <MessageHeader>\n" +
                "    <MessageId>TEST123</MessageId>\n" +
                "  </MessageHeader>\n" +
                "  <ReleaseList>\n" +
                "    <Release>\n" +
                "      <ReleaseId>\n" +
                "        <ISRC>ISRC123456789</ISRC>\n" +
                "      </ReleaseId>\n" +
                "    </Release>\n" +
                "  </ReleaseList>\n" +
                "</ernm:NewReleaseMessage>";
        
        // When
        boolean isValid = ddexService.validateDdexMessage(validXml);
        
        // Then
        assertTrue(isValid);
    }
    
    @Test
    void testValidateDdexMessage_Invalid() {
        // Given
        String invalidXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<invalid>No DDEX structure</invalid>";
        
        // When
        boolean isValid = ddexService.validateDdexMessage(invalidXml);
        
        // Then
        assertFalse(isValid);
    }
    
    @Test
    void testParseDdexMessage() {
        // Given
        String xmlContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<ernm:NewReleaseMessage xmlns:ernm=\"http://ddex.net/xml/ern/41\">\n" +
                "  <ReleaseList>\n" +
                "    <Release>\n" +
                "      <ReleaseId>\n" +
                "        <ISRC>ISRC123456789</ISRC>\n" +
                "        <ISWC>ISWC123456789</ISWC>\n" +
                "      </ReleaseId>\n" +
                "      <ReleaseTitle>\n" +
                "        <TitleText>Test Song</TitleText>\n" +
                "      </ReleaseTitle>\n" +
                "      <Artist>\n" +
                "        <PartyName>Test Artist</PartyName>\n" +
                "      </Artist>\n" +
                "    </Release>\n" +
                "  </ReleaseList>\n" +
                "</ernm:NewReleaseMessage>";
        
        // When
        Map<String, String> metadata = ddexService.parseDdexMessage(xmlContent);
        
        // Then
        assertNotNull(metadata);
        assertEquals("ISRC123456789", metadata.get("isrc"));
        assertEquals("ISWC123456789", metadata.get("iswc"));
        assertEquals("Test Song", metadata.get("title"));
        assertEquals("Test Artist", metadata.get("artist"));
    }
    
    @Test
    void testIdentifyMessageType_NewRelease() {
        // Given
        String xmlContent = "<?xml version=\"1.0\"?>\n" +
                "<ernm:NewReleaseMessage xmlns:ernm=\"http://ddex.net/xml/ern/41\">\n" +
                "</ernm:NewReleaseMessage>";
        
        // When
        String messageType = ddexService.identifyMessageType(xmlContent);
        
        // Then
        assertEquals("NewReleaseMessage", messageType);
    }
    
    @Test
    void testIdentifyMessageType_ReleaseUpdate() {
        // Given
        String xmlContent = "<?xml version=\"1.0\"?>\n" +
                "<ernm:ReleaseUpdateMessage xmlns:ernm=\"http://ddex.net/xml/ern/41\">\n" +
                "</ernm:ReleaseUpdateMessage>";
        
        // When
        String messageType = ddexService.identifyMessageType(xmlContent);
        
        // Then
        assertEquals("ReleaseUpdateMessage", messageType);
    }
    
    @Test
    void testSendDdexMessage() {
        // Given
        String xmlContent = "<?xml version=\"1.0\"?>\n" +
                "<ernm:NewReleaseMessage xmlns:ernm=\"http://ddex.net/xml/ern/41\">\n" +
                "</ernm:NewReleaseMessage>";
        String recipientId = "RECIPIENT001";
        
        // When
        boolean success = ddexService.sendDdexMessage(xmlContent, recipientId);
        
        // Then
        assertTrue(success);
    }
} 