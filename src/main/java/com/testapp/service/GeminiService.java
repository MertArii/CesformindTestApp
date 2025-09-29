package com.testapp.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class GeminiService {
    
    @Value("${gemini.api.key}")
    private String apiKey;
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final String MODEL_NAME = "gemini-2.5-flash-image-preview";

    public GeminiService() {
        this.restTemplate = new RestTemplate();
    }

    public String generateImage(String prompt) {
        try {
            // Prompt boyutunu kontrol et ve optimize et
            String optimizedPrompt = optimizePrompt(prompt);
            System.out.println("🎨 Gemini ile resim üretiliyor (optimized): " + optimizedPrompt.substring(0, Math.min(100, optimizedPrompt.length())) + "...");
            
            String url = "https://generativelanguage.googleapis.com/v1beta/models/" + MODEL_NAME + ":generateContent?key=" + apiKey;
            
            // Gemini için optimize edilmiş request
            String requestBody = String.format("""
                {
                    "contents": [{
                        "parts": [{
                            "text": "Create a high-quality, detailed image: %s"
                        }]
                    }],
                    "generationConfig": {
                        "responseModalities": ["IMAGE"],
                        "temperature": 0.7,
                        "topK": 40,
                        "topP": 0.95,
                        "maxOutputTokens": 1024
                    }
                }
                """, optimizedPrompt);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

            System.out.println("📡 Gemini API Response alındı");
            System.out.println("📊 Response boyutu: " + response.getBody().length() + " karakter");
            
            // Response'u işle
            JsonNode root = objectMapper.readTree(response.getBody());
            System.out.println("🔍 Response JSON yapısı: " + root.toPrettyString().substring(0, Math.min(500, root.toPrettyString().length())));
            
            JsonNode candidates = root.path("candidates");
            System.out.println("🎯 Candidates sayısı: " + candidates.size());

            if (candidates.isArray() && candidates.size() > 0) {
                JsonNode content = candidates.get(0).path("content");
                System.out.println("📝 Content yapısı: " + content.toPrettyString().substring(0, Math.min(300, content.toPrettyString().length())));
                
                JsonNode parts = content.path("parts");
                System.out.println("🧩 Parts sayısı: " + parts.size());
                
                if (parts.isArray() && parts.size() > 0) {
                    for (int i = 0; i < parts.size(); i++) {
                        JsonNode part = parts.get(i);
                        System.out.println("🔍 Part " + i + " yapısı: " + part.toPrettyString().substring(0, Math.min(200, part.toPrettyString().length())));
                        
                        JsonNode inlineData = part.path("inlineData");
                        if (!inlineData.isMissingNode() && !inlineData.path("data").isMissingNode()) {
                            String base64Data = inlineData.path("data").asText();
                            String mimeType = inlineData.path("mimeType").asText();
                            System.out.println("🖼️ Resim bulundu! MIME: " + mimeType + ", Base64 boyutu: " + base64Data.length());
                            
            // Resim boyutunu kontrol et
            String dataUrl = "data:" + mimeType + ";base64," + base64Data;
            if (dataUrl.length() > 4000000) { // 4MB'den büyükse
                System.out.println("⚠️ Resim çok büyük (" + dataUrl.length() + " karakter), basit placeholder kullanılıyor");
                return generateSimplePlaceholder(prompt);
            }
            
            // Base64 verisinin geçerliliğini kontrol et
            if (base64Data.length() > 3000000) { // 3MB'den büyük base64
                System.out.println("⚠️ Base64 veri çok büyük (" + base64Data.length() + " karakter), placeholder kullanılıyor");
                return generateSimplePlaceholder(prompt);
            }
                            
                            System.out.println("✅ Gemini ile resim başarıyla üretildi!");
                            return dataUrl;
                        }
                    }
                }
            }

            // Fallback: basit placeholder
            System.out.println("⚠️ Gemini resim üretemedi, basit placeholder kullanılıyor");
            System.out.println("🔍 Response tam içeriği: " + response.getBody());
            return generateSimplePlaceholder(prompt);
            
        } catch (Exception e) {
            System.err.println("❌ Gemini API hatası: " + e.getMessage());
            e.printStackTrace();
            return generateSimplePlaceholder(prompt);
        }
    }
    
    /**
     * Prompt'u optimize et - çok büyükse kısalt
     */
    private String optimizePrompt(String prompt) {
        // Maksimum 1000 karakter sınırı
        final int MAX_PROMPT_LENGTH = 2000;
        
        if (prompt.length() <= MAX_PROMPT_LENGTH) {
            return prompt;
        }
        
        System.out.println("⚠️ Prompt çok büyük (" + prompt.length() + " karakter), " + MAX_PROMPT_LENGTH + " karaktere kısaltılıyor");
        
        // İlk 800 karakteri al ve sonuna özet ekle
        String truncated = prompt.substring(0, 800);
        String summary = "... (prompt kısaltıldı, " + (prompt.length() - 800) + " karakter çıkarıldı)";
        
        return truncated + summary;
    }
    
    
    /**
     * Alternatif resim üretim metodu - daha kısa prompt ile
     */
    public String generateImageWithShortPrompt(String prompt) {
        try {
            // Prompt'u daha da kısalt
            String shortPrompt = prompt.length() > 200 ? prompt.substring(0, 200) + "..." : prompt;
            System.out.println("🎨 Kısa prompt ile resim üretiliyor: " + shortPrompt);
            
            return generateImage(shortPrompt);
        } catch (Exception e) {
            System.err.println("❌ Kısa prompt ile resim üretimi başarısız: " + e.getMessage());
            return generateSimplePlaceholder(prompt);
        }
    }
    
    /**
     * Çok büyük promptlar için özel işlem
     */
    public String generateImageForLargePrompt(String prompt) {
        try {
            // Önce prompt'u analiz et
            if (prompt.length() > 50000) {
                System.out.println("🚨 Çok büyük prompt tespit edildi (" + prompt.length() + " karakter)");
                
                // En önemli kısmı çıkar (ilk 1000 karakter)
                String essentialPrompt = prompt.substring(0, Math.min(1000, prompt.length()));
                System.out.println("📝 Sadece temel kısım kullanılıyor: " + essentialPrompt.substring(0, 100) + "...");
                
                return generateImage(essentialPrompt);
            }
            
            return generateImage(prompt);
        } catch (Exception e) {
            System.err.println("❌ Büyük prompt işleme hatası: " + e.getMessage());
            return generateSimplePlaceholder(prompt);
        }
    }
    
    /**
     * Test metodu - Gemini API'nin çalışıp çalışmadığını kontrol et
     */
    public String testGeminiConnection() {
        try {
            System.out.println("🧪 Gemini API bağlantı testi başlatılıyor...");
            String testPrompt = "A simple red circle";
            return generateImage(testPrompt);
        } catch (Exception e) {
            System.err.println("❌ Gemini API test hatası: " + e.getMessage());
            e.printStackTrace();
            return "TEST_FAILED";
        }
    }
    
    private String generateSimplePlaceholder(String prompt) {
        // Sadece basit bir placeholder URL
        return "https://via.placeholder.com/400x300/4A90E2/FFFFFF?text=Image+Loading";
    }
}