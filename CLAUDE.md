# Micronaut Slack Library Development Guide

## Build Commands
- `./gradlew build` - Build the project
- `./gradlew test` - Run all tests
- `./gradlew test --tests "com.agorapulse.slack.event.RunOnceBoltEventHandlerSpec"` - Run a single test
- `./gradlew check` - Run checkstyle, codenarc, and tests
- `./gradlew aggregateCheckstyle` - Run Java style checks
- `./gradlew aggregateCodenarc` - Run Groovy style checks

## Code Style Guidelines
- Java 17+ with standardized naming conventions (camelCase for methods/variables)
- 160 character line length maximum
- Use spaces, not tabs for indentation
- Braces required for control structures
- Static imports are allowed
- Groovy code must use @CompileStatic except in *Function and *Spec classes
- Prefer explicit error handling and avoid catching generic Exceptions
- Use UTC timezone and English locale in tests for location independence
- Follow proper license headers (Apache 2.0)
- Favor composition over inheritance
- Test files should end with *Spec.groovy