package com.constructioncompany.api;

public record MachineDto(
    String machineId,
    String model,
    String type,
    boolean used
) {
}
