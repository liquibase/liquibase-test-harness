package liquibase.harness.util

import liquibase.harness.config.DatabaseUnderTest
import liquibase.harness.config.TestConfig
import liquibase.util.StreamUtil

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths
import java.util.logging.Logger
import java.util.stream.Collectors

class FileUtils {

    static SortedMap<String, String> resolveInputFilePaths(DatabaseUnderTest database, String basePath, String inputFormat) {
        inputFormat = inputFormat ?: ""
        def returnPaths = new TreeMap<String, String>()

        // Check if this database excludes default changelogs
        boolean excludeDefaults = shouldExcludeDefaultChangelogs(database.name, basePath)

        for (def resource : TestConfig.instance.resourceAccessor.search(basePath, true)) {
            String filePath = resource.getPath()
            def validFile = false
            //is it a common changelog? (skip if excludeDefaultChangelogs marker exists)
            if (!excludeDefaults && filePath =~ basePath+"/[\\w.]*\\."+inputFormat+"\$") {
                validFile = true
            } else if (filePath =~ basePath+"/${database.name}/[\\w.]*\\.${inputFormat}\$") {
                //is it a database-specific changelog?
                validFile = true
            } else if (filePath =~ basePath+"/${database.name}/${database.version}/[\\w.]*\\.${inputFormat}\$") {
                //is it a database-major-version specific changelog?
                validFile = true
            }
            if (validFile) {
                def fileName = filePath.replaceFirst(".*/", "").replaceFirst("\\.[^.]+\$", "")
                if (!returnPaths.containsKey(fileName) || returnPaths.get(fileName).length() < filePath.length()) {
                    returnPaths.put(fileName, filePath)
                }
            }
        }
        Logger.getLogger(FileUtils.class.name).info("Found " + returnPaths.size() + " changeLogs for " + database.name +
                "/" + database.version + " in " + basePath + (excludeDefaults ? " (excluding defaults)" : ""))
        return returnPaths
    }

    /**
     * Checks if a database has an excludeDefaultChangelogs marker file.
     * When this marker exists in the database-specific changelog folder,
     * default changelogs from the base path are not inherited.
     * The marker file can have any extension (e.g., excludeDefaultChangelogs.txt)
     * or no extension at all.
     *
     * @param databaseName The database name (e.g., "snowflake", "dynamodb")
     * @param basePath The base changelog path (e.g., "liquibase/harness/change/changelogs")
     * @return true if the marker exists and defaults should be excluded
     */
    static boolean shouldExcludeDefaultChangelogs(String databaseName, String basePath) {
        def resourceAccessor = TestConfig.instance.resourceAccessor
        def dbFolder = basePath + "/" + databaseName

        // Search for any file starting with "excludeDefaultChangelogs" (any extension or none)
        for (def resource : resourceAccessor.search(dbFolder, false)) {
            def fileName = resource.getPath().replaceFirst(".*/", "")
            if (fileName.startsWith("excludeDefaultChangelogs")) {
                Logger.getLogger(FileUtils.class.name).info("Found " + fileName + " marker for " + databaseName +
                        " - default changelogs will not be inherited")
                return true
            }
        }
        return false
    }

    /**
     * Loads Pro-only changetype names from proOnlyChangetypes.txt files.
     * Merges a global file (applying to all databases) with a database-specific file.
     * File format: one changetype name per line; lines starting with # are comments.
     *
     * @param databaseName The database name
     * @param expectedFolder The expected folder path (e.g., "liquibase/harness/change/expectedSql")
     * @return Set of Pro-only changetype names relevant to this database
     */
    static Set<String> loadProOnlyChangetypes(String databaseName, String expectedFolder) {
        Set<String> proOnlySet = new HashSet<>()
        def resourceAccessor = TestConfig.instance.resourceAccessor

        // Global file — applies to every database
        def globalPath = expectedFolder + "/proOnlyChangetypes.txt"
        def globalResource = resourceAccessor.get(globalPath)
        if (globalResource.exists()) {
            StreamUtil.readStreamAsString(globalResource.openInputStream()).split("\n").each { String line ->
                def trimmed = line.trim()
                if (trimmed && !trimmed.startsWith("#")) {
                    proOnlySet.add(trimmed)
                }
            }
        }

        // Database-specific file — adds DB-specific Pro-only types
        def dbPath = expectedFolder + "/" + databaseName + "/proOnlyChangetypes.txt"
        def dbResource = resourceAccessor.get(dbPath)
        if (dbResource.exists()) {
            StreamUtil.readStreamAsString(dbResource.openInputStream()).split("\n").each { String line ->
                def trimmed = line.trim()
                if (trimmed && !trimmed.startsWith("#")) {
                    proOnlySet.add(trimmed)
                }
            }
            Logger.getLogger(FileUtils.class.name).info("Loaded DB-specific Pro-only changetypes for " + databaseName)
        }

        return proOnlySet
    }

