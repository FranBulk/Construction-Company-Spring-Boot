package com.constructioncompany.web;

import com.constructioncompany.api.QueryRequest;
import com.constructioncompany.api.QueryResponse;
import com.constructioncompany.service.QueryService;
import java.util.Map;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/queries")
public class QueryController {

    private final QueryService queryService;

    public QueryController(QueryService queryService) {
        this.queryService = queryService;
    }

    @PostMapping("/{queryName}")
    public QueryResponse runQuery(@PathVariable String queryName, @RequestBody(required = false) QueryRequest request) {
        Map<String, String> args = request == null ? Map.of() : request.args();
        return new QueryResponse(queryService.execute(queryName, args));
    }
}
