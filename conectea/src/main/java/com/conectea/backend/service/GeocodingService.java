package com.conectea.backend.service;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class GeocodingService {

    private static final Pattern CEP_PATTERN = Pattern.compile("\\d{5}-?\\d{3}");

    public double[] getCoordinates(String endereco) {
        // Tentativa 1: Endereço completo
        double[] coords = fetchCoordinates(endereco);
        
        // Tentativa 2: Apenas CEP (se existir no endereço)
        if (coords == null) {
            String cep = extractCEP(endereco);
            if (cep != null) {
                coords = fetchCoordinates(cep);
            }
        }
        
        // Tentativa 3: Cidade e estado
        if (coords == null) {
            String cidadeEstado = extractCidadeEstado(endereco);
            if (cidadeEstado != null) {
                coords = fetchCoordinates(cidadeEstado);
            }
        }
        
        return coords;
    }

    private double[] fetchCoordinates(String endereco) {
        try {
            String enderecoNormalizado = normalizeAddress(endereco);
            String url = buildGeocodingUrl(enderecoNormalizado);
            
            SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
            factory.setConnectTimeout(5000);
            factory.setReadTimeout(5000);
            
            RestTemplate restTemplate = new RestTemplate(factory);
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "ConectEA/1.0 (contato@conectea.com)");
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                url, 
                HttpMethod.GET, 
                entity, 
                new ParameterizedTypeReference<List<Map<String, Object>>>() {}
            );
            
            List<Map<String, Object>> results = response.getBody();

            if (results != null && !results.isEmpty()) {
                Map<String, Object> place = results.get(0);
                double lat = Double.parseDouble(place.get("lat").toString());
                double lon = Double.parseDouble(place.get("lon").toString());
                return new double[]{lat, lon};
            }
        } catch (Exception e) {
            // Falha silenciosa
        }
        return null;
    }

    private String normalizeAddress(String endereco) {
        return endereco
            .replace("º", "")
            .replace("nº", "numero")
            .replace("°", "")
            .replace("ª", "")
            .replace(",", " ")
            .replace(";", " ")
            .replace("  ", " ")
            .trim();
    }

    private String buildGeocodingUrl(String endereco) {
        return UriComponentsBuilder.fromUriString("https://nominatim.openstreetmap.org/search")
            .queryParam("q", endereco)
            .queryParam("format", "json")
            .queryParam("limit", 1)
            .build()
            .toUriString();
    }

    private String extractCEP(String endereco) {
        Matcher matcher = CEP_PATTERN.matcher(endereco);
        return matcher.find() ? matcher.group() : null;
    }

    private String extractCidadeEstado(String endereco) {
        // Extrai os últimos 2 componentes (cidade e estado)
        String[] partes = endereco.split(",");
        if (partes.length >= 2) {
            return partes[partes.length - 2].trim() + ", " + partes[partes.length - 1].trim();
        }
        return null;
    }
}