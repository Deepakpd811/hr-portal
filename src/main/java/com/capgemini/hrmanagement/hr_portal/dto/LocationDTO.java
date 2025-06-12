package com.capgemini.hrmanagement.hr_portal.dto;

import lombok.Data;

@Data
public class LocationDTO {
    private Long locationId;
    private String streetAddress;
    private String postalCode;
    private String city;
    private String stateProvince;
    private String countryName;
    private String regionName;

    @Override
    public String toString() {
        return streetAddress + ", " + city + ", " + stateProvince + ", " + countryName + ", " + regionName;
    }
}
