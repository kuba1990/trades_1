Trade Enrichment API

This document provides instructions on how to use the Trade Enrichment API endpoints to upload trade data and retrieve enriched trade data.

## Endpoints

### POST `/enrich`

This endpoint is used to upload a CSV file containing trade data and start the processing command. The server generates a unique identifier for the processing request and returns it.

#### Request

- **URL**: `/enrich`
- **Method**: `POST`
- **Content-Type**: `multipart/form-data`
- **Parameter**:
  - `file`: The CSV file containing trade data.

#### Example cURL Command
curl -X POST -F "file=@path/to/your/trade.csv" http://localhost:8080/enrich


### GET `/enrich`

This endpoint is used to retrieve the enriched trade data as a CSV file based on the provided identifier

#### Request

- **URL**: `/enrich`
- **Method**: `GET`
- **Parameter**:
  - `identifier`: The unique identifier received from the POST request.

#### Example cURL Command
curl -X GET "http://localhost:8080/enrich?identifier=your-identifier

#### Error Handling Example
If the order is not ready or does not exist, you will receive the following response:

{
"status": 202,
"error": "Order not ready or not exists"
}

#### Known Issues and Areas for Improvement
1. **Lack of Endpoint Security**
   Currently, the API endpoints lack security measures. There are no authentication or authorization mechanisms in place to protect the endpoints from unauthorized access. It is recommended to implement security measures such as:

Authentication: Ensure that only authenticated users can access the endpoints.
Authorization: Implement role-based access control to restrict access to certain endpoints based on user roles.
HTTPS: Use HTTPS to encrypt data transmitted between the client and server.

2.**Lack of Performance Testing**
Performance testing has not been conducted for the API. It is crucial to evaluate how the API performs under various load conditions to ensure it can handle high traffic and large datasets efficiently. Recommended performance testing includes:

Load Testing: Determine how the API behaves under expected load conditions.
Stress Testing: Evaluate the API's performance under extreme conditions to identify its breaking point.
Scalability Testing: Assess the API's ability to scale up or down based on load.

3. **Need for More Extensive End-to-End Tests**
Currently, there is a lack of comprehensive end-to-end tests for the API. While some tests are in place, they should be extended and enhanced to cover all critical paths and edge cases. This includes:

End-to-End Testing: Ensure that the entire process, from uploading a file to retrieving enriched data, works seamlessly.
Integration Testing: Validate the interaction between different components of the system.
Unit Testing: Increase coverage of individual components to ensure they function as expected.

4. **Architectural Considerations and Need for Refactoring**
The current architecture of the application is relatively weak and may require refactoring, especially if new functionalities are to be added in the future. The existing design may not support scalability, maintainability, and extensibility effectively


5.  **Lack of validation and error handling **
    The application currently does not validate the format of the data being uploaded and lacks error handling for such cases. It is crucial to implement 
    data validation.


