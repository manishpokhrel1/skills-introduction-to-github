This folder should contain the canonical Gradle wrapper JAR and properties.

Files expected:
- `gradle-wrapper.properties` (provided)
- `gradle-wrapper.jar` (Gradle wrapper bootstrap JAR)

To generate the proper `gradle-wrapper.jar` on your machine, run:

```bash
gradle wrapper --gradle-version 8.3
```

If you cannot run `gradle` locally, download `gradle-wrapper.jar` from a trusted Gradle distribution or run the `run-tests.ps1`/`run-tests.sh` scripts which invoke Gradle inside Docker.
