package com.mmva.newsapp;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;

/**
 * Temporary test class to generate BCrypt hash for a given password.
 * Run this test to see the generated hash in the console output and in
 * BCRYPT_HASH.txt file.
 */
public class BCryptHashGeneratorTest {

    @Test
    public void generateBCryptHash() throws IOException {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String password = "Password1234";
        String hash = encoder.encode(password);

        String output = "\n" +
                "==================================================\n" +
                "BCrypt Hash Generator\n" +
                "==================================================\n" +
                "Password: " + password + "\n" +
                "Generated Hash: " + hash + "\n" +
                "Hash Length: " + hash.length() + "\n" +
                "==================================================\n" +
                "Use this hash in the SQL UPDATE:\n" +
                "UPDATE admin_users SET admin_users_password_hash = '" + hash
                + "' WHERE admin_users_email = 'Manikandan@newsapp.com';\n" +
                "==================================================\n";

        System.out.println(output);

        // Write to file for easy access
        String filePath = Paths.get("BCRYPT_HASH.txt").toAbsolutePath().toString();
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write(output);
            System.out.println("Hash also written to: " + filePath);
        }
    }
}
