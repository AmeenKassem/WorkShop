package workshop.demo.InfrastructureLayer;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale.Category;
import org.json.JSONArray;
import org.json.JSONObject;

import org.springframework.stereotype.Repository;

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
        List<Integer> res= a.getSameProduct("bsle", -1, 0.35);
        for (Integer i : res) {
            System.err.println(i);
        }
    }

    public boolean isActive() {
        // TODO Auto-generated method stub
        return true;
    }
}
