using System.Security.Cryptography;
using System.Text;
using System.Net.Http.Headers;
using System.Globalization;

public class Program
{
    private string HashRequestBody(string body)
    {
        using (var sha256Hasher = SHA256.Create())
        {
            var bytes = Encoding.UTF8.GetBytes(body);
            var hashBytes = sha256Hasher.ComputeHash(bytes);
            return Convert.ToBase64String(hashBytes);
        }
    }

    private string CreateSignature(string secret, string clientId, string httpVerb, string contentType, string hashedBody, string pathAndQuery, string timeStamp)
    {
        var encoding = new UTF8Encoding();
        var messageToSign = string.Join("\n", clientId, httpVerb, contentType, hashedBody, pathAndQuery, timeStamp);
        byte[] keyByte = encoding.GetBytes(secret);
        byte[] messageBytes = encoding.GetBytes(messageToSign);
        using (var hmacsha256 = new HMACSHA256(keyByte))
        {
            byte[] hashmessage = hmacsha256.ComputeHash(messageBytes);
            return Convert.ToBase64String(hashmessage);
        }
    }

    private async Task Get(string clientId, string clientSecret, string baseUrl)
    {
        var url = new Uri($@"{baseUrl}/v1.1/brand");
        var httpVerb = "GET";
        var contentType = ""; // Use empty string for GET requests as they do not have a content type.
        var httpBody = ""; // Use empty string for GET requests as they do not have a body.
        var hashedBody = HashRequestBody(httpBody); // Must still hash the empty body
        var pathAndQuery = url.PathAndQuery;
        var timeStamp = DateTime.UtcNow.ToString(DateTimeFormatInfo.InvariantInfo.RFC1123Pattern);

        var signedMessage = CreateSignature(clientSecret, clientId, httpVerb, contentType, hashedBody, pathAndQuery, timeStamp);

        using (var client = new HttpClient())
        {
            var request = new HttpRequestMessage(HttpMethod.Get, url);
            request.Headers.Authorization = new AuthenticationHeaderValue("OloSignature", $"{clientId}:{signedMessage}");
            request.Headers.Add("Date", timeStamp);

            var response = await client.SendAsync(request);
            Console.WriteLine($"RESPONSE CODE: {response.StatusCode}");
            var responseBody = await response.Content.ReadAsStringAsync();
            Console.WriteLine($"RESPONSE BODY: \n{responseBody}");
        }
    }

    private async Task Post(string clientId, string clientSecret, string baseUrl)
    {
        var url = new Uri($@"{baseUrl}/v1.1/users/exists");
        var httpVerb = "POST";
        var contentType = "application/json"; // All Olo API requests with a body should have a content type of application/json
        var httpBody = "{\"email\":\"noreply@olo.com\"}";
        var hashedBody = HashRequestBody(httpBody);
        var pathAndQuery = url.PathAndQuery;
        var timeStamp = DateTime.UtcNow.ToString(DateTimeFormatInfo.InvariantInfo.RFC1123Pattern);

        var signedMessage = CreateSignature(clientSecret, clientId, httpVerb, contentType, hashedBody, pathAndQuery, timeStamp);
    
        using (var client = new HttpClient())
        {
            var request = new HttpRequestMessage(HttpMethod.Post, url);
            request.Headers.Authorization = new AuthenticationHeaderValue("OloSignature", $"{clientId}:{signedMessage}");
            request.Content = new StringContent(httpBody, new MediaTypeHeaderValue(contentType));
            request.Headers.Add("Date", timeStamp);

            var response = await client.SendAsync(request);
            Console.WriteLine($"RESPONSE CODE: {response.StatusCode}");
            var responseBody = await response.Content.ReadAsStringAsync();
            Console.WriteLine($"RESPONSE BODY: \n{responseBody}");
        }
    }

    private static async Task Main(string[] args)
    {
	var clientId = "ENTER YOUR OLO SANDBOX CLIENT ID";
	var clientSecret = "ENTER YOUR OLO SANDBOX CLIENT SECRET";
	var baseUrl = "https://ordering.api.olosandbox.com";
        var program = new Program();

        Console.WriteLine("********************* START GET Request *********************");
        await program.Get(clientId, clientSecret, baseUrl);
        Console.WriteLine("********************* END GET Request *********************\n");

        Console.WriteLine("********************* START POST Request *********************");
        await program.Post(clientId, clientSecret, baseUrl);
	Console.WriteLine("********************* END POST Request *********************\n");
    }
}