package com.constructioncompany.web;

import com.constructioncompany.api.EmployeeDto;
import com.constructioncompany.api.MachineDto;
import com.constructioncompany.api.MaterialDto;
import com.constructioncompany.api.WarehouseDto;
import com.constructioncompany.repository.EmployeeRepository;
import com.constructioncompany.repository.MachineRepository;
import com.constructioncompany.repository.MaterialRepository;
import com.constructioncompany.repository.WarehouseRepository;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class CatalogController {

    private final EmployeeRepository employeeRepository;
    private final MachineRepository machineRepository;
    private final WarehouseRepository warehouseRepository;
    private final MaterialRepository materialRepository;

    public CatalogController(
        EmployeeRepository employeeRepository,
        MachineRepository machineRepository,
        WarehouseRepository warehouseRepository,
        MaterialRepository materialRepository
    ) {
        this.employeeRepository = employeeRepository;
        this.machineRepository = machineRepository;
        this.warehouseRepository = warehouseRepository;
        this.materialRepository = materialRepository;
    }

    @GetMapping("/employees")
    public List<EmployeeDto> allEmployees() {
        return employeeRepository.findAll();
    }

    @GetMapping("/machines")
    public List<MachineDto> allMachines() {
        return machineRepository.findAll();
    }

    @GetMapping("/warehouses")
    public List<WarehouseDto> allWarehouses() {
        return warehouseRepository.findAll();
    }

    @GetMapping("/materials")
    public List<MaterialDto> allMaterials() {
        return materialRepository.findAll();
    }
}
