import org.junit.Test
import static liquibase.harness.util.TestUtils.parseQuery

class SnowflakeQueryFormatTest {

    @Test
    void testSnowflakeFormatExtraction() {
        String generatedSql = "alter session set jdbc_query_result_format = 'JSON'\nCREATE TABLE test_table (id int)"
        String expectedSql = "CREATE TABLE test_table (id int)"
        
        String cleanedSql = parseQuery(generatedSql)
        assert cleanedSql == expectedSql
    }
}
