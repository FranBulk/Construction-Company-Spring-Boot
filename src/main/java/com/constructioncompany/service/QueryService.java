package com.constructioncompany.service;

import com.constructioncompany.api.PaymentRequest;
import com.constructioncompany.api.PaymentResult;
import com.constructioncompany.repository.PaymentRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class QueryService {

    private static final String NOT_FOUND = "No variable found in the DB";

    private final JdbcTemplate jdbcTemplate;
    private final PaymentRepository paymentRepository;
    private final Map<String, Function<Map<String, String>, String>> queries;

    public QueryService(JdbcTemplate jdbcTemplate, PaymentRepository paymentRepository) {
        this.jdbcTemplate = jdbcTemplate;
        this.paymentRepository = paymentRepository;
        this.queries = Map.ofEntries(
            Map.entry("ListConstructionSiteReferences", args -> reference("Construction sites:", """
                select cs.ConstructionSite_ID,
                       nvl(c.Name, 'No customer') || ' - ' || nvl(cs.City, 'No city')
                from ConstructionSite cs
                left join Customers c on cs.Customer_ID = c.Customer_ID
                order by cs.ConstructionSite_ID
                """)),
            Map.entry("ListCustomerReferences", args -> reference("Customers:", """
                select Customer_ID, Name
                from Customers
                order by Customer_ID
                """)),
            Map.entry("ListEmployeeReferences", args -> combinedReference(
                section("Employees:", """
                    select Employee_ID, FirstName || ' ' || LastName
                    from Employees
                    order by Employee_ID
                    """),
                section("Licenses:", """
                    select l.Employee_ID || '-' || l.Machine_ID,
                           e.FirstName || ' ' || e.LastName || ' - ' || nvl(l.LicenseType, 'No license type')
                    from Licenses l
                    join Employees e on l.Employee_ID = e.Employee_ID
                    order by l.Employee_ID, l.Machine_ID
                    """)
            )),
            Map.entry("ListPaymentReferences", args -> combinedReference(
                section("Employee payment records:", """
                    select ep.Year || '-W' || lpad(ep.WeekNumber, 2, '0') || '-' || ep.Employee_ID,
                           e.FirstName || ' ' || e.LastName
                    from EmployeePayment ep
                    join Employees e on ep.Employee_ID = e.Employee_ID
                    order by ep.Year, ep.WeekNumber, ep.Employee_ID
                    """),
                section("Employees:", """
                    select Employee_ID, FirstName || ' ' || LastName
                    from Employees
                    order by Employee_ID
                    """)
            )),
            Map.entry("ListPayrollPaymentReferences", args -> reference("Employees available for payment:", """
                select Employee_ID, FirstName || ' ' || LastName
                from Employees
                order by Employee_ID
                """)),
            Map.entry("ListMachineReferences", args -> combinedReference(
                section("Machines:", """
                    select Machine_ID, nvl(Model, 'No model') || ' - ' || nvl(Type, 'No type')
                    from Machines
                    order by Machine_ID
                    """),
                section("Machines in construction:", """
                    select mc.ConstructionSite_ID || '-' || mc.Machine_ID,
                           nvl(m.Model, 'No model') || ' - ' || nvl(m.Type, 'No type')
                    from MachinesInConstruction mc
                    join Machines m on mc.Machine_ID = m.Machine_ID
                    order by mc.ConstructionSite_ID, mc.Machine_ID
                    """)
            )),
            Map.entry("ListMaterialWarehouseReferences", args -> combinedReference(
                section("Materials:", """
                    select Material_ID, Name
                    from Material
                    order by Material_ID
                    """),
                section("Warehouses:", """
                    select WareHouse_ID, Name
                    from WareHouse
                    order by WareHouse_ID
                    """),
                section("Stock:", """
                    select s.WareHouse_ID || '-' || s.Material_ID,
                           nvl(w.Name, 'No warehouse') || ' - ' || nvl(m.Name, 'No material')
                    from Stock s
                    join WareHouse w on s.WareHouse_ID = w.WareHouse_ID
                    join Material m on s.Material_ID = m.Material_ID
                    order by s.WareHouse_ID, s.Material_ID
                    """)
            )),
            Map.entry("ListExternalServiceReferences", args -> combinedReference(
                section("External service companies:", """
                    select Company_ID, CompanyName
                    from ServiceExternalCompany
                    order by Company_ID
                    """),
                section("Services in construction:", """
                    select sic.ConstructionSite_ID || '-' || sic.Company_ID,
                           sec.CompanyName || ' - ' || nvl(sic.TypeService, 'No service type')
                    from ServiceInConstruction sic
                    join ServiceExternalCompany sec on sic.Company_ID = sec.Company_ID
                    order by sic.ConstructionSite_ID, sic.Company_ID
                    """)
            )),
            Map.entry("ConstructionInfo", this::constructionInfo),
            Map.entry("SearchConstructionSiteByName", this::searchConstructionSiteByName),
            Map.entry("SearchConstructionSiteByID", this::searchConstructionSiteById),
            Map.entry("AllOfTheEmployeesInTheConstruction", this::employeesInConstruction),
            Map.entry("CustomerInfo", this::customerInfo),
            Map.entry("ConsultEmployeesUnderManagers", args -> rows("""
                select e.FirstName || ' ' || e.LastName || ' Boss: ' || b.FirstName || ' ' || b.LastName
                from Employees e
                join Employees b on e.Boss_ID = b.Employee_ID
                order by e.Employee_ID
                """)),
            Map.entry("EmployeeInfo", this::employeeInfo),
            Map.entry("EmployeesInBank", this::employeesInBank),
            Map.entry("EmployeesWithSameLicense", this::employeesWithSameLicense),
            Map.entry("ConsultPaidPeriod", this::consultPaidPeriod),
            Map.entry("ConsultDeductibleTax", this::consultDeductibleTax),
            Map.entry("PayWeek", this::payWeek),
            Map.entry("ListEmployeesToPay", this::listEmployeesToPay),
            Map.entry("PayEmployee", this::payEmployee),
            Map.entry("ListAllMachines", args -> rows("""
                select Machine_ID || ' Model: ' || Model || ' Type: ' || Type || ' Is used: ' || IsUsed
                from Machines
                order by Machine_ID
                """)),
            Map.entry("ListAllMachinesInConstruction", this::machinesInConstruction),
            Map.entry("MaterialPurpose", this::materialPurpose),
            Map.entry("CurrentStockQuantity", this::currentStockQuantity),
            Map.entry("ListAllWarehouses", args -> rows("""
                select Name || ' Country: ' || Country || ' City: ' || City ||
                       ' Street: ' || Street || ' Address: ' || Address || ' Phone: ' || Phone
                from WareHouse
                order by WareHouse_ID
                """)),
            Map.entry("WhichWareHousesAreInTheConstructionSite", this::warehousesInConstruction),
            Map.entry("ServiceExternalCompanies", args -> rows("""
                select CompanyName || ' Phone: ' || Phone || ' Email: ' || Email
                from ServiceExternalCompany
                order by Company_ID
                """)),
            Map.entry("ListServiceCompanyByTypeService", this::serviceCompanyByType)
        );
    }

    public String execute(String name, Map<String, String> args) {
        Function<Map<String, String>, String> query = queries.get(name);
        if (query == null) {
            throw new IllegalArgumentException("Unknown query: " + name);
        }
        return query.apply(args == null ? Map.of() : args);
    }

    private String constructionInfo(Map<String, String> args) {
        return single("""
            select 'Country: ' || cs.Country || ' City: ' || cs.City ||
                   ' Street: ' || cs.Street || ' Address: ' || cs.Address ||
                   ' Customer: ' || nvl(c.Name, 'No customer')
            from ConstructionSite cs
            left join Customers c on cs.Customer_ID = c.Customer_ID
            where cs.ConstructionSite_ID = ?
            """, required(args, "construction_id"));
    }

    private String searchConstructionSiteByName(Map<String, String> args) {
        return rows("""
            select 'Construction site: ' || cs.ConstructionSite_ID ||
                   ' Customer: ' || c.Name ||
                   ' Country: ' || cs.Country ||
                   ' City: ' || cs.City ||
                   ' Street: ' || cs.Street ||
                   ' Address: ' || cs.Address
            from ConstructionSite cs
            join Customers c on cs.Customer_ID = c.Customer_ID
            where upper(c.Name) like '%' || upper(?) || '%'
            order by cs.ConstructionSite_ID
            """, required(args, "name"));
    }

    private String searchConstructionSiteById(Map<String, String> args) {
        return single("""
            select 'Construction site: ' || cs.ConstructionSite_ID ||
                   ' Customer: ' || nvl(c.Name, 'No customer') ||
                   ' Country: ' || cs.Country ||
                   ' City: ' || cs.City ||
                   ' Street: ' || cs.Street ||
                   ' Address: ' || cs.Address
            from ConstructionSite cs
            left join Customers c on cs.Customer_ID = c.Customer_ID
            where cs.ConstructionSite_ID = ?
            """, required(args, "construction_site_id"));
    }

    private String employeesInConstruction(Map<String, String> args) {
        return rows("""
            select Employee_ID || ' ' || FirstName || ' ' || LastName
            from Employees
            where ConstructionSite_ID = ?
            order by Employee_ID
            """, required(args, "construction_site_id"));
    }

    private String customerInfo(Map<String, String> args) {
        return single("""
            select Name || ' Phone: ' || Phone || ' Email: ' || Email || ' Customer type: ' || CustomerType
            from Customers
            where Customer_ID = ?
            """, required(args, "customer_id"));
    }

    private String employeeInfo(Map<String, String> args) {
        return single("""
            select FirstName || ' ' || LastName ||
                   ' Start date: ' || to_char(StartDate, 'YYYY-MM-DD') ||
                   ' Hire date: ' || to_char(HireDate, 'YYYY-MM-DD') ||
                   ' Phone: ' || Phone
            from Employees
            where Employee_ID = ?
            """, required(args, "employee_id"));
    }

    private String employeesInBank(Map<String, String> args) {
        return rows("""
            select FirstName || ' ' || LastName || ' Bank account: ' || BankAccount
            from Employees
            where BankName = ?
            order by Employee_ID
            """, required(args, "bank_name"));
    }

    private String employeesWithSameLicense(Map<String, String> args) {
        return rows("""
            select e.Employee_ID || ' ' || e.FirstName || ' ' || e.LastName ||
                   ' Machine: ' || l.Machine_ID ||
                   ' License type: ' || l.LicenseType
            from Licenses l
            join Employees e on l.Employee_ID = e.Employee_ID
            where l.LicenseType = ?
            order by e.Employee_ID
            """, required(args, "license_type"));
    }

    private String consultPaidPeriod(Map<String, String> args) {
        return single("""
            select PaymentAmount
            from EmployeePayment
            where Year = ?
              and WeekNumber = ?
              and Employee_ID = ?
            """, requiredInt(args, "year"), requiredInt(args, "week_number"), required(args, "employee_id"));
    }

    private String consultDeductibleTax(Map<String, String> args) {
        return single("""
            select Tax
            from EmployeePayment
            where Year = ?
              and WeekNumber = ?
              and Employee_ID = ?
            """, requiredInt(args, "year"), requiredInt(args, "week_number"), required(args, "employee_id"));
    }

    private String payWeek(Map<String, String> args) {
        Integer count = jdbcTemplate.queryForObject("""
            select count(*)
            from EmployeePayment
            where Year = ?
              and WeekNumber = ?
              and Employee_ID = ?
              and PayDate is not null
            """, Integer.class, requiredInt(args, "year"), requiredInt(args, "week_number"), required(args, "employee_id"));
        return count != null && count > 0 ? "True" : "False";
    }

    private String listEmployeesToPay(Map<String, String> args) {
        int year = requiredInt(args, "year");
        int weekNumber = requiredInt(args, "week_number");
        List<String> lines = paymentRepository.findEmployeesToPay(year, weekNumber).stream()
            .map(status -> "%s %s | Current pay: %s | Payment amount: %s | Tax deducted: %s | Net payment: %s | Status: %s".formatted(
                status.employeeId(),
                status.employeeName(),
                money(status.currentPay()),
                money(status.paymentAmount()),
                money(status.tax()),
                money(status.netPayment()),
                status.status()
            ))
            .toList();
        return lines.isEmpty() ? NOT_FOUND : "Payment week: " + weekNumber + " Year: " + year + "\n" + String.join("\n", lines);
    }

    private String payEmployee(Map<String, String> args) {
        PaymentResult result = paymentRepository.registerPayment(new PaymentRequest(
            requiredInt(args, "year"),
            requiredInt(args, "week_number"),
            required(args, "employee_id")
        ));

        if ("NOT_FOUND".equals(result.status())) {
            return NOT_FOUND;
        }

        String title = "ALREADY_PAID".equals(result.status())
            ? "This employee has already been paid."
            : "Payment registered successfully.";
        return """
            %s
            Employee ID: %s
            Employee: %s
            Week: %d
            Year: %d
            Current pay: %s
            Payment amount: %s
            Tax deducted: %s
            Net payment: %s%s
            """.formatted(
            title,
            result.employeeId(),
            result.employeeName(),
            result.weekNumber(),
            result.year(),
            money(result.currentPay()),
            money(result.paymentAmount()),
            money(result.tax()),
            money(result.netPayment()),
            result.payDate() == null ? "" : "\nPay date: " + result.payDate()
        ).trim();
    }

    private String machinesInConstruction(Map<String, String> args) {
        return rows("""
            select m.Machine_ID || ' Model: ' || m.Model ||
                   ' Type: ' || m.Type ||
                   ' Start date: ' || to_char(mc.StartMachineDate, 'YYYY-MM-DD') ||
                   ' Finish date: ' || to_char(mc.FinishMachineDate, 'YYYY-MM-DD')
            from MachinesInConstruction mc
            join Machines m on mc.Machine_ID = m.Machine_ID
            where mc.ConstructionSite_ID = ?
            order by m.Machine_ID
            """, required(args, "construction_site_id"));
    }

    private String materialPurpose(Map<String, String> args) {
        return single("""
            select Purpose
            from Material
            where upper(Name) = upper(?)
            """, required(args, "name"));
    }

    private String currentStockQuantity(Map<String, String> args) {
        return single("""
            select QuantityKg
            from Stock
            where WareHouse_ID = ?
              and Material_ID = ?
            """, required(args, "warehouse_id"), required(args, "material_id"));
    }

    private String warehousesInConstruction(Map<String, String> args) {
        return rows("""
            select WareHouse_ID || ' ' || Name ||
                   ' Country: ' || Country ||
                   ' City: ' || City ||
                   ' Street: ' || Street ||
                   ' Address: ' || Address ||
                   ' Phone: ' || Phone
            from WareHouse
            where ConstructionSite_ID = ?
            order by WareHouse_ID
            """, required(args, "construction_site_id"));
    }

    private String serviceCompanyByType(Map<String, String> args) {
        return rows("""
            select sec.CompanyName ||
                   ' Phone: ' || sec.Phone ||
                   ' Email: ' || sec.Email ||
                   ' Construction site: ' || sic.ConstructionSite_ID ||
                   ' Service: ' || sic.ServiceDescription
            from ServiceInConstruction sic
            join ServiceExternalCompany sec on sic.Company_ID = sec.Company_ID
            where sic.TypeService = ?
            order by sec.Company_ID
            """, required(args, "type_service"));
    }

    private String reference(String title, String sql) {
        List<String> rows = jdbcTemplate.query(sql, (rs, rowNum) -> rs.getString(1) + " | " + rs.getString(2));
        return rows.isEmpty() ? NOT_FOUND : title + "\n" + String.join("\n", rows);
    }

    private String combinedReference(ReferenceSection... sections) {
        StringBuilder output = new StringBuilder();
        for (ReferenceSection section : sections) {
            String value = reference(section.title(), section.sql());
            if (!NOT_FOUND.equals(value)) {
                if (output.length() > 0) {
                    output.append("\n\n");
                }
                output.append(value);
            }
        }
        return output.length() == 0 ? NOT_FOUND : output.toString();
    }

    private String rows(String sql, Object... args) {
        List<String> rows = jdbcTemplate.query(sql, (rs, rowNum) -> Objects.toString(rs.getObject(1), ""), args);
        return rows.isEmpty() ? NOT_FOUND : String.join("\n", rows);
    }

    private String single(String sql, Object... args) {
        List<String> rows = jdbcTemplate.query(sql, (rs, rowNum) -> Objects.toString(rs.getObject(1), ""), args);
        return rows.stream().findFirst().filter(value -> !value.isBlank()).orElse(NOT_FOUND);
    }

    private String required(Map<String, String> args, String name) {
        String value = args.get(name);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " is required.");
        }
        return value.trim();
    }

    private int requiredInt(Map<String, String> args, String name) {
        return Integer.parseInt(required(args, name));
    }

    private static ReferenceSection section(String title, String sql) {
        return new ReferenceSection(title, sql);
    }

    private static String money(BigDecimal value) {
        return value == null ? "0.00" : "%.2f".formatted(value);
    }

    private record ReferenceSection(String title, String sql) {
    }
}
