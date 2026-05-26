package com.constructioncompany.web;

import com.constructioncompany.api.ConstructionSiteDto;
import com.constructioncompany.api.EmployeeDto;
import com.constructioncompany.repository.ConstructionSiteRepository;
import com.constructioncompany.repository.EmployeeRepository;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/construction-sites")
public class ConstructionSiteController {

    private final ConstructionSiteRepository constructionSiteRepository;
    private final EmployeeRepository employeeRepository;

    public ConstructionSiteController(
        ConstructionSiteRepository constructionSiteRepository,
        EmployeeRepository employeeRepository
    ) {
        this.constructionSiteRepository = constructionSiteRepository;
        this.employeeRepository = employeeRepository;
    }

    @GetMapping
    public List<ConstructionSiteDto> allSites() {
        return constructionSiteRepository.findAll();
    }

    @GetMapping("/{siteId}")
    public ResponseEntity<ConstructionSiteDto> siteById(@PathVariable String siteId) {
        return constructionSiteRepository.findById(siteId)
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{siteId}/employees")
    public List<EmployeeDto> employeesBySite(@PathVariable String siteId) {
        return employeeRepository.findByConstructionSite(siteId);
    }
}
