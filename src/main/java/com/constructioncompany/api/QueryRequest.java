package com.constructioncompany.api;

import java.util.Map;

public record QueryRequest(Map<String, String> args) {
}
