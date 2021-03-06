package de.glomex.player.javafx;

import de.glomex.player.api.events.SubscribeControl;
import de.glomex.player.api.playback.PlaybackControl;
import de.glomex.player.model.api.Logging;
import de.glomex.player.model.playback.EmptyPlaybackListener;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Logger;

/**
 * Created by <b>me@olexxa.com</b>
 */
public class JavaFXUtils {

    private static final Logger log = Logging.getLogger(JavaFXUtils.class);

    static class JavaFXPlaybackListener extends EmptyPlaybackListener {

        private final ImprovedButton playBtn;
        private final Text positionWidget;

        public JavaFXPlaybackListener(ImprovedButton playBtn, Text positionWidget) {
            this.playBtn = playBtn;
            this.positionWidget = positionWidget;
        }

        @Override
        public void onPlay() {
            playBtn.setSelectedSuppressed(true);
        }

        @Override
        public void onPause() {
            playBtn.setSelectedSuppressed(false);
        }

        @Override
        public void onSeek(long position) {
            ensureFxThread( () -> positionWidget.setText("At " + position) );
        }

        @Override
        public void onFinished() {
            playBtn.setSelectedSuppressed(true);
        }

    }

    static class ImprovedButton extends ToggleButton {

        private boolean suppressCommands;

        public ImprovedButton(Image onImage, Image offImage, Runnable onFunction, Runnable offFunction) {
            ImageView toggleImage = new ImageView();
            toggleImage.setPreserveRatio(true);
            toggleImage.setFitWidth(32);
            toggleImage.imageProperty().bind(Bindings
                    .when(selectedProperty())
                    .then(onImage)
                    .otherwise(offImage)
            );
            setGraphic(toggleImage);

            setBorder(Border.EMPTY);
            setBackground(Background.EMPTY);

            selectedProperty().addListener(
                (observable, oldValue, value) -> {
                    if (!suppressCommands)
                        (value ? onFunction : offFunction).run();
                }
            );
        }

        public void setSelectedSuppressed(boolean state) {
            ensureFxThread( () -> {
                suppressCommands = true;
                setSelected(state);
                suppressCommands = false;
            });
        }
    }

    public static VBox createBars(String text, Node component, PlaybackControl playbackControl, SubscribeControl subscribeManager) {
        VBox layout = new VBox();
        {
            HBox buttonBar = new HBox();
            buttonBar.setStyle("-fx-border-color: black");
            {
                ClassLoader cl = Thread.currentThread().getContextClassLoader();
                buttonBar.setBackground(new Background(new BackgroundImage(
                    new Image(cl.getResourceAsStream("background.gif")),
                    BackgroundRepeat.REPEAT, BackgroundRepeat.NO_REPEAT,
                    BackgroundPosition.CENTER, BackgroundSize.DEFAULT
                )));
                buttonBar.setAlignment(Pos.CENTER);

                Label label = new Label(text);
                label.setAlignment(Pos.CENTER_LEFT);

                Text positionWidget = new Text();
                ImprovedButton playBtn = new ImprovedButton(
                    new Image(cl.getResourceAsStream("pause.png")),
                    new Image(cl.getResourceAsStream("play.png")),
                    playbackControl::play,
                    playbackControl::pause
                );

                subscribeManager.registerListener(new JavaFXPlaybackListener(playBtn, positionWidget));

                buttonBar.getChildren().addAll(label, playBtn, positionWidget);
            }
            layout.getChildren().addAll(buttonBar, component);
        }
        return layout;
    }

    public static void ensureFxThread(@NotNull Callback action) {
        if (Platform.isFxApplicationThread())
            action.callback();
        else
            Platform.runLater(action::callback);
    }
}
