package com.constructioncompany.api;

public record WarehouseDto(
    String warehouseId,
    String constructionSiteId,
    String name,
    String country,
    String city,
    String street,
    String address,
    String phone
) {
}
