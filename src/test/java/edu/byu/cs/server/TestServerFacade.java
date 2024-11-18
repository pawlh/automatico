package edu.byu.cs.server;

import com.google.gson.Gson;
import edu.byu.cs.server.exception.ResponseParseException;
import edu.byu.cs.server.exception.ServerConnectionException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;

public class TestServerFacade {
    private final String serverURL;

    public TestServerFacade(String serverURL, int port) {
        this.serverURL = "http://%s:%d".formatted(serverURL, port);
    }

    public Object makeRequest(String method, String path) throws IOException, ServerConnectionException, ResponseParseException {
        return makeRequest(method, path, null, null, Object.class);
    }

    public <T> T makeRequest(String method, String path, Object request, Map<String, String> headers,
            Class<T> responseClass) throws IOException, ServerConnectionException, ResponseParseException {
        HttpURLConnection http = getConnection(method, serverURL + path);
        writeRequest(http, request, headers);
        connect(http);
        return readResponse(http, responseClass);
    }

    private HttpURLConnection getConnection(String method, String urlString) throws ServerConnectionException {
        try {
            URL url = (new URI(urlString)).toURL();
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            http.setRequestMethod(method);
            http.setDoOutput("POST".equals(method) || "PUT".equals(method));
            return http;
        } catch (IOException | URISyntaxException e) {
            throw new ServerConnectionException("Failed to set up HTTP connection: " + e.getMessage());
        }
    }

    private void writeRequest(HttpURLConnection http, Object requestBody, Map<String, String> headers) throws ServerConnectionException {
        try {
            writeHeaders(http, headers);
            writeRequestBody(http, requestBody);
        } catch (IOException e) {
            throw new ServerConnectionException("Could not write request body: " + e.getMessage());
        }
    }

    private void connect(HttpURLConnection http) throws ServerConnectionException {
        try {
            http.connect();
        } catch (IOException e) {
            throw new ServerConnectionException("Failed to connect to server: " + e.getMessage());
        }
    }

    private <T> T readResponse(HttpURLConnection http, Class<T> responseClass) throws IOException,
            ResponseParseException {
        String respString = getRespString(http);
        try {
            return new Gson().fromJson(respString, responseClass);
        } catch (Exception e) {
            String message = String.format("Error parsing response. Expected JSON, got '%s'", respString);
            throw new ResponseParseException(message, e);
        }
    }

    private void writeHeaders(HttpURLConnection http, Map<String, String> headers) {
        http.addRequestProperty("Content-type", "application/json");
        if (headers != null) {
            for (String headerName : headers.keySet()) {
                String headerValue = headers.get(headerName);
                http.addRequestProperty(headerName, headerValue);
            }
        }
    }

    private void writeRequestBody(HttpURLConnection http, Object request) throws IOException {
        if (request != null) {
            String requestData = new Gson().toJson(request);
            try (OutputStream reqBody = http.getOutputStream()) {
                reqBody.write(requestData.getBytes());
            }
        }
    }

    private String getRespString(HttpURLConnection http) throws IOException {
        InputStream respBody;
        if (http.getResponseCode() / 100 == 2) {
            respBody = http.getInputStream();
        } else {
            respBody = http.getErrorStream();
        }
        StringBuilder sb = new StringBuilder();
        InputStreamReader sr = new InputStreamReader(respBody);
        char[] buf = new char[1024];
        int len;
        while ((len = sr.read(buf)) > 0) {
            sb.append(buf, 0, len);
        }
        respBody.close();
        return sb.toString();
    }
}