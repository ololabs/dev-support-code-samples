using System.Security.Cryptography;
using System.Text;

class WebhookSignatureGenerator
{
    static void Main(string[] args)
    {
        // Can be used as reference to verify your implementation of webhook signature validation.
        // Replace these values with the ones you received from Olo
        string payload = "{\"message\":\"Olo webhook test!\"}"; // Ensure the body is the raw unformatted value.
        string oloMessageId = "f8dac5dd-d3b2-w76c-b969-a668c699637c"; // Value of the X-Olo-Message-Id header
        string oloTimestamp = "635616089149791951"; // Value of the X-Olo-Timestamp header; represents number of ticks since 1/1/0001 12:00am UTC
        string webhookUrl = "https://www.olo.com/webhook/";
        string sharedSecret = "ThisIsADemonstrationSecret";

        string dataToSign = $"{webhookUrl}\n{payload}\n{oloMessageId}\n{oloTimestamp}";

        string signedMessage = GenerateSignature(sharedSecret, dataToSign);

        Console.WriteLine("Generated Signature: " + signedMessage);
    }

    public static string GenerateSignature(string sharedSecret, string dataToSign)
    {
        byte[] secretKeyBytes = Encoding.UTF8.GetBytes(sharedSecret);

        using (HMACSHA256 hmac = new HMACSHA256(secretKeyBytes))
        {
            byte[] dataBytes = Encoding.UTF8.GetBytes(dataToSign);
            byte[] hmacBytes = hmac.ComputeHash(dataBytes);

            return Convert.ToBase64String(hmacBytes);
        }
    }
}