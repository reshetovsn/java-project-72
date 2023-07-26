.DEFAULT_GOAL := run-dist

run-dist:
	@./build/install/app/bin/app

clean:
	@./gradlew clean

build:
	@./gradlew clean build

install:
	@./gradlew clean installDist

run:
	@./gradlew run

test:
	@./gradlew test

report:
	@./gradlew jacocoTestReport

lint:
	@./gradlew checkstyleMain checkstyleTest
	
update-deps:
	@./gradlew useLatestVersions

.PHONY: build

