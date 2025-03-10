const crypto = require('crypto');

// Can be used as reference to verify your implementation of webhook signature validation.
// Replace these values with the ones you received from Olo in order to generate a signature for the webhook.
const payload = '{"message":"Olo webhook test!"}'; // Ensure the body is the raw unformatted value.
const oloMessageId = "f8dac5dd-d3b2-w76c-b969-a668c699637c"; // Value of the X-Olo-Message-Id header
const oloTimestamp = "635616089149791951"; // Value of the X-Olo-Timestamp header; represents number of ticks since 1/1/0001 12:00:00am UTC
const webhookUrl = "https://www.olo.com/webhook/";
const sharedSecret = "ThisIsADemonstrationSecret";

const encodedSharedSecret = Buffer.from(sharedSecret, 'utf-8');
const encodedToSign = `${webhookUrl}\n${payload}\n${oloMessageId}\n${oloTimestamp}`;

const hmac = crypto.createHmac('sha256', encodedSharedSecret);
hmac.update(encodedToSign, 'utf-8');
const signedMessage = hmac.digest('base64');

console.log("Generated Signature: " + signedMessage);