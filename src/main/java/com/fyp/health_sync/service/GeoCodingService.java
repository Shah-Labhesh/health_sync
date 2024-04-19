package com.fyp.health_sync.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class GeoCodingService {

    @Value("${google.maps.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate;

    public GeoCodingService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String getAddressFromCoordinates(double latitude, double longitude) throws JsonProcessingException {
        String apiUrl = String.format("https://maps.googleapis.com/maps/api/geocode/json?latlng=%s,%s&key=%s",
                latitude, longitude, apiKey);

        // Make an HTTP GET request to the API
        String jsonResponse = restTemplate.getForObject(apiUrl, String.class);
        System.out.println(jsonResponse);
        return parseAddressFromJson(jsonResponse);
    }

    private String parseAddressFromJson(String jsonResponse) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();

            JsonNode jsonNode = objectMapper.readTree(jsonResponse);
            JsonNode plusCode = jsonNode.get("plus_code");
        return plusCode.get("compound_code").asText();
    }
}
