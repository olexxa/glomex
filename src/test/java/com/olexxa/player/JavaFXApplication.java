package com.olexxa.player;

import com.olexxa.player.api.PlayerAPIFactory;
import com.olexxa.player.api.playlist.Content;
import com.olexxa.player.javafx.JavaFXUtils;
import com.olexxa.player.model.api.ContentImpl;
import javafx.application.Application;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Example of 3rd party APP.
 *
 * It prepares UI Container element to integrate player into,
 * creates UI controls for playback and initialize a player API.
 *
 * Created by <b>me@olexxa.com</b>
 */
public class JavaFXApplication extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        stage.setTitle("Java FX Player");
        stage.setMinWidth(400);
        stage.setMinHeight(300);

        initializeAPI(stage);
    }

    private void initializeAPI(Stage stage) throws MalformedURLException {
        URL url = new URL("http://static.ipoker.com/aogtwister/video/Age%20Of%20The%20Gods%2009.mp4");
        Content clip = new ContentImpl(url);

        PlayerAPIFactory.<Stage, Node>createAPI(
            stage, true,
            (api, playerComponent) -> {
                Parent pane = JavaFXUtils.createBars("3rd party APP controls", playerComponent, api.playbackController());
                Scene scene = new Scene(pane);
                stage.setScene(scene);
                stage.show();

                //api.requestFullScreen();
                api.playlistManager().addContent(clip);
                api.playlistManager().skipTo(clip);
                api.playbackController().play();
                //api.destroy(() -> System.out.print("Kill it"));
            }
        );
    }

}
