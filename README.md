# CI server
Here is a CI server that supports continuous integration for Gradle projects. 
At each push to the project repo, the server is called as a webhook by Github. 
Then the server builds the project, runs all tests and sets the commit status.

## Dependencies and versions
The CI server can only be used by Gradle Java projects, specifically `gradle-7.5.1` using JUnit5.
Make sure to include the following dependencies in `build.gradle`.

`
implementation 'org.junit.jupiter:junit-jupiter:5.8.1'
testImplementation 'org.junit.jupiter:junit-jupiter-api:5.8.1'
testImplementation 'org.junit.jupiter:junit-jupiter:5.8.1'
testRuntimeOnly 'org.junit.jupiter:junit-jupiter-engine:5.8.1'
`

## Run server locally
Store the access token in file oauthtoken.secret. The access token should be stored in plain text without quotes.
The token must have access to the repo:status scope.

To build and test the server locally, clone this repository.
**Intellij**: 
1. reload build.gradle
2. to run all tests run `test` in `build.gradle`
3. run main program in `ContinuousIntegrationServerTest.java`.

**command line**
1. run `./gradlew build` to build project and run all tests
2. run `./gradlew run` to launch server

## Webhooks and connecting project to CI
In order for the project to use the CI, check the [documentation](https://docs.github.com/en/developers/webhooks-and-events/webhooks/about-webhooks).
Add `http://188.150.30.242:8090/` in the `Payload URL` field, only select `push` events. 

## Retrieve older builds
To retrieve older builds, browse the link http://188.150.30.242:8090/builds.html, which will show a list with links to all older 
builds. There you can choose to click on the link to a specific build to get more information. To implement this we
have a database in JSON format in the "database" directory where all the build history is saved. Once a new build is 
made the build history is updated.

## Build and test execution
The compilation and test execution was implemented through using [Gradle Tooling API](https://docs.gradle.org/current/javadoc/org/gradle/tooling/package-summary.html).
The `BuildLauncher` builds the projects and runs specified tasks, defined in `build.gradle`.
For the CI server to be able to run all tests, the project should include the following in `build.gradle`.
This functionality has been tested by cloning a dummy repo with 2 separate branches.
One branch contains true tests, and one branch contains failed tests.


```
test {
    useJUnitPlatform()
}
```

## Assessment branch
The branch *assessment* was created to test if the CI server is working correctly.  

## Notification of build status
Server sets the commit status as per this [documentation](https://docs.github.com/en/rest/commits/statuses?apiVersion=2022-11-28#create-a-commit-status).
The POST request sent to set the status must include a header containing an authorization token, generated in GitHub.

**NOTE: Make sure to enable permission for the token to set commit statuses.**

## Contributions
### Glacier Ali
- Implement CI server running tests in project, in collaboration with Frida Grönberg.
- Commit status, in collaboration with the rest of the team.
- Put everything together, in collaboration with everyone else.

### Frida Grönberg
- Implement CI server running tests in project, in collaboration with Glacier Ali.
- Commit status, in collaboration with the rest of the team.
- Put everything together, in collaboration with everyone else.

### Adam Jama
- Clone repository function, in collaboration with Gustaf Bergmark.
- Commit status, in collaboration with the rest of the team.
- Put everything together, in collaboration with everyone else.

### Gustaf Bergmark
- Clone repository function, in collaboration with Adam Jama.
- Retrieve older builds in collaboration with Carl Peterson.
- Put everything together, in collaboration with everyone else.
- Set up server.
- Set up webhooks for the CI server
- Commit status, in collaboration with the rest of the team.

### Carl Peterson
- Parsing of incoming HTTP request and its payload.
- Retrieve older builds in collaboration with Gustaf Bergmark
- Put everything together, in collaboration with everyone else.
- Commit status, in collaboration with the rest of the team.