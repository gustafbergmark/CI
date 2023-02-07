# CI server


## Dependencies and versions
The CI server can only be used by Gradle Java projects using JUnit5.
Make sure to include the following dependencies in `build.gradle`. To deploy the server you will need to have ngrok 
installed (think so). 

`
TODO <insert gradle version and distribution>
implementation 'org.junit.jupiter:junit-jupiter:5.8.1'
testImplementation 'org.junit.jupiter:junit-jupiter-api:5.8.1'
testImplementation 'org.junit.jupiter:junit-jupiter:5.8.1'
testRuntimeOnly 'org.junit.jupiter:junit-jupiter-engine:5.8.1'
`

## Run server locally
To build and test the server locally, clone this repository and run the following in the command line: (FIX)

## Deploy server
TODO: BESKRIV NGROK PROCEDUREN?
To actually run the server 

## Webhooks and connecting project to CI
In order for the project to use the CI, check the [documentation](https://docs.github.com/en/developers/webhooks-and-events/webhooks/about-webhooks).
Add `<ngrok adress>` in the `Payload URL` field, only select `push` events. 

## Retrieve older builds
To retrieve older builds, got to the link <-adress>/builds.html which will show a list with links to all older 
builds. There you can choose to click on the link to a specific build to get more information. To implement this we
have a database in JSON format in the "database" directory where all the build history is saved. Once a new build is 
made the build history is updated. 

## Assessment branch


## Notification
Server sets the commit status as per this [documentation](https://docs.github.com/en/pull-requests/collaborating-with-pull-requests/collaborating-on-repositories-with-code-quality-features/about-status-checks).


## Contributions
### Glacier Ali
- Implement CI server running tests in project, in collaboration with Frida Grönberg
- Commit status, in collaboration with Frida Grönberg & Adam Jama.
- put everything together, in collaboration with everyone else.

### Frida Grönberg
- Implement CI server running tests in project, in collaboration with Glacier Ali
- Commit status, in collaboration with Glacier Ali & Adam Jama.
- put everything together, in collaboration with everyone else.

### Adam Jama
- Clone repository function, in collaboration with Gustaf Bergmark.
- Commit status, in collaboration with Glacier Ali & Frida Grönberg.
- put everything together, in collaboration with everyone else.

### Gustaf Bergmark
- Clone repository function, in collaboration with Adam Jama.
- Retrieve older builds in collaboration with Carl Peterson
- put everything together, in collaboration with everyone else.
- Set up server
- Set up webhooks for the CI server

### Carl Peterson
- Parsing of incoming HTTP request and its payload
- Retrieve older builds in collaboration with Gustaf Bergmark
- Put everything together, in collaboration with everyone else