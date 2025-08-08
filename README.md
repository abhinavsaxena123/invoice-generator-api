## Project Overview 📃
A backend Spring Boot API for creating, managing, and sending invoices.

invoice-generator-api is a robust backend service built with Java and the Spring Boot framework. It provides all the necessary server-side functionality for a modern invoice generation application. The API handles user authentication, data storage for invoices and users, and the business logic required to manage financial documents securely and efficiently. It is designed to be deployed as a cloud-native service, communicating with a separate frontend application via a RESTful interface.

## Technology Stack
Framework: Spring Boot 3

Language: Java

Security: Spring Security 6 (configured for JWT validation)

Database: Spring Data MongoDB for seamless integration with MongoDB Atlas.

Build Tool: Apache Maven

API Specification: REST


## Key Integrations
Clerk: For user sign-up, sign-in, and JWT-based authentication.

MongoDB Atlas: As the fully-managed cloud database for storing all application data.

Brevo: As the SMTP relay service for sending transactional emails.

