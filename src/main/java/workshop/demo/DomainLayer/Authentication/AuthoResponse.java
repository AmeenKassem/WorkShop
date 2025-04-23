package workshop.demo.DomainLayer.Authentication;

import com.fasterxml.jackson.databind.ObjectMapper;

public class AuthoResponse {

    public String userName;
    public int id;

    public boolean isRegisterd() {
        return userName != null;
    }

    public AuthoResponse(String userName,int id) {
        this.userName = userName;
        this.id=id;
    }

    public AuthoResponse() {} // Needed for deserialization

    // Convert to JSON
    public String toJson() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(this);
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert to JSON", e);
        }
    }

    // Construct from JSON
    public AuthoResponse(String json) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            AuthoResponse parsed = mapper.readValue(json, AuthoResponse.class);
            this.userName = parsed.userName;
            this.id = parsed.id;
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse from JSON", e);
        }
    }
}
