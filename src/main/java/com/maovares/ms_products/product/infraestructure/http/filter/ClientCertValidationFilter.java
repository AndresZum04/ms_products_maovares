package com.maovares.ms_products.product.infraestructure.http.filter;

import java.io.ByteArrayInputStream;
import java.security.MessageDigest;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class ClientCertValidationFilter implements Filter {

    private static final String EXPECTED_THUMBPRINT = System.getenv("CLIENT_CERT_THUMBPRINT");

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) {

        HttpServletRequest httpReq = (HttpServletRequest) request;
        HttpServletResponse httpRes = (HttpServletResponse) response;

        try {
            String certHeader = httpReq.getHeader("X-ARR-ClientCert");

            if (certHeader == null || certHeader.isEmpty()) {
                sendError(httpRes, HttpServletResponse.SC_FORBIDDEN, "Missing client certificate");
                return;
            }

            byte[] decoded = Base64.getDecoder().decode(certHeader);

            X509Certificate cert = (X509Certificate) CertificateFactory
                    .getInstance("X.509")
                    .generateCertificate(new ByteArrayInputStream(decoded));

            MessageDigest md = MessageDigest.getInstance("SHA-1");
            String thumbprint = bytesToHex(md.digest(cert.getEncoded()));

            if (EXPECTED_THUMBPRINT == null ||
                    !thumbprint.equalsIgnoreCase(EXPECTED_THUMBPRINT)) {
                sendError(httpRes, HttpServletResponse.SC_FORBIDDEN, "Invalid certificate");
                return;
            }
            chain.doFilter(request, response);

        } catch (Exception e) {
            sendError(httpRes, HttpServletResponse.SC_FORBIDDEN, "Certificate validation error");
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }

    private void sendError(HttpServletResponse response, int status, String message) {
        try {
            response.setStatus(status);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"" + message + "\"}");
            response.getWriter().flush();
        } catch (Exception e) {

        }
    }
}
