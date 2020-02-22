package live.heatmap.hivemqauth;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Properties;

public class MySQLBackend {

    private final HikariDataSource dataSource;
    private final Properties config;

    public MySQLBackend(Properties config, HikariConfig hikariConfig) {
        this.config = config;
        this.dataSource = new HikariDataSource(hikariConfig);
    }

    public boolean checkMatch(String username, String password) throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("SELECT " + config.getProperty("passwordColumnName") + " FROM " + config.getProperty("table") + " WHERE " + config.getProperty("usernameColumnName") + " = `" + username + "`");
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                String passwordHash = resultSet.getString(config.getProperty("passwordColumnName"));
                switch (resultSet.getString("hashAlgorithm").toLowerCase()) {
                    case "plain":
                        if (password.equals(passwordHash)) {
                            return true;
                        }
                        break;
                    default:
                        throw new IllegalArgumentException("Unknown hash method: " + resultSet.getString("hashAlgorithm"));
                }
            }
        }
        return false;
    }

}
