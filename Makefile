SHELL:=/bin/bash

POSTGRE_VERSION=12
POSTGRE_PORT=5432
POSTGRE_DATA=/tmp/ethparser-db

.PHONY: help run start-db clean-db connect-db
all: help
help: Makefile
	@echo
	@echo " Choose a command run in "$(PROJECTNAME)":"
	@echo
	@sed -n 's/^##//p' $< | column -t -s ':' |  sed -e 's/^/ /'
	@echo

start-db:
	@docker network create eth-postgre || true
	@docker run -d --network eth-postgre --name eth-postgre -v ${POSTGRE_DATA}:/var/lib/postgresql/data -v $(shell pwd)/tmp/dump:/docker-entrypoint-initdb.d -e POSTGRES_USER=harvest -e POSTGRES_PASSWORD=password -e POSTGRES_DB=harvest -p ${POSTGRE_PORT}:5432 postgres:${POSTGRE_VERSION} || true
	@echo "DB started, connect with make connect-db"

stop-db:
	@docker stop eth-postgre || true

clean-db: stop-db
	@docker rm eth-postgre || true
	@docker network rm eth-postgre || true
	@sudo rm -Rf ${POSTGRE_DATA} || true
	@echo "DB cleaned"

logs-db: start-db
	@docker logs -f eth-postgre

connect-db:
	@echo "Default password is: password"
	@docker run -it --network eth-postgre --rm postgres:${POSTGRE_VERSION} psql -h eth-postgre -U harvest

backup-db:
	@echo "Default password is: password"
	@docker run -it --network eth-postgre -v $(shell pwd)/tmp/backup:/restore --rm postgres:${POSTGRE_VERSION} sh -c "pg_dump -h eth-postgre -U harvest harvest > /restore/dump_data.sql"

run:
	@mvn clean
	@mvn spring-boot:run -Dethparser.web3Url="https://eth-mainnet.alchemyapi.io/v2/$ALCHEMY_TOKEN" -Dethparser.etherscanApiKey=$ETHERSCAN_TOKEN -Dspring.datasource.url=jdbc:postgresql://localhost:5432/harvest -Dspring.datasource.username=harvest -Dspring.datasource.password=password

test:
	@mvn clean
	@mvn test -Dspring.config.location=file:src/test/resources/application.yml -Dethparser.web3Url="https://eth-mainnet.alchemyapi.io/v2/$ALCHEMY_TOKEN" -Dethparser.etherscanApiKey=$ETHERSCAN_TOKEN -Dspring.datasource.url=jdbc:postgresql://localhost:5432/harvest -Dspring.datasource.username=harvest -Dspring.datasource.password=password

build:
	@mvn verify -Dspring.config.location=file:tmp/application.yml