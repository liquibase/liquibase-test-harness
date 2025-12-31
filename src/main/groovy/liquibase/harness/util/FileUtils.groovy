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
     * Loads the skip changetypes list for a database from skipChangetypes.txt.
     * The file should contain one changetype name per line. Lines starting with # are comments.
     *
     * @param databaseName The database name
     * @param expectedFolder The expected folder path (e.g., "liquibase/harness/change/expectedSql")
     * @return Set of changetype names to skip
     */
    static Set<String> loadSkipChangetypes(String databaseName, String expectedFolder) {
        Set<String> skipSet = new HashSet<>()
        def resourceAccessor = TestConfig.instance.resourceAccessor
        def skipFilePath = expectedFolder + "/" + databaseName + "/skipChangetypes.txt"
        def resource = resourceAccessor.get(skipFilePath)

        if (resource.exists()) {
            def text = StreamUtil.readStreamAsString(resource.openInputStream())
            text.split("\n").each { String line ->
                def trimmed = line.trim()
                // Skip empty lines and comments
                if (trimmed && !trimmed.startsWith("#")) {
                    skipSet.add(trimmed)
                }
            }
            if (!skipSet.isEmpty()) {
                Logger.getLogger(FileUtils.class.name).info("Loaded " + skipSet.size() +
                        " skip changetypes for " + databaseName + " from " + skipFilePath)
            }
        }
        return skipSet
    }

    /**
     * Checks if a changetype should be skipped for a database based on skipChangetypes.txt.
     *
     * @param change The changetype name (e.g., "addAutoIncrement")
     * @param databaseName The database name
     * @param expectedFolder The expected folder path
     * @return true if the changetype is listed in skipChangetypes.txt
     */
    static boolean shouldSkipChangetype(String change, String databaseName, String expectedFolder) {
        return loadSkipChangetypes(databaseName, expectedFolder).contains(change)
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
        // Check if this changetype should be skipped via skipChangetypes.txt
        if (shouldSkipChangetype(change, databaseName, expectedFolder)) {
            return "SKIP TEST\nSkipped via skipChangetypes.txt"
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
