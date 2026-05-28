package Kizuna_core_service.shared.controller;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@RestController
@RequestMapping("/public/seed")
public class SeedController {

    private final DataSource dataSource;

    public SeedController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @PostMapping
    public String seedDatabase() {
        try (Connection connection = dataSource.getConnection()) {
            Resource resource = new ClassPathResource("db/seed/core-seed.sql");
            ScriptUtils.executeSqlScript(connection, resource);
            return "Database seeded successfully from core-seed.sql";
        } catch (SQLException e) {
            return "Error seeding database: " + e.getMessage();
        }
    }
}
