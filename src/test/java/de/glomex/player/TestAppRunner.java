package de.glomex.player;

import javafx.application.Application;

import java.io.IOException;
import java.util.logging.*;

/**
 * Created by <b>me@olexxa.com</b>
 */
public class TestAppRunner {

    public static void main(String[] args) throws IOException {
        LogManager.getLogManager().readConfiguration(
            Thread.currentThread().getContextClassLoader().getResourceAsStream("META-INF/logging.properties")
        );
        Application.launch(TestFXApplication.class);
    }

}
