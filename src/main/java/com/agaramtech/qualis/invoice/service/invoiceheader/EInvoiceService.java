package com.agaramtech.qualis.invoice.service.invoiceheader;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

public class EInvoiceService {

    private static final String AUTH_URL = "https://apisandbox.whitebooks.in/einvoice/authenticate?email=dhivyadhakshna25@gmail.com";
    private String authToken;

    public String authenticate() throws Exception {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet request = new HttpGet(AUTH_URL);

        // Add headers
        request.addHeader("username", "BVMGSP");
        request.addHeader("password", "Wbooks@0142");
        request.addHeader("ip_address", "192.168.158.126");
        request.addHeader("client_id", "EINS9bb12e46-1325-4d63-b418-bca3ac9bc2d8");
        request.addHeader("client_secret", "EINSe3b9b484-f601-44e3-a6a1-c18ee8ffbdd7");
        request.addHeader("gstin", "29AAGCB1286Q000");
        request.addHeader("email", "dhivyadhakshna25@gmail.com");

        // Execute request
        String responseString = EntityUtils.toString(httpClient.execute(request).getEntity(), "UTF-8");

        // Debug: Print response
        System.out.println("Response from API: " + responseString);

        // Parse JSON response
        JSONObject responseJson = new JSONObject(responseString);
        if ("Sucess".equals(responseJson.getString("status_cd"))) {
            this.authToken = responseJson.getJSONObject("data").getString("AuthToken");
            return this.authToken;
        } else {
            throw new Exception("Authentication failed: " + responseJson.getString("status_desc"));
        }
    }


    public String getAuthToken() {
        return this.authToken;
    }
    
   

}
