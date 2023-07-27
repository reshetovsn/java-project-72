setup:
	gradle wrapper --gradle-version 8.2.1

clean:
	./gradlew clean

build:
	./gradlew clean build

start:
	APP_ENV=development ./gradlew run

install:
	./gradlew install

start-dist:
	APP_ENV=production ./build/install/app/bin/app

generate-migrations:
	./gradlew generateMigrations

lint:
	./gradlew checkstyleMain checkstyleTest

test:
	./gradlew test

report:
	./gradlew jacocoTestReport

check-updates:
	./gradlew dependencyUpdates

image-build:
	docker build -t hexletcomponents/app:latest .

image-push:
	docker push hexletcomponents/app:latest

.PHONY: build
