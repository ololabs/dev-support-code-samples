<?php

class Signature {

    private function createSignature($clientSecret, $clientId, $httpVerb, $contentType, $httpBody, $pathAndQuery, $timeStamp) 
    {
        $hashedBody = base64_encode(hash('sha256', $httpBody, true)); // The body must by hashed, even if it is empty
        $messageToSign = implode("\n", [
            $clientId,
            $httpVerb,
            $contentType,
            $hashedBody,
            $pathAndQuery,
            $timeStamp
            ]);
        return base64_encode(hash_hmac('sha256', $messageToSign, $clientSecret, true));
    }

    public function get($clientId, $clientSecret, $baseUrl) 
    {
        $pathAndQuery = "/v1.1/brand";
        $url = $baseUrl . $pathAndQuery;
        $httpVerb = "GET";
        $contentType = ""; // Use empty string for GET requests as they do not have a content type.
        $httpBody = ""; // Use empty string for GET requests as they do not have a body.
        $timeStamp = gmdate("D, d M Y H:i:s T");

        $signedMessage = $this->createSignature($clientSecret, $clientId, $httpVerb, $contentType, $httpBody, $pathAndQuery, $timeStamp);

        $ch = curl_init($url);
        curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
        curl_setopt($ch, CURLOPT_HTTPHEADER, [
            "Authorization: OloSignature {$clientId}:{$signedMessage}",
            "Date: {$timeStamp}",
        ]);
        $response = curl_exec($ch);
        curl_close($ch);

        echo $response . "\n";
    }

    public function post($clientId, $clientSecret, $baseUrl) 
    {
        $pathAndQuery = "/v1.1/users/exists";
        $url = $baseUrl . $pathAndQuery;
        $httpVerb = "POST";
        $contentType = "application/json"; // All Olo API requests with a body should have a content type of application/json
        $httpBody = json_encode(["email" => "noreply@olo.com"]);
        $timeStamp = gmdate("D, d M Y H:i:s T");

        $signedMessage = $this->createSignature($clientSecret, $clientId, $httpVerb, $contentType, $httpBody, $pathAndQuery, $timeStamp);

        $ch = curl_init($url);
        curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
        curl_setopt($ch, CURLOPT_HTTPHEADER, [
            "Authorization: OloSignature {$clientId}:{$signedMessage}",
            "Date: {$timeStamp}",
            "Content-Type: {$contentType}",
        ]);
        curl_setopt($ch, CURLOPT_POST, true);
        curl_setopt($ch, CURLOPT_POSTFIELDS, $httpBody);
        $response = curl_exec($ch);
        curl_close($ch);
        
        echo $response . "\n";
    }
}

// Main code to run the requests
$clientId = "ENTER YOUR OLO SANDBOX CLIENT ID";
$clientSecret = "ENTER YOUR OLO SANDBOX CLIENT SECRET";
$baseUrl = "https://ordering.api.olosandbox.com";
$api = new Signature();

echo "********************* START GET Request *********************\n";
$api->get($clientId, $clientSecret, $baseUrl);
echo "********************* END GET Request *********************\n\n";

echo "********************* START POST Request *********************\n";
$api->post($clientId, $clientSecret, $baseUrl);
echo "********************* END POST Request *********************\n\n";

?>
