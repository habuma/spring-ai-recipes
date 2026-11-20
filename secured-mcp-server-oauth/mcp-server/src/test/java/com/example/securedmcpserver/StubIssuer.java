package com.example.securedmcpserver;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPublicKey;
import java.util.Arrays;
import java.util.Base64;

/**
 * Minimal OIDC discovery endpoint so the resource server can start without
 * the real authorization server running on port 9999.
 */
final class StubIssuer {

  private static final HttpServer SERVER = start();

  static String issuerUri() {
    return "http://localhost:" + SERVER.getAddress().getPort();
  }

  private static String jwks() {
    try {
      var generator = KeyPairGenerator.getInstance("RSA");
      generator.initialize(2048);
      var key = (RSAPublicKey) generator.generateKeyPair().getPublic();
      var encoder = Base64.getUrlEncoder().withoutPadding();
      return """
          {"keys":[{"kty":"RSA","use":"sig","alg":"RS256","kid":"stub",
          "n":"%s","e":"%s"}]}
          """.formatted(
          encoder.encodeToString(unsigned(key.getModulus())),
          encoder.encodeToString(unsigned(key.getPublicExponent())));
    } catch (GeneralSecurityException e) {
      throw new IllegalStateException(e);
    }
  }

  private static byte[] unsigned(BigInteger value) {
    var bytes = value.toByteArray();
    return bytes[0] == 0 ? Arrays.copyOfRange(bytes, 1, bytes.length) : bytes;
  }

  private static HttpServer start() {
    try {
      var server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
      var issuer = "http://localhost:" + server.getAddress().getPort();
      var discovery = """
          {"issuer":"%1$s","jwks_uri":"%1$s/jwks",
           "authorization_endpoint":"%1$s/authorize","token_endpoint":"%1$s/token",
           "response_types_supported":["code"],"subject_types_supported":["public"],
           "id_token_signing_alg_values_supported":["RS256"]}
          """.formatted(issuer).getBytes(StandardCharsets.UTF_8);
      server.createContext("/.well-known", exchange -> {
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, discovery.length);
        exchange.getResponseBody().write(discovery);
        exchange.close();
      });
      server.createContext("/jwks", exchange -> {
        var body = jwks().getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, body.length);
        exchange.getResponseBody().write(body);
        exchange.close();
      });
      server.start();
      return server;
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }

}
