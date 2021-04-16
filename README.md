#Read Me

##Requirements
* Java 15
* A MySQL 5.7 database named `utopia` with the [correct schema](https://github.com/Java-Feb-CRAM/liquibase-ut)
* Credentials in `~/.m2/settings.xml` to access the private shared maven repo. [Contact Rob Maes](https://github.com/robert-maes) for access
* A private Stripe API key
##Building
To build this project clone this repository and run `mvn clean install`

##Running
The following environment variables are required to run this application:
* `UT_MYSQL` : `{"username": "XXXXX","password": "XXXXX","host": "localhost","port": 3306}`
* `UT_EUREKA` : `http://localhost:8761/eureka`
* `UT_STRIPE_API_KEY` : `{"stripeApiKey": "XXXXX"}`

This application relies on the discovery server
