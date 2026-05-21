package com.mealcircle2.mealcircle2.service.serviceImpl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mealcircle2.mealcircle2.dto.RazorpayOrderResponse;
import com.mealcircle2.mealcircle2.dto.RazorpayPaymentVerifyRequest;
import com.mealcircle2.mealcircle2.model.Mess;
import com.mealcircle2.mealcircle2.service.MessService;
import com.mealcircle2.mealcircle2.service.RazorpayService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

@Service
public class RazorpayServiceImpl implements RazorpayService {

  private final MessService messService;
  private final ObjectMapper objectMapper;
  private final HttpClient httpClient;

  @Value("${razorpay.key-id:}")
  private String razorpayKeyId;

  @Value("${razorpay.key-secret:}")
  private String razorpayKeySecret;

  public RazorpayServiceImpl(MessService messService, ObjectMapper objectMapper) {
    this.messService = messService;
    this.objectMapper = objectMapper;
    this.httpClient = HttpClient.newHttpClient();
  }

  @Override
  public RazorpayOrderResponse createOrder(String messId, String customerId) {
    validateKeys();

    Mess mess = messService.getMessById(messId);
    int amountInPaise = toPaise(mess.getPricePerMonth());

    try {
      String receipt = buildReceipt(messId, customerId);
      String payload = objectMapper.createObjectNode()
          .put("amount", amountInPaise)
          .put("currency", "INR")
          .put("receipt", receipt)
          .put("payment_capture", 1)
          .toString();

      HttpRequest request = HttpRequest.newBuilder()
          .uri(URI.create("https://api.razorpay.com/v1/orders"))
          .header("Content-Type", "application/json")
          .header("Authorization", buildBasicAuthHeader())
          .POST(HttpRequest.BodyPublishers.ofString(payload))
          .build();

      HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

      if (response.statusCode() < 200 || response.statusCode() >= 300) {
        throw new RuntimeException("Unable to create Razorpay order: " + response.body());
      }

      JsonNode root = objectMapper.readTree(response.body());
      String orderId = root.path("id").asText("");
      if (orderId.isBlank()) {
        throw new RuntimeException("Invalid Razorpay order response");
      }

      return RazorpayOrderResponse.builder()
          .key(razorpayKeyId)
          .orderId(orderId)
          .amount(amountInPaise)
          .currency("INR")
          .messId(mess.getId())
          .messName(mess.getMessName())
          .build();
    } catch (Exception e) {
      throw new RuntimeException(e.getMessage() != null ? e.getMessage() : "Razorpay order creation failed", e);
    }
  }

  @Override
  public Mess verifyPaymentAndJoin(RazorpayPaymentVerifyRequest request, String customerId) {
    validateKeys();

    if (request == null
        || isBlank(request.getMessId())
        || isBlank(request.getRazorpayOrderId())
        || isBlank(request.getRazorpayPaymentId())
        || isBlank(request.getRazorpaySignature())) {
      throw new RuntimeException("Invalid payment payload");
    }

    String payload = request.getRazorpayOrderId() + "|" + request.getRazorpayPaymentId();
    String expectedSignature = hmacSha256Hex(payload, razorpayKeySecret);

    if (!expectedSignature.equals(request.getRazorpaySignature())) {
      throw new RuntimeException("Payment signature verification failed");
    }

    return messService.joinMess(request.getMessId(), customerId);
  }

  private void validateKeys() {
    if (isBlank(razorpayKeyId) || isBlank(razorpayKeySecret)) {
      throw new RuntimeException("Razorpay credentials are not configured");
    }
  }

  private String buildBasicAuthHeader() {
    String token = razorpayKeyId + ":" + razorpayKeySecret;
    return "Basic " + Base64.getEncoder().encodeToString(token.getBytes(StandardCharsets.UTF_8));
  }

  private int toPaise(double amountInRupees) {
    if (amountInRupees <= 0) {
      throw new RuntimeException("Invalid mess price configured");
    }
    return (int) Math.round(amountInRupees * 100);
  }

  private String hmacSha256Hex(String payload, String secret) {
    try {
      Mac sha256Hmac = Mac.getInstance("HmacSHA256");
      SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
      sha256Hmac.init(secretKey);
      byte[] bytes = sha256Hmac.doFinal(payload.getBytes(StandardCharsets.UTF_8));

      StringBuilder sb = new StringBuilder(bytes.length * 2);
      for (byte b : bytes) {
        sb.append(String.format("%02x", b));
      }
      return sb.toString();
    } catch (Exception e) {
      throw new RuntimeException("Unable to verify payment signature", e);
    }
  }

  private String buildReceipt(String messId, String customerId) {
    long timestamp = Instant.now().toEpochMilli();
    int identityHash = Math.abs((messId + ":" + customerId).hashCode());
    String receipt = "mc_" + timestamp + "_" + identityHash;

    return receipt.length() > 40 ? receipt.substring(0, 40) : receipt;
  }

  private boolean isBlank(String value) {
    return value == null || value.isBlank();
  }
}
