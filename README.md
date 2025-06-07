KYC Identity Verification Workflow
Project Overview
This project demonstrates a workflow for testing the KYC (Know Your Customer) identity verification process using Sumsub’s third-party verification services. The goal is to streamline the identity verification process and ensure compliance with regulatory requirements.

Before you begin, ensure you have met the following requirements:

JDK 11 or higher
Gradle
An account with Sumsub and access to their API
A database (e.g., PostgreSQL, MySQL)
Installation
Clone the repository:

bash

Copy
git clone https://github.com/yourusername/kyc-verification.git
cd kyc-verification
Build the project:

bash

Copy
./gradlew build
Configuration
Open src/main/resources/application.properties and add the following configuration:

properties

Copy
# Sumsub API Configuration
sumsub.app-token=your_app_token
sumsub.secret-key=your_secret_key
sumsub.base-url=https://api.sumsub.com

# Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/your_db_name
spring.datasource.username=your_db_username
spring.datasource.password=your_db_password
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
Replace your_app_token, your_secret_key, your_db_name, your_db_username, and your_db_password with your actual credentials.

Ensure your database is up and running.

Usage
Start the application:

bash

Copy
./gradlew bootRun
Open your web browser and navigate to http://localhost:8080 to access the KYC verification interface.

Follow the prompts to submit a new identity verification request.

Testing
To run tests for the application, use the following command:

bash

Copy
./gradlew test
Ensure all tests pass before deploying or further developing the project.

Contributing
Contributions are welcome! Please follow these steps:

Fork the repository.
Create a new branch (git checkout -b feature-branch).
Make your changes and commit them (git commit -m 'Add new feature').
Push to the branch (git push origin feature-branch).
Open a pull request.
