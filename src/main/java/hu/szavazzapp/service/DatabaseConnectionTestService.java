package hu.szavazzapp.service;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;

import javax.sql.DataSource;

import org.springframework.stereotype.Service;

@Service
public class DatabaseConnectionTestService {

    private final DataSource dataSource;

    public DatabaseConnectionTestService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public DatabaseStatus testConnection() {
        try (Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery("SELECT 1")) {

            int testValue = 0;
            if (resultSet.next()) {
                testValue = resultSet.getInt(1);
            }

            return new DatabaseStatus(
                    true,
                    "Az adatbázis kapcsolat működik.",
                    testValue,
                    null,
                    LocalDateTime.now());
        } catch (Exception exception) {
            return new DatabaseStatus(
                    false,
                    "Nem sikerült kapcsolódni az adatbázishoz.",
                    null,
                    exception.getClass().getSimpleName() + ": " + exception.getMessage(),
                    LocalDateTime.now());
        }
    }

    public record DatabaseStatus(
            boolean connected,
            String message,
            Integer testValue,
            String error,
            LocalDateTime checkedAt) {
    }
}
