package st.cbse.crm.customerComponent.data;


public class Address {
    private String postalCode;
    private String street;
    private String city;

    public Address() {}

    public Address(String postalCode, String street, String city) {
        this.postalCode = postalCode;
        this.street = street;
        this.city = city;
    }
}
