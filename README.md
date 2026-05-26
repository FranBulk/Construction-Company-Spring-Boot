# Construction Company

This repository now contains two applications:

- The original Python desktop application.
- A Spring Boot REST API that connects to the existing Oracle database and uses RabbitMQ plus Apache Camel for asynchronous payroll payment processing.

## Spring Boot API

### Requirements

- Docker Desktop.
- Oracle running locally with this connection:
  - User: `ConstructionCompany`
  - Password: `const123`
  - Service: `XEPDB1`
  - Port: `1521`

### Run RabbitMQ and the Spring Boot API with Docker

```powershell
docker compose up --build
```

The API is available at:

```text
http://localhost:8080
```

RabbitMQ Management is available at:

```text
http://localhost:15672
User: guest
Password: guest
```

The Dockerized API connects to Oracle through:

```text
jdbc:oracle:thin:@host.docker.internal:1521/XEPDB1
```

If you run the API directly from an IDE instead of Docker, it uses:

```text
jdbc:oracle:thin:@localhost:1521/XEPDB1
```

### Useful endpoints

```text
GET  /api/health
GET  /api/customers
GET  /api/customers/{customerId}
POST /api/customers
GET  /api/construction-sites
GET  /api/construction-sites/{siteId}
GET  /api/construction-sites/{siteId}/employees
GET  /api/employees
GET  /api/machines
GET  /api/warehouses
GET  /api/materials
GET  /api/payments/due?year=2026&weekNumber=2
GET  /api/payments/{year}/{weekNumber}/{employeeId}
POST /api/payments/pay
POST /api/payments/pay-now
```

`POST /api/payments/pay` sends a payment request to RabbitMQ. Apache Camel consumes the message and registers the payment in Oracle.

Example:

```powershell
Invoke-RestMethod `
  -Method Post `
  -Uri http://localhost:8080/api/payments/pay `
  -ContentType "application/json" `
  -Body '{"year":2026,"weekNumber":2,"employeeId":"E001"}'
```

`POST /api/payments/pay-now` performs the same payment synchronously, without RabbitMQ, which is useful for quick database testing.

### Environment variables

```text
ORACLE_URL
ORACLE_USER
ORACLE_PASSWORD
RABBITMQ_HOST
RABBITMQ_PORT
RABBITMQ_USERNAME
RABBITMQ_PASSWORD
SERVER_PORT
```

## Original Desktop App

## Run

Double-click `run_construction_manager.bat`, or run:

```powershell
python construction_manager_app.py
```

If Python is installed but Oracle support is missing:

```powershell
pip install oracledb
```

## App Login

Use this login to open the desktop app:

```text
User: ConstructionCompany
Password: const123
```

The folder should contain:

```text
construction_manager_app.py
run_construction_manager.bat
scripts/
  ConstructionSite.py
  Customers.py
  EmployeePayment.py
  Employees.py
  Licenses.py
  Machines.py
  MachinesInConstruction.py
  Material.py
  ReferenceData.py
  ServiceExternalCompany.py
  ServiceInConstruction.py
  Stock.py
  WareHouse.py
database/
  Constructora_fixed.ddl
  Constructora_seed_data.sql
```
