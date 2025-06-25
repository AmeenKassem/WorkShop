package workshop.demo.DTOs;

import com.fasterxml.jackson.databind.ObjectMapper;

public class SupplyDetails {

    public String address;
    public String city;
    public String state;
    public String zipCode;
    public String name;
    public String country;

    public SupplyDetails(String address, String city, String state, String zipCode) {
        this.address = address;
        this.city = city;
        this.state = state;
        this.zipCode = zipCode;
    }
    public SupplyDetails(String address, String city, String state, String zipCode,String name,String country) {
        this.address = address;
        this.city = city;
        this.state = state;
        this.zipCode = zipCode;
        this.name= name;
        this.country=country;
    }

    public SupplyDetails() {

    }

    public static SupplyDetails getTestDetails() {
        return new SupplyDetails("123 Test Street", "Testville", "TS", "00000");
    }

    //added this for tests
    public static SupplyDetails test_fail_supply() throws Exception {
        return new SupplyDetails(null, "Testville", "TS", null);
    }

    public static SupplyDetails getSupplyDetailsFromJSON(String json) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json, SupplyDetails.class);
    }
}
