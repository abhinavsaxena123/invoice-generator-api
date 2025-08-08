package com.project.invoiceGeneratorApi.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.security.PublicKey;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ClerkJwtAuthFilter extends OncePerRequestFilter {

    //Injects the Clerk Issuer URL from your application properties.
    @Value("${clerk.issuer}")
    private String clerkIssuer;

    //Injects your custom JWKS provider bean.
    private final ClerkJwksprovider jwksprovider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        //Exclude webhooks from JWT authentication
        if(request.getRequestURI().contains("/api/webhooks")){
            filterChain.doFilter(request, response);
            return;
        }

        //Retrieves the value of the Authorization header.
        String authHeader = request.getHeader("Authorization");

        //If no header is present, the filter should not block the request.
        if(authHeader == null || !authHeader.startsWith("Bearer")){
            filterChain.doFilter(request, response);
            return;
        }

        try {
            //Correctly extracts the token string by removing "Bearer " (7 characters) from the header.
            String token = authHeader.substring(7);

            // Splits the JWT into its three parts (header, payload, signature).
            String[] chunks = token.split("\\.");

            //Decodes the Base64 URL-encoded header.
            String headerJson = new String(Base64.getUrlDecoder().decode(chunks[0]));
            ObjectMapper mapper = new ObjectMapper();
            JsonNode headerNode = mapper.readTree(headerJson);

            String kid = headerNode.get("kid").asText();

            PublicKey publicKey = jwksprovider.getPublicKey(kid);

            Jws<Claims> claimsJws = Jwts.parserBuilder()
                    .setSigningKey(publicKey)
                    .setAllowedClockSkewSeconds(60)
                    .requireIssuer(clerkIssuer)
                    .build()
                    .parseClaimsJws(token);

            //Gets the payload (claims) of the JWT.
            Claims claims = claimsJws.getBody();

            //Extracts the user's ID from the sub (subject) claim.
            String clerkUserId = claims.getSubject();

            List<SimpleGrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));

            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(clerkUserId, null, authorities);
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);

            filterChain.doFilter(request, response);  // new line added

        } catch (ExpiredJwtException e) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "JWT Token has expired.");
            return; // Terminate the filter chain
        } catch (SignatureException e) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid JWT Token Signature.");
            return; // Terminate the filter chain
        } catch (Exception e) {
            // General catch-all for other parsing errors, key not found, etc.
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid JWT Token.");
            return; // Terminate the filter chain
        }

    }
}
