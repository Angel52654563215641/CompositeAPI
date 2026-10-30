# E-Commerce Gateway & Dashboard — Composite API

Proyecto académico individual con tres aplicaciones Spring Boot:
- `catalog-service` (puerto 8081): catálogo de productos.
- `inventory-service` (puerto 8082): existencias, almacén y entrega.
- `composite-api` (puerto 8080): consulta los dos servicios y ExchangeRate-API de forma concurrente, calcula el precio en MXN y consolida el JSON.

## Requisitos
- JDK 21 o superior
- Maven 3.9+
- Conexión a internet para consultar ExchangeRate-API

## Ejecutar en Windows (PowerShell)
Abre tres terminales en la carpeta raíz del proyecto y ejecuta un comando en cada una:

Terminal 1:
```powershell
mvn -pl catalog-service spring-boot:run
```

Terminal 2:
```powershell
mvn -pl inventory-service spring-boot:run
```

Terminal 3:
```powershell
mvn -pl composite-api spring-boot:run
```

Si Maven no reconoce los módulos al ejecutar desde la raíz, primero compila todo:
```powershell
mvn clean package
```
Después puedes ejecutar cada módulo con `mvn -pl nombre-del-modulo spring-boot:run`.

## Endpoints
- `GET http://localhost:8081/microservices/products/P-101`
- `GET http://localhost:8082/microservices/inventory/P-101`
- `GET http://localhost:8080/api/v1/products/P-101/summary`

Productos de prueba: `P-101`, `P-102`, `P-103`. Para probar un ID inexistente, utiliza `P-999`; la API compuesta debe responder con error HTTP 502 si una fuente falla.

## Panel web
Con los tres servicios en ejecución, abre `http://localhost:8080/` para consultar el panel de productos. Puedes buscar por ID o seleccionar uno de los productos de prueba. El panel consume el endpoint de resumen existente; no modifica las rutas ni el formato de respuesta de la API.

## Ejemplo de respuesta
El precio en MXN es variable porque depende de la tasa de cambio actual. Ejemplo ilustrativo:
```json
{
  "product_id": "P-101",
  "title": "Laptop Gamer XYZ",
  "description": "Laptop para videojuegos y tareas exigentes",
  "category": "Computadoras",
  "price_usd": 1200.00,
  "price_mxn": 20400.00,
  "exchange_rate_mxn": 17.00,
  "stock_status": "IN_STOCK",
  "available_units": 15,
  "warehouse_location": "Almacén Chihuahua",
  "estimated_delivery": "2 a 4 días hábiles",
  "currency_conversion_source": "ExchangeRate-API (open.er-api.com)",
  "status": "SUCCESS",
  "generated_at": "2026-10-29T20:00:00Z"
}
```
Los valores de tipo de cambio y fecha son ilustrativos; el servicio calcula el precio con la tasa devuelta en cada consulta.

## Manejo de errores
- Producto o inventario inexistente: el servicio correspondiente responde 404.
- Si una llamada a servicios internos o a la API externa falla, el Composite API responde 502 Bad Gateway.
- Si la API externa no devuelve una tasa MXN, se informa una respuesta incompleta como error de gateway.

## Git y GitHub (Feature Branch)
```powershell
git init
git add .
git commit -m "chore: crear estructura inicial del composite api"
git branch -M main
git checkout -b feature/composite-api
git add .
git commit -m "feat: implementar api compuesta de productos e inventario"
```
Crea un repositorio vacío en GitHub y conecta la URL que GitHub te proporcione:
```powershell
git remote add origin URL_DE_TU_REPOSITORIO
git push -u origin main
git push -u origin feature/composite-api
```
No pegues literalmente `URL_DE_TU_REPOSITORIO`: reemplázala por la URL real de tu repositorio.

## Nota
Esta plantilla es una base funcional para la práctica. Antes de entregar, ejecuta `mvn clean package`, inicia los tres servicios y prueba los endpoints y el panel web.



