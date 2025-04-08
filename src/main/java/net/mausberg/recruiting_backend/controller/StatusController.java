package net.mausberg.recruiting_backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import net.mausberg.recruiting_backend.config.DatabaseConfig;

@RestController
public class StatusController {

    @Autowired
    private DatabaseConfig databaseConfig;

    @GetMapping("/status")
    public String getStatus() {
        if (databaseConfig.isDatabaseReachable()) {
            return "Database is reachable";
        } else {
            return "Database is not reachable";
        }
    }
}
