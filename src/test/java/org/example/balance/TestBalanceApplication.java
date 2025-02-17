package org.example.balance;

import org.springframework.boot.SpringApplication;
import org.testcontainers.utility.TestcontainersConfiguration;

public class TestBalanceApplication {

    public static void main(String[] args) {
        SpringApplication.from(BalanceApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
