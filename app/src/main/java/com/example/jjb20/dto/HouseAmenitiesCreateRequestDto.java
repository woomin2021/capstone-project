package com.example.jjb20.dto;

import java.util.List;

public class HouseAmenitiesCreateRequestDto {
    private Long houseId;
    private List<String> amenityCodes;

    public HouseAmenitiesCreateRequestDto(Long houseId, List<String> amenityCodes) {
        this.houseId = houseId;
        this.amenityCodes = amenityCodes;
    }

    public Long getHouseId() {
        return houseId;
    }

    public void setHouseId(Long houseId) {
        this.houseId = houseId;
    }

    public List<String> getAmenityCodes() {
        return amenityCodes;
    }

    public void setAmenityCodes(List<String> amenityCodes) {
        this.amenityCodes = amenityCodes;
    }
}

