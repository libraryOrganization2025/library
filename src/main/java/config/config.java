package config;

import io.github.cdimascio.dotenv.Dotenv;

public class config {

    private static final Dotenv dotenv = Dotenv.load();

    public static final String EMAIL = dotenv.get("EMAIL_USERNAME");
    public static final String EMAIL_PASSWORD = dotenv.get("EMAIL_PASSWORD");
}
