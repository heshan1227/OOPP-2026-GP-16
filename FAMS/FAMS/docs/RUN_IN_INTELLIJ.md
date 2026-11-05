# Open and run FAMS in IntelliJ IDEA

## Open the project
1. Extract this ZIP to a normal folder (do not open a Java file by itself).
2. In IntelliJ IDEA, choose **File → Open** and select the extracted folder that contains `pom.xml`.
3. If prompted, choose **Open as Project** and **Trust Project**.
4. Open the Maven tool window and click **Reload All Maven Projects** if dependency import does not start automatically.
5. Set **File → Project Structure → Project SDK** to a locally installed JDK 17 or newer. The Maven compiler is configured to target Java 17.

## Run the application
- Select the shared run configuration **FAMS Main** in the top toolbar and click Run.
- If IntelliJ does not show it, open `src/com/fams/Main.java`, click the green gutter arrow beside `main`, or create an Application configuration with main class `com.fams.Main`.
- The first run needs internet access for Maven to download MySQL Connector/J.

## Database setup
1. Start MySQL.
2. Execute `database_schema.sql` against a clean local database.
3. Check `src/com/fams/database/DBConnection.java` and configure `FAMS_DB_URL`, `FAMS_DB_USER`, and `FAMS_DB_PASSWORD` for your MySQL installation if needed.
4. Launch the application again. Database-dependent modules will not fully work until MySQL is configured.

## Terminal build (optional)
From the extracted project folder, run `mvn clean package`, then `java -jar target/fams-integrated-1.0.0.jar`. Maven must be installed for this terminal method; IntelliJ's Maven integration can also run the lifecycle.

**Note:** The ZIP includes the source and a shared IntelliJ run configuration. It cannot include your local JDK installation, MySQL server, database credentials, or downloaded Maven dependencies.
