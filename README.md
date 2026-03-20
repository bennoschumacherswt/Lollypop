# Lollypop — Matsecom Subscriber Management System

A JavaFX 21 desktop application for managing mobile subscribers, usage sessions,
and billing invoices for the fictional Matsecom network.

---

## Prerequisites

| Tool    | Version |
|---------|---------|
| Java    | 21+     |
| Maven   | 3.9+    |
| MySQL   | 8.0+    |

---

## Database Setup (run once)

1. Start MySQL and log in as root (or any user with CREATE DATABASE rights).
2. Run the schema script:

```sql
mysql -u root -p < src/main/resources/schema.sql
```

This creates the `lollypop` database and the `subscriber` + `session` tables.

3. Edit `src/main/resources/dbconfig.properties` if your credentials differ:

```properties
db.url=jdbc:mysql://localhost:3306/lollypop?useSSL=false&serverTimezone=UTC
db.user=root
db.password=test
```

---

## Running the Application

### Option A — Maven (recommended)

```bash
mvn javafx:run
```

### Option B — Build a fat JAR and run it

```bash
mvn package -DskipTests
java -jar target/matsecom-subscriber-mgmt-1.0.0-SNAPSHOT.jar
```

A `dbconfig.properties` file must exist in the **same directory** as the JAR
(or on the classpath). Copy it from `src/main/resources/` if needed:

```bash
cp src/main/resources/dbconfig.properties .
java -jar target/matsecom-subscriber-mgmt-1.0.0-SNAPSHOT.jar
```

### Option C — IntelliJ IDEA

1. Open the project folder in IntelliJ.
2. Let IntelliJ import the Maven project (it will download dependencies automatically).
3. Make sure your Project SDK is set to JDK 21 (`File → Project Structure → SDK`).
4. Use the **"mvn javafx:run"** run configuration (pre-configured in `.idea/`).

---

## Project Structure

```
src/
├── main/
│   ├── java/com/lollypop/
│   │   ├── Main.java                  # Plain entry point (no extends Application)
│   │   ├── LollypopApp.java           # JavaFX Application entry point
│   │   ├── ServiceFactory.java        # Dependency-injection wiring
│   │   ├── dao/                       # Data Access Objects
│   │   ├── model/                     # Domain models + enums
│   │   ├── service/                   # Business logic
│   │   ├── ui/                        # JavaFX panels and windows
│   │   └── util/                      # InputValidator, CrashRecoveryManager
│   └── resources/
│       ├── dbconfig.properties        # DB connection settings
│       └── schema.sql                 # Database DDL
└── test/                              # JUnit tests
```

---

## Fixes Applied (vs. original zip)

| # | File | Problem | Fix |
|---|------|---------|-----|
| 1 | `pom.xml` | XML comments contained `--` (double dash), making the file invalid XML that some parsers rejected | Replaced `--` inside comments with single `-` |
| 2 | `src/main/resources/dbconfig.properties` | `db.password` and the next `db.url` were on the **same line** with no newline separator — MySQL password was read as `testdb.url=jdbc:...` | Added proper line break; properties now parse correctly |
| 3 | `src/main/java/com/lollypop/dao/dbconfig.properties` | Same corruption as above | Same fix |
| 4 | `.idea/runConfigurations/Main.xml` | `VM_PARAMETERS` hardcoded to `C:\apps\javafx-sdk-26\lib` (Windows-only absolute path) — would fail on any other machine | Removed the hardcoded path; added a **"mvn javafx:run"** Maven run config |
| 5 | `.idea/misc.xml` | `project-jdk-name="openjdk-26"` — referenced a non-existent SDK | Changed to `"21"` which IntelliJ resolves from any installed JDK 21 |

---

## Subscription Plans

| Plan        | Base fee | Included minutes | Extra min rate | Data      |
|-------------|----------|-----------------|----------------|-----------|
| GreenMobilS | €8/mo    | 0               | €0.08/min      | 500 MB    |
| GreenMobilM | €22/mo   | 100             | €0.06/min      | 2,048 MB  |
| GreenMobilL | €42/mo   | 150             | €0.04/min      | 5,120 MB  |

## Terminal Types

| Terminal         | Technologies | Max data rate |
|------------------|-------------|---------------|
| PhairPhone       | 2G + 3G     | 20 Mbit/s     |
| Pear_aphone_4s   | 2G + 3G     | 20 Mbit/s     |
| Samsung_S42plus  | 2G + 3G + 4G| 300 Mbit/s    |
