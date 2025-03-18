require 'net/http'
require 'uri'
require 'openssl'
require 'base64'
require 'json'
require 'time'

class Program
  def hash_request_body(body)
    sha256 = OpenSSL::Digest::SHA256.new
    hash = sha256.digest(body.encode('utf-8'))
    Base64.encode64(hash).strip
  end

  def create_signature(secret, client_id, http_verb, content_type, hashed_body, path_and_query, time_stamp)
    message_to_sign = [client_id, http_verb, content_type, hashed_body, path_and_query, time_stamp].join("\n")
    key = secret.encode('utf-8')
    message = message_to_sign.encode('utf-8')

    hmac = OpenSSL::HMAC.new(key, OpenSSL::Digest::SHA256.new)
    hmac.update(message)
    Base64.encode64(hmac.digest).strip
  end

  def get(client_id, client_secret, base_url)
    path_and_query = "/v1.1/brand"
    url = URI(base_url + path_and_query)
    http_verb = "GET"
    content_type = "" # Use empty string for GET requests as they do not have a content type.
    http_body = "" # Use empty string for GET requests as they do not have a body.
    hashed_body = hash_request_body(http_body)
    time_stamp = Time.now.utc.strftime("%a, %d %b %Y %H:%M:%S GMT")

    signed_message = create_signature(client_secret, client_id, http_verb, content_type, hashed_body, path_and_query, time_stamp)

    uri = URI(url)
    request = Net::HTTP::Get.new(uri)
    request['Authorization'] = "OloSignature #{client_id}:#{signed_message}"
    request['Date'] = time_stamp

    response = Net::HTTP.start(uri.hostname, uri.port, use_ssl: true) do |http|
      http.request(request)
    end

    puts "RESPONSE CODE: #{response.code}"
    puts "RESPONSE BODY: \n#{response.body}"
  end

  def post(client_id, client_secret, base_url)
    path_and_query = "/v1.1/users/exists"
    url = URI(base_url + path_and_query)
    http_verb = "POST"
    content_type = "application/json"
    http_body = "{\"email\":\"noreply@olo.com\"}"
    hashed_body = hash_request_body(http_body)
    time_stamp = Time.now.utc.strftime("%a, %d %b %Y %H:%M:%S GMT")

    signed_message = create_signature(client_secret, client_id, http_verb, content_type, hashed_body, path_and_query, time_stamp)

    uri = URI(url)
    request = Net::HTTP::Post.new(uri, { 'Content-Type' => content_type, 'Date' => time_stamp })
    request['Authorization'] = "OloSignature #{client_id}:#{signed_message}"
    request.body = http_body

    response = Net::HTTP.start(uri.hostname, uri.port, use_ssl: true) do |http|
      http.request(request)
    end

    puts "RESPONSE CODE: #{response.code}"
    puts "RESPONSE BODY: \n#{response.body}"
  end
end

client_id = "ENTER YOUR OLO SANDBOX CLIENT ID"
client_secret = "ENTER YOUR OLO SANDBOX CLIENT SECRET"
base_url = "https://ordering.api.olosandbox.com"
program = Program.new

puts "********************* START GET Request *********************"
program.get(client_id, client_secret, base_url)
puts "********************* END GET Request *********************\n"

puts "********************* START POST Request *********************"
program.post(client_id, client_secret, base_url)
puts "********************* END POST Request *********************\n"
