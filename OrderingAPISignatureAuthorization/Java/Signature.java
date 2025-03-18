import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.time.format.DateTimeFormatter;
import java.time.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;

public class Signature {
	private static String SignMessage(String sharedSecret, String clientId, String pathAndQuery, String requestMethod,String contentType, String hashedBody, String timeStamp) throws Exception {
		String messageToSign = String.join("\n", new String[] { clientId, requestMethod, contentType, hashedBody, pathAndQuery, timeStamp });
		System.out.println(String.format("MESSAGE TO SIGN: %n%1s%n", messageToSign));

		Mac sha256_HMAC;
		sha256_HMAC = Mac.getInstance("HmacSHA256");
		SecretKeySpec secret_key = new SecretKeySpec(sharedSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
		sha256_HMAC.init(secret_key);
		return Base64.getEncoder().encodeToString(sha256_HMAC.doFinal(messageToSign.getBytes(StandardCharsets.UTF_8)));
	}

	private static String HashRequestBody(String body) throws Exception {
		MessageDigest digest = MessageDigest.getInstance("SHA-256");
		return Base64.getEncoder().encodeToString(digest.digest(body.getBytes()));
	}

	private static void SendRequest(HttpRequest request) throws Exception {
		HttpResponse<String> response = HttpClient.newBuilder()
			.build()
			.send(request, HttpResponse.BodyHandlers.ofString());

		System.out.println(String.format("RESPONSE CODE: %1s", response.statusCode()));
		System.out.println(String.format("RESPONSE BODY: %n%1s", response.body()));
	}

	private static void Get(String clientId, String clientSecret, String baseUrl) throws Exception {
		String pathAndQuery = "/v1.1/brand";
		String url = baseUrl + pathAndQuery;
		String requestMethod = "GET";
		String contentType = ""; // Use empty string for GET requests as they do not have a content type.
		String body = ""; // Use empty string for GET requests as they do not have a body.
		String hashedBody = HashRequestBody(body); // Must still hash the empty body
		String timeStamp = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss O").format(ZonedDateTime.now(ZoneOffset.UTC));
		String signedMessage = SignMessage(clientSecret, clientId, pathAndQuery, requestMethod, contentType, hashedBody, timeStamp);

		HttpRequest request = HttpRequest.newBuilder()
			.uri(new URI(url))
			.header("Authorization", String.format("OloSignature %1s:%2s", clientId, signedMessage))
			.header("Date", timeStamp)
			.GET()
			.build();

		SendRequest(request);
	}

	private static void Post(String clientId, String clientSecret, String baseUrl) throws Exception {
		String pathAndQuery = "/v1.1/users/exists";
		String url = baseUrl + pathAndQuery;
		String requestMethod = "POST";
		String contentType = "application/json"; // All Olo API requests with a body should have a content type of application/json
		String body = "{\"email\":\"noreply@olo.com\"}";
		String hashedBody = HashRequestBody(body);
		String timeStamp = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss O").format(ZonedDateTime.now(ZoneOffset.UTC));
		String signedMessage = SignMessage(clientSecret, clientId, pathAndQuery, requestMethod, contentType, hashedBody, timeStamp);

		HttpRequest request = HttpRequest.newBuilder()
			.uri(new URI(url))
			.header("Authorization", String.format("OloSignature %1s:%2s", clientId, signedMessage))
			.header("Date", timeStamp)
			.header("Content-Type", contentType)
			.POST(HttpRequest.BodyPublishers.ofString(body))
			.build();

		SendRequest(request);
	}

	public static void main(String[] args) throws Exception {
		String clientId = "ENTER YOUR OLO SANDBOX CLIENT ID";
		String clientSecret = "ENTER YOUR OLO SANDBOX CLIENT SECRET";
		String baseUrl = "https://ordering.api.olosandbox.com";

		System.out.println("********************* START GET Request *********************");
		Get(clientId, clientSecret, baseUrl);
		System.out.println("********************* END GET Request *********************\n");

		System.out.println("********************* START POST Request *********************");
		Post(clientId, clientSecret, baseUrl);
		System.out.println("********************* END POST Request *********************\n");
	}
}