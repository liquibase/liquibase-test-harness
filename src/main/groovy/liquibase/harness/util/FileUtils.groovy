package liquibase.harness.util

import liquibase.harness.config.DatabaseUnderTest
import liquibase.harness.config.TestConfig
import liquibase.util.StreamUtil
import java.nio.charset.StandardCharsets
import java.util.logging.Logger
import java.util.stream.Collectors

class FileUtils {

    static SortedMap<String, String> resolveInputFilePaths(DatabaseUnderTest database, String basePath, String inputFormat) {
        inputFormat = inputFormat ?: ""
        def returnPaths = new TreeMap<String, String>()
        for (String filePath : TestConfig.instance.resourceAccessor.list(null, basePath, true,
                true, false)) {
            def validFile = false
            //is it a common changelog?
            if (filePath =~ basePath+"/[\\w.]*\\."+inputFormat+"\$") {
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
        Logger.getLogger(this.class.name).info("Found " + returnPaths.size() + " changeLogs for " + database.name +
                "/" + database.version + " in "+basePath)
        return returnPaths
    }

    static String getFileContent(String change, String databaseName, String version , String expectedFolder,
                                 String fileExtension) {
        def resourceAccessor = TestConfig.instance.resourceAccessor

        def content = resourceAccessor.openStream(null, expectedFolder + "/" + databaseName
                + "/" + version + "/" + change + fileExtension)
        if (content != null) {
            return StreamUtil.readStreamAsString(content)
        }

        content = resourceAccessor.openStream(null, expectedFolder + "/" + databaseName
                + "/" + change + fileExtension)
        if (content != null) {
            return StreamUtil.readStreamAsString(content)
        }

        content = resourceAccessor.openStream(null, expectedFolder + "/" + change + fileExtension)
        if (content != null) {
            return StreamUtil.readStreamAsString(content)
        }
        return null
    }

    static String getSqlFileContent(String changeObject, String databaseName,
                                    String version, String expectedFolder) {
        return getFileContent(changeObject, databaseName, version, expectedFolder, ".sql")
    }

    static String getJSONFileContent(String changeObject, String databaseName,
                                     String version, String expectedFolder) {
        return getFileContent(changeObject, databaseName, version, expectedFolder, ".json")
    }

    static String getResourceContent(String resourceName) {
        InputStream inputStream = this.class.getResourceAsStream(resourceName)
        assert inputStream : "Can't find resource file " + resourceName + "!"
        return new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8)).lines()
                .collect(Collectors.joining("\n"))
    }

    static Boolean deleteFile(String fileName) {
        try {
            new File(fileName).delete()
        } catch (IOException exception) {
            Logger.getLogger(this.class.name).severe("Failed to delete generated changelog " + exception.message + " "
                    + exception.printStackTrace())
            return false
        }
        return true
    }
}
