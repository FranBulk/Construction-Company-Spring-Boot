package com.constructioncompany.api;

public record ConstructionSiteDto(
    String constructionSiteId,
    String customerId,
    String customerName,
    String country,
    String city,
    String street,
    String address
) {
}
