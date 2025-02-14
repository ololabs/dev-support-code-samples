<?php

// Can be used as reference to verify your implementation of webhook signature validation.
// Replace the values below with the values you received from Olo in order to generate a signature for the webhook.
$payload = '{"message":"Olo webhook test!"}'; // Ensure the body is the raw unformatted value. Any conversion would change the resulting signature.
$oloMesssageId = "f8dac5dd-d3b2-w76c-b969-a668c699637c"; // Value of the X-Olo-Message-Id header
$oloTimestamp = "635616089149791951"; // Value of the X-Olo-Timestamp header; represents number of ticks since 1/1/0001 12:00am UTC
$webhookUrl = "https://www.olo.com/webhook/";
$sharedSecret = "ThisIsADemonstrationSecret";

$encodedSharedSecret = mb_convert_encoding($sharedSecret, 'UTF-8');
$encodedToSign = mb_convert_encoding("$webhookUrl\n$payload\n$oloMesssageId\n$oloTimestamp", 'UTF-8');
$signedMessage = base64_encode(hash_hmac('sha256', $encodedToSign, $encodedSharedSecret, true));

echo "Generated Signature: " . $signedMessage;
?>