    /**
     * Returns true when the changetype is Pro-only AND the test run is using community artifacts.
     * Controlled by the system property "useProArtifacts" (set to "true" for Pro runs).
     *
     * @param change The changetype name
     * @param databaseName The database name
     * @param expectedFolder The expected folder path
     * @return true if the changetype should be skipped in community mode
     */
    static boolean shouldSkipProOnlyChangetype(String change, String databaseName, String expectedFolder) {
        if ("true".equalsIgnoreCase(System.getProperty("useProArtifacts"))) {
            return false
        }
        return loadProOnlyChangetypes(databaseName, expectedFolder).contains(change)
    }

    static String getSqlFileContent(String change, String databaseName,
                                    String version, String expectedFolder) {
        return getFileContent(change, databaseName, version, expectedFolder, ".sql")
    }

    static String getJSONFileContent(String change, String databaseName,
                                     String version, String expectedFolder) {
        return getFileContent(change, databaseName, version, expectedFolder, ".json")
    }

    static String getResourceContent(String resourceName) {
        try (InputStream inputStream = FileUtils.class.getResourceAsStream(resourceName)) {
            assert inputStream : "Can't find resource file " + resourceName + "!"
            return new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8)).lines()
                    .collect(Collectors.joining("\n"))
        }
    }

    static String readFile(String filePath) {
        try {
            return Files.readString(Paths.get(filePath), StandardCharsets.UTF_8)
        } catch (IOException exception) {
            Logger.getLogger(FileUtils.class.name).severe("Failed to read file: " + filePath + " - " + exception.message)
            return ""
        }
    }

    static boolean deleteFile(String fileName) {
        boolean deleted = new File(fileName).delete()
        if (!deleted) {
            Logger.getLogger(FileUtils.class.name).warning("Failed to delete file: " + fileName)
        }
        return deleted
    }

    static String getFileContent(String change, String databaseName, String version, String expectedFolder,
                                  String fileExtension) {
        // Skip Pro-only changetypes when running against community Liquibase
        if (shouldSkipProOnlyChangetype(change, databaseName, expectedFolder)) {
            return "SKIP TEST\nPro-only changetype — skipped in community mode"
        }

        def resourceAccessor = TestConfig.instance.resourceAccessor
        def fileName = change + fileExtension

        // Try paths in order of specificity: version-specific -> db-specific -> default
        def pathsToTry = [
                "${expectedFolder}/${databaseName}/${version}/${fileName}",
                "${expectedFolder}/${databaseName}/${fileName}",
                "${expectedFolder}/${fileName}"
        ]

        for (String path : pathsToTry) {
            def resource = resourceAccessor.get(path)
            if (resource.exists()) {
                return StreamUtil.readStreamAsString(resource.openInputStream())
            }
        }
        return null
    }

    /**
     * Substitutes placeholders in expectedSql content with actual database values.
     * Supported placeholders:
     * - ${CATALOG_NAME} - replaced with the database's default catalog name
     * - ${SCHEMA_NAME} - replaced with the database's default schema name
     *
     * @param content The file content potentially containing placeholders
     * @param catalogName The catalog name to substitute (may be null)
     * @param schemaName The schema name to substitute (may be null)
     * @return Content with placeholders replaced, or original content if no placeholders found
     */
    static String substitutePlaceholders(String content, String catalogName, String schemaName) {
        if (content == null || !content.contains('${')) {
            return content
        }
        String result = content
        if (catalogName != null) {
            result = result.replace('${CATALOG_NAME}', catalogName)
        }
        if (schemaName != null) {
            result = result.replace('${SCHEMA_NAME}', schemaName)
        }
        return result
    }
}
