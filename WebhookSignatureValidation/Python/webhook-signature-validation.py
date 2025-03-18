import hmac
import hashlib
import base64

# Can be used as reference to verify your implementation of webhook signature validation.
# Replace these values with the ones you received from Olo
payload = '{"message":"Olo webhook test!"}'  # Ensure the body is the raw unformatted value.
olo_message_id = "f8dac5dd-d3b2-w76c-b969-a668c699637c"  # Value of the X-Olo-Message-Id header
olo_timestamp = "635616089149791951"  # Value of the X-Olo-Timestamp header; represents number of ticks since 1/1/0001 12:00am UTC
webhook_url = "https://www.olo.com/webhook/"
shared_secret = "ThisIsADemonstrationSecret"

data_to_sign = f"{webhook_url}\n{payload}\n{olo_message_id}\n{olo_timestamp}"

def generate_signature(shared_secret, data_to_sign):
    hmac_obj = hmac.new(shared_secret.encode('utf-8'), data_to_sign.encode('utf-8'), hashlib.sha256)
    return base64.b64encode(hmac_obj.digest()).decode('utf-8')

signed_message = generate_signature(shared_secret, data_to_sign)

print("Generated Signature:", signed_message)