# Jenkins adapter.

In order to function correctly you need to create you own ".env"-file below src/main/resources.
After creation you also need to provide the following parameters:

* JENKINS_USER
* JENKINS_PWD
* JENKINS_URL
* jwk_kid
* jwk_url
* jwk_alg

## Building the service

```bash
./gradlew build
```

When started the service provides following paths:

- on port 8080:

* GET: /api/v1/jenkins-adapter/<microservice>/lastbuild
* GET: /api/v1/jenkins-adapter/<microservice>/lastsuccessfulbuild
* GET: /api/v1/jenkins-adapter/<microservice>/artifacts
* GET: /api/v1/jenkins-adapter/<microservice>/build/<number>/artifacts
* GET: /api/v1/jenkins-adapter/<microservice>/artifacts/<filename>
* GET: /api/v1/jenkins-adapter/<microservice>/build/<number>/artifacts/<filename>

- on port 8081:

* GET: /health
* GET: /metrics/
* GET: /metrics/counterA (or counterB)

Enjoy! :)