package liquibase.harness.change;

import org.h2.tools.TriggerAdapter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;

class H2Trigger extends TriggerAdapter {

    @Override
    public void fire(Connection connection, ResultSet resultSet, ResultSet resultSet1) throws SQLException {
        try {
            PreparedStatement stmt = connection.prepareStatement(
                    "SELECT * FROM posts;");
            stmt.execute();
        } catch (SQLException exception) {
            Logger.getLogger(H2Trigger.class.getName()).warning("Failed to execute H2 Database trigger! " + exception.getMessage());
        } finally {
            connection.close();
        }
    }
}
