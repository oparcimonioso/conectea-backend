package com.conectea.backend.model;

import jakarta.persistence.Embeddable;

@Embeddable
public class Address {
    private String cep;
    private String street;
    private String number;
    private String neighborhood;
    private String city;
    private String state;

    // getters & setters
    public String getCep() { return cep; }
    public void setCep(String cep) { this.cep = cep; }
    public String getStreet() { return street; }
    public void setStreet(String street) { this.street = street; }
    public String getNumber() { return number; }
    public void setNumber(String number) { this.number = number; }
    public String getNeighborhood() { return neighborhood; }
    public void setNeighborhood(String neighborhood) { this.neighborhood = neighborhood; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
}