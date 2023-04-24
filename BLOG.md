Upgrade to newer versions

Bumped versions in pom.xml
* Spring Boot parent: 3.0.5
* Vaadin: 24.0.4
* Pi4J: 2.3.0

Removed:

* package.json
* tsconfig.json
* vite.config.js
* vite.generated.js
* 
* 
  https://vaadin.com/docs/v23/upgrading/V23-V24-steps

To clean and build Vaadin

```
mvn vaadin:clean-frontend
mvn vaadin:prepare-frontend
mvn vaadin:build-frontend
```


