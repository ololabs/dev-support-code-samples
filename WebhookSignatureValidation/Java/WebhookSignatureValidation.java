import java.nio.charset.StandardCharsets;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class WebhookSignatureValidation {

    public static void main(String[] args) throws Exception {
        // Can be used as reference to verify your implementation of webhook signature validation.
        // Replace these values with the ones you received from Olo
        String payload = "{\"message\":\"Olo webhook test!\"}"; // Ensure the body is the raw unformatted value.
        String oloMessageId = "f8dac5dd-d3b2-w76c-b969-a668c699637c"; // Value of the X-Olo-Message-Id header
        String oloTimestamp = "635616089149791951"; // Value of the X-Olo-Timestamp header; represents number of ticks since 1/1/0001 12:00:00am UTC
        String webhookUrl = "https://www.olo.com/webhook/";
        String sharedSecret = "ThisIsADemonstrationSecret";

        String dataToSign = webhookUrl + "\n" + payload + "\n" + oloMessageId + "\n" + oloTimestamp;

        String signedMessage = generateSignature(sharedSecret, dataToSign);

        System.out.println("Generated Signature: " + signedMessage);
    }

    public static String generateSignature(String sharedSecret, String dataToSign) throws Exception {
        SecretKeySpec secretKeySpec = new SecretKeySpec(sharedSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");

        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(secretKeySpec);

        byte[] rawHmac = mac.doFinal(dataToSign.getBytes(StandardCharsets.UTF_8));

        return Base64.getEncoder().encodeToString(rawHmac);
    }
}