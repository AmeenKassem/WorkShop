package workshop.demo.InfrastructureLayer;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale.Category;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.databind.ObjectMapper;

@Repository
public class AISearch {
    private String api = "http://localhost:5000";
    private String match_request = "/get-matches-products";

    public List<Integer> getSameProduct(String inputToSearch, int categoryNum, double conffedince) {
        try {
            String encodedInput = inputToSearch.replace(" ", "%2520");
            System.out.println(encodedInput);

            URL url = new URL(api + match_request + "?category=" + categoryNum + "&same=" + conffedince + "&input="
                    + encodedInput);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();

            // Set method and headers
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json; utf-8");
            con.setRequestProperty("Accept", "application/json");
            con.setDoOutput(true);

            // JSON body
            // String jsonInputString = String.format("""
            // {
            // "input": "%s",
            // "category": -1,
            // "same": 1
            // }
            // """, inputToSearch);
            // // Send request
            // try (OutputStream os = con.getOutputStream()) {
            // byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
            // os.write(input, 0, input.length);
            // }

            int status = con.getResponseCode();
            // Read the response
            InputStream responseStream = (status >= 200 && status < 300)
                    ? con.getInputStream()
                    : con.getErrorStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(responseStream));
            String line;
            StringBuilder response = new StringBuilder();

            while ((line = in.readLine()) != null) {
                response.append(line);
            }

            in.close();

            // Print the response
            System.out.println("Response: " + response.toString());
            return extractProductIds(response.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Integer> extractProductIds(String jsonString) {
        JSONArray jsonArray = new JSONArray(jsonString);
        int[] productIds = new int[jsonArray.length()];
        List<Integer> ids = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject obj = jsonArray.getJSONObject(i);
            productIds[i] = obj.getInt("productId");
            ids.add(productIds[i]);
        }

        return ids;
    }

    public static void main(String[] args) {
        AISearch a = new AISearch();
        a.trainProduct("bsle",new String[]{"cheetos","btata"});
        new Thread(new Runnable(){

            @Override
            public void run() {
                // TODO Auto-generated method stub
                a.trainProduct("bsle",new String[]{"cheetos","btata"});
                
            }

        }).start();
        

    }

    public boolean isActive() {
        return true;
    }

    private static final HttpClient client = HttpClient.newHttpClient();
    private static final ObjectMapper mapper = new ObjectMapper(); // From Jackson

    public void trainProduct(String name, String[] keywords) {
        try {
            // Prepare JSON payload
            Map<String, Object> payload = new HashMap<>();
            payload.put("string1", name);
            payload.put("string2_list", Arrays.asList(keywords));
            payload.put("same", 1); // or 0 depending on logic

            String json = mapper.writeValueAsString(payload);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:5000/addPairs")) // Replace with your Flask server address
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            // Send the request
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("Response Code: " + response.statusCode());
            System.out.println("Response Body: " + response.body());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
