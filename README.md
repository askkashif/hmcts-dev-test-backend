# HMCTS Dev Test Backend
This will be the backend for the brand new HMCTS case management system. As a potential candidate we are leaving
this in your hands. Please refer to the brief for the complete list of tasks! Complete as much as you can and be
as creative as you want.

You should be able to run `./gradlew build` to start with to ensure it builds successfully. Then from that you
can run the service in IntelliJ (or your IDE of choice) or however you normally would.

There is an example endpoint provided to retrieve an example of a case. You are free to add/remove fields as you
wish.


The Application contains the following features

1) Signup
2) Signin
3) CRUD for cases
4) ADMIN can perform all CRUD operations
5) USER can only create and read cases
6) JWT based authentication
7) Relavant tests for unit, functional, integration and smoke.
8) Swagger accesible at http://localhost:4000/swagger-ui/index.html

Run Docker via docker-compose up -d to spin up postgres

Run backend via intalij by executing Application.java