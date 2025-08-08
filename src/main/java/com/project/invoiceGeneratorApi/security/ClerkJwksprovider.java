package com.project.invoiceGeneratorApi.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

@Component
public class ClerkJwksprovider {

    // used for injecting values from application.properties. It's a form of dependency injection.
    @Value("${clerk.jwks-url}")
    private String jwksUrl;

    //This map serves as a cache to verify a token
    private final Map<String, PublicKey> keyCache = new HashMap<>();

    //records the last time the application successfully fetched the JWKS from the external URL.
    private long lastFetchTime = 0;

    //defines the Time-To-Live (TTL) for the cache.
    //It sets a rule that the keys in the cache are considered valid for up to one hour.
    private static final long CACHE_TTL = 3600000;     // 1 hour in milliseconds

    //This is for thread safety.
    //multiple threads might try to access and modify the keyCache and lastFetchTime fields at the same time.
    private final ReentrantLock lock = new ReentrantLock();

    public PublicKey getPublicKey(String kid) throws Exception {

        lock.lock();

        try {
            // Check cache validity
            // Checks if the public key for the requested kid already exists in the local cache.
            if (keyCache.containsKey(kid) && System.currentTimeMillis() - lastFetchTime < CACHE_TTL) {
                return keyCache.get(kid);
            }

            // Refresh keys if the cache is expired or the key is missing
            //It calls the refreshKeys() method, which performs a new network request to the JWKS URL,
            // downloads the latest key set, and updates the keyCache.
            refreshKeys();

            // After refresh, check if the key is now available
            if(keyCache.containsKey(kid)) {
                return keyCache.get(kid);
            } else {
                throw new KeyNotFoundException("Public key with kid: " + kid + " not found after refreshing JWKS.");
            }
        } finally {
            lock.unlock(); // Always release the lock in a finally block
        }

    }

    //purpose is to safely and reliably fetch the latest set of public keys from a remote URL,
    // parse them, and prepare them to be used for JWT verification.
    private void refreshKeys() throws  Exception {

        // Use a rest template for better HTTP handling
        //RestTemplate is a powerful tool in Spring for making synchronous HTTP requests.
        // It's used here to perform the GET request to fetch the JWKS.
        RestTemplate restTemplate = new RestTemplate();
        JsonNode jwks;

        try {
            //This method sends an HTTP GET request.
            //jwksUrl: This is the URL to which the request is sent
            //JsonNode.class: This tells RestTemplate to automatically convert the JSON response body
            // into a JsonNode object (from the Jackson library).
            jwks = restTemplate.getForObject(jwksUrl, JsonNode.class);
        } catch (Exception e) {
            throw new FetchingKeysException("Failed to fetch JWKS from URL: " + jwksUrl, e);
        }

        if (jwks == null || !jwks.has("keys")) {
            throw new InvalidJwksException("JWKS response is invalid or missing 'keys' array.");
        }

        JsonNode keys = jwks.get("keys");

        //A temporary map is created.
        // All the new keys are added to this temporary map first.  This ensures that the main keyCache is not in an inconsistent state while new keys are being added.
        // Once the loop is complete, the old cache is replaced with this new one in a single, atomic operation
        Map<String, PublicKey> newKeyCache = new HashMap<>();

        for (JsonNode keyNode : keys) {
            if (keyNode.has("kid") && keyNode.has("kty") && keyNode.has("alg") && keyNode.has("n") && keyNode.has("e")) {
                String kid = keyNode.get("kid").asText();
                String kty = keyNode.get("kty").asText();
                String alg = keyNode.get("alg").asText();

                // Only process RSA keys with RS256 algorithm
                //Clerk uses RS256 for its tokens. This code explicitly checks that the key is an RSA key
                // and that its algorithm is RS256
                if ("RSA".equals(kty) && "RS256".equals(alg)) {
                    String n = keyNode.get("n").asText();
                    String e = keyNode.get("e").asText();

                    // Correctly use URL-safe decoding for both n and e
                    PublicKey publicKey = createPublicKey(n, e);
                    newKeyCache.put(kid, publicKey);
                }
            }
        }

        // Atomically replace the cache to avoid race conditions
        keyCache.clear();
        keyCache.putAll(newKeyCache);
        lastFetchTime = System.currentTimeMillis();
    }

    private PublicKey createPublicKey(String modulus, String exponent) throws Exception{

        //The modulus (n) and exponent (e) values in a JWK are not plain text;
        //they are Base64 URL-encoded. Before they can be used to create a public key, they
        // must be decoded back into their raw byte representation.
        byte[] modulusBytes = Base64.getUrlDecoder().decode(modulus);
        byte[] exponentBytes = Base64.getUrlDecoder().decode(exponent); // Corrected: use getUrlDecoder()

        //BigInteger constructor that takes a signum (1 for positive) and a byte array is used here.
        BigInteger modulusBigInt = new BigInteger(1, modulusBytes);
        BigInteger exponentBigInt = new BigInteger(1, exponentBytes);

        RSAPublicKeySpec spec = new RSAPublicKeySpec(modulusBigInt, exponentBigInt);
        KeyFactory factory = KeyFactory.getInstance("RSA");

        return factory.generatePublic(spec);
    }

    // Custom exceptions for clarity
    public static class KeyNotFoundException extends RuntimeException {
        public KeyNotFoundException(String message) {
            super(message);
        }
    }

    public static class FetchingKeysException extends RuntimeException {
        public FetchingKeysException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class InvalidJwksException extends RuntimeException {
        public InvalidJwksException(String message) {
            super(message);
        }
    }

}
