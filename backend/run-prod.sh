#!/bin/bash
cd "$(dirname "$0")"
./mvnw spring-boot:run -Dspring-boot.run.profiles=prod
