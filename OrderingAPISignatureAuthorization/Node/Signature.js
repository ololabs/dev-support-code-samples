const crypto = require('crypto');
const axios = require('axios');

function hashRequestBody(body) {
  const hash = crypto.createHash('sha256');
  hash.update(body, 'utf8');
  return hash.digest('base64');
}

function createSignature(secret, clientId, httpVerb, contentType, hashedBody, pathAndQuery, timeStamp) {
  const messageToSign = `${clientId}\n${httpVerb}\n${contentType}\n${hashedBody}\n${pathAndQuery}\n${timeStamp}`;
  const hmac = crypto.createHmac('sha256', secret);
  hmac.update(messageToSign, 'utf8');
  return hmac.digest('base64');
}

async function get(clientId, clientSecret, baseUrl) {
  const pathAndQuery = "/v1.1/brand";
  const url = baseUrl + pathAndQuery;
  const httpVerb = "GET";
  const contentType = '';  // Empty string for GET requests
  const httpBody = '';  // Empty body for GET requests
  const hashedBody = hashRequestBody(httpBody);  // Hash the empty body
  const timeStamp = new Date().toUTCString();

  const signedMessage = createSignature(clientSecret, clientId, httpVerb, contentType, hashedBody, pathAndQuery, timeStamp);

  try {
    const response = await axios.get(url, {
      headers: {
        'Authorization': `OloSignature ${clientId}:${signedMessage}`,
        'Date': timeStamp
      }
    });
    console.log('RESPONSE CODE:', response.status);
    console.log('RESPONSE BODY:', response.data);
  } catch (error) {
    console.error('Error with GET request:', error);
  }
}

async function post(clientId, clientSecret, baseUrl) {
  const pathAndQuery = "/v1.1/users/exists";
  const url = baseUrl + pathAndQuery;
  const httpVerb = "POST";
  const contentType = "application/json";
  const httpBody = '{"email":"noreply@olo.com"}';
  const hashedBody = hashRequestBody(httpBody);
  const timeStamp = new Date().toUTCString();

  const signedMessage = createSignature(clientSecret, clientId, httpVerb, contentType, hashedBody, pathAndQuery, timeStamp);

  try {
    const response = await axios.post(url, httpBody, {
      headers: {
        'Authorization': `OloSignature ${clientId}:${signedMessage}`,
        'Date': timeStamp,
        'Content-Type': contentType
      }
    });
    console.log('RESPONSE CODE:', response.status);
    console.log('RESPONSE BODY:', response.data);
  } catch (error) {
    console.error('Error with POST request:', error);
  }
}

(async function main() {
  const clientId = "ENTER YOUR OLO SANDBOX CLIENT ID";
  const clientSecret = "ENTER YOUR OLO SANDBOX CLIENT SECRET";
  const baseUrl = "https://ordering.api.olosandbox.com";

  console.log("********************* START GET Request *********************");
  await get(clientId, clientSecret, baseUrl);
  console.log("********************* END GET Request *********************\n");

  console.log("********************* START POST Request *********************");
  await post(clientId, clientSecret, baseUrl);
  console.log("********************* END POST Request *********************\n");
})();
