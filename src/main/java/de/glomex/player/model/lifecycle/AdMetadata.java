package de.glomex.player.model.lifecycle;

import de.glomex.player.api.lifecycle.*;
import de.glomex.player.api.playlist.MediaID;
import de.glomex.player.model.media.MediaUUID;
import org.jetbrains.annotations.NotNull;

import java.net.URL;

/**
 * Created by <b>me@olexxa.com</b>
 */
public class AdMetaData implements AdData {

    MediaID id;
    protected URL metadataURL;
    protected AdPosition position;
    protected Long time;

    // mock: for tests
    public AdMetaData() {
        id = new MediaUUID();
    }

    public AdMetaData(@NotNull AdData origin) {
        this.id = origin.id();
        this.metadataURL = origin.metadataURL();
        this.position = origin.position();
    }

    @Override
    public @NotNull MediaID id() {
        return id;
    }

    @Override
    public URL metadataURL() {
        return metadataURL;
    }

    @Override
    public AdPosition position() {
        return position;
    }

    /**
     * Cap value to duration passed.
     */
    public Long resolve(Long duration) {
        if (position.isRelative()) {
            //noinspection ConstantConditions
            if (duration == null)
                time = position.relative() == 0? 0l : null;
            else
                time  =Math.round(duration * position.relative());
        } else {
            time = position.time();
            if (duration != null)
                time = Math.min(duration, time);
        }
        return time;
    }

    public boolean scheduled() {
        return time != null;
    }

    public Long time() {
        return time;
    }

}