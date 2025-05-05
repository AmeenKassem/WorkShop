package workshop.demo.DTOs;

public class SupplyDetails {
    public String address;
    public String city;
    public String state;
    public String zipCode;

    public SupplyDetails(String address, String city, String state, String zipCode) {
        this.address = address;
        this.city = city;
        this.state = state;
        this.zipCode = zipCode;
    }
}
