import hashlib
import hmac
import base64
import json
import requests
from email.utils import formatdate


def create_signature(client_secret, client_id, http_verb, content_type, hashed_body, path_and_query, time_stamp):
        message_to_sign = f"{client_id}\n{http_verb}\n{content_type}\n{hashed_body}\n{path_and_query}\n{time_stamp}"
        hmac_sha256 = hmac.new(client_secret.encode('utf-8'), message_to_sign.encode('utf-8'), hashlib.sha256)
        return base64.b64encode(hmac_sha256.digest()).decode('utf-8')

def hash_request_body(body):
        sha256_hasher = hashlib.sha256()
        sha256_hasher.update(body.encode('utf-8'))
        hash_bytes = sha256_hasher.digest()
        return base64.b64encode(hash_bytes).decode('utf-8') 

def get(client_id, client_secret, base_url):
        path_and_query = "/v1.1/brand"
        url = base_url + path_and_query
        http_verb = "GET"
        content_type = ""  # Use empty string for GET requests as they do not have a content type.
        http_body = ""  # Use empty string for GET requests as they do not have a body.
        hashed_body = hash_request_body(http_body)
        time_stamp = formatdate(timeval=None, localtime=False, usegmt=True)

        signed_message = create_signature(client_secret, client_id, http_verb, content_type, hashed_body, path_and_query, time_stamp)

        headers = {
            "Authorization": f"OloSignature {client_id}:{signed_message}",
            "Date": time_stamp
        }

        response = requests.get(url, headers=headers)
        print(response.text)

def post(client_id, client_secret, base_url):
        path_and_query = "/v1.1/users/exists"
        url = base_url + path_and_query
        http_verb = "POST"
        content_type = "application/json"
        http_body = json.dumps({"email": "noreply@olo.com"})
        hashed_body = hash_request_body(http_body)
        time_stamp = formatdate(timeval=None, localtime=False, usegmt=True)

        signed_message = create_signature(client_secret, client_id, http_verb, content_type, hashed_body, path_and_query, time_stamp)
        
        headers = {
            "Authorization": f"OloSignature {client_id}:{signed_message}",
            "Date": time_stamp,
            "Content-Type": content_type
        }

        response = requests.post(url, headers=headers, data=http_body)
        print(response.text)

client_id = "ENTER YOUR OLO SANDBOX CLIENT ID"
client_secret = "ENTER YOUR OLO SANDBOX CLIENT SECRET"
base_url = "https://ordering.api.olosandbox.com"

print("********************* START GET Request *********************")
get(client_id, client_secret, base_url)
print("********************* END GET Request *********************\n")

print("********************* START POST Request *********************")
post(client_id, client_secret, base_url)
print("********************* END POST Request *********************\n")