# Update to 2025 Versions

## Bumps

* Java 19 -> 24
* Pi4J 2.3.0 -> 3.0.1
* Vaadin 24.0.4 -> 24.8.6
    * Could not execute build-frontend goal: Error occured during goal execution: Unsupported class file major version
      68
* Spring Boot Starter 3.0.6 -> 3.5.4
    * The build process encountered an error: Could not execute prepare-frontend goal. Error occured during goal
      execution: com/fasterxml/jackson/core/util/Separators$Spacing

## Code changes

* Pi4J
    * BoardInfo library removed as it is now integrated in the Pi4J core library
* Vaadin
    * SideNav
    * Needed to fix a build problem causing the production version to throw console error about push not working: make
      sure to remove `src/main/bundles` when upgrading Vaadin versions. Remove all these to make sure the build will be
      OK:
        * `/frontend/generated/`
        * `/src/main/bundles`
        * `/target`
        * `/package.json`
        * `/package-lock.json`
        * `/tsconfig.json`
        * `/types.d.ts`