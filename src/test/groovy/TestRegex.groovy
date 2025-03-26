import org.junit.Test

class TestRegex {

    @Test
    void testRemoveSnowflakeJdbcFormatCommand() {
        String sql = "alter session set jdbc_query_result_format = 'JSON' CREATE TABLE test_table (id int)"
        String cleaned = sql.replaceAll(/(?i)alter\s+session\s+set\s+jdbc_query_result_format\s*=\s*['"]JSON['"]/, "").trim()
        println "Original: '$sql'"
        println "Cleaned: '$cleaned'"
        assert cleaned == "CREATE TABLE test_table (id int)"

        // Test with spaces and different box
        sql = "ALTER SESSION SET  jdbc_query_result_format='JSON' CREATE TABLE test_table (id int)"
        cleaned = sql.replaceAll(/(?i)alter\s+session\s+set\s+jdbc_query_result_format\s*=\s*['"]JSON['"]/, "").trim()
        println "Original: '$sql'"
        println "Cleaned: '$cleaned'"
        assert cleaned == "CREATE TABLE test_table (id int)"
        
        // Test as it appears in the code
        sql = "alter session set jdbc_query_result_format = 'JSON'"
        cleaned = sql.replaceAll(/(?i)alter\s+session\s+set\s+jdbc_query_result_format\s*=\s*['"]JSON['"]/, "").trim()
        println "Original: '$sql'"
        println "Cleaned: '$cleaned'"
        assert cleaned == ""
        
        // Test with double quotes
        sql = "alter session set jdbc_query_result_format = \"JSON\""
        cleaned = sql.replaceAll(/(?i)alter\s+session\s+set\s+jdbc_query_result_format\s*=\s*['"]JSON['"]/, "").trim()
        println "Original: '$sql'"
        println "Cleaned: '$cleaned'"
        assert cleaned == ""
    }
}
