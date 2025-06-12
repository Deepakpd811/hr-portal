package com.capgemini.hrmanagement.hr_portal.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class LocationDTO {
    private BigDecimal locationId;
    private String streetAddress;
    private String postalCode;
    private String city;
    private String stateProvince;
    private String countryId;
}
