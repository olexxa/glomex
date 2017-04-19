package de.glomex.player.model.lifecycle;

import com.google.inject.Inject;
import de.glomex.player.api.lifecycle.AdData;
import de.glomex.player.api.lifecycle.LifecycleListener;
import de.glomex.player.api.lifecycle.MediaData;
import de.glomex.player.api.playlist.MediaID;
import de.glomex.player.model.api.EtcController;
import de.glomex.player.model.api.ExecutionManager;
import de.glomex.player.model.api.GlomexPlayerFactory;
import de.glomex.player.model.api.Logging;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.logging.Logger;

/**
 * Contract for this class is:
 *   - being given a media ID, create lifecycle object
 *   - populate it with data obtained from resolvers, asynchronously and in protected way
 *   - hide synchronization
 *
 * Created by <b>me@olexxa.com</b>
 */
public class LifecycleFetcher {

    private static final Logger log = Logging.getLogger(LifecycleFetcher.class);

    private final @NotNull EtcController etcController;
    private final @NotNull LifecycleListener lifecycleListener;

    private @Nullable Consumer<Lifecycle> callback;

    private final @NotNull CountDownLatch latch = new CountDownLatch(2);
    private final @NotNull Future<MediaData> mediaFuture;
    private final @NotNull Future<List<AdData>> adsFuture;

    private final @NotNull Lifecycle lifecycle;

    public LifecycleFetcher(@NotNull MediaID mediaID, @Nullable Consumer<Lifecycle> callback) {
        this.etcController = GlomexPlayerFactory.instance(EtcController.class);
        this.lifecycleListener = GlomexPlayerFactory.instance(LifecycleListener.class);

        this.callback = callback;

        lifecycle = new Lifecycle(mediaID);

        ExecutionManager executor = GlomexPlayerFactory.instance(ExecutionManager.class);
        mediaFuture = executor.submit(this::fetchMedia);
        adsFuture = executor.submit(this::fetchAds);
    }

    private @Nullable MediaData fetchMedia() {
        MediaData mediaData = null;
        try {
            mediaData = etcController.mediaResolver().resolve(lifecycle.mediaID);
            lifecycle.media(mediaData);
            lifecycleListener.onMediaResolved(lifecycle.mediaID);
        } catch (RuntimeException error) {
            log.severe("Error getting media " + lifecycle.mediaID + ": " + error.getMessage());
            lifecycleListener.onMediaError(lifecycle.mediaID);
            // N.B. cancel add loading if still running
            adsFuture.cancel(true);
            latch.countDown();
        } finally {
            latchDown();
        }
        return mediaData;
    }

    private @Nullable List<AdData> fetchAds() {
        List<AdData> ads = null;
        try {
            ads = etcController.adResolver().resolve(lifecycle.mediaID);
            lifecycle.ads(ads);
            lifecycleListener.onAdsResolved(lifecycle.mediaID);
        } catch (RuntimeException error) {
            log.warning("Error getting ads " + lifecycle.mediaID + ": " + error.getMessage());
            log.warning("Skipping ads");
            lifecycleListener.onAdError(lifecycle.mediaID);
        } finally {
            latchDown();
        }
        return ads;
    }

    private void latchDown() {
        latch.countDown();
        if (callback != null && latch.getCount() == 0) {
            callback.accept(lifecycle);
            shutdown();
        }
    }

    // block current thread
    public Lifecycle lifecycle() {
        try {
            latch.await();
        } catch (InterruptedException e) {
            log.severe("Interrupted: " + e.getMessage());
        }
        return lifecycle;
    }

    public void shutdown() {
        mediaFuture.cancel(true);
        adsFuture.cancel(true);
        callback = null;
        // improve: future with interrupted seems more logical...
        latch.countDown();
        latch.countDown();
    }


//    public Lifecycle lifecycle() {
//        return lifecycle.ready()? lifecycle : obtainLifecycle();
//    }

//    private Lifecycle obtainLifecycle() {
//        try {
//            lifecycle.media = mediaFuture.get();
//            lifecycleListener.onMediaResolved(lifecycle.mediaID);
//        } catch (InterruptedException interrupted) {
//            log.fine("Getting media interrupted " + lifecycle.mediaID);
//            cancelAdsFetch();
//            return lifecycle;
//        } catch (ExecutionException error) {
//            log.severe("Error getting media " + lifecycle.mediaID + ": " + error.getCause().getMessage());
//            cancelAdsFetch();
//            return lifecycle;
//        } finally {
//            mediaFuture = null;
//        }
//
//        check for ads
//        try {
//            lifecycle.ads(adsFuture.get());
//            lifecycleListener.onAdsResolved(lifecycle.mediaID);
//        } catch (InterruptedException interrupted) {
//            log.fine("Getting ads interrupted " + lifecycle.mediaID);
//        } catch (ExecutionException error) {
//            log.warning("Error getting ads " + lifecycle.mediaID + ": " + error.getCause().getMessage());
//            log.warning("Skipping ads");
//        } finally {
//            adsFuture = null;
//        }
//
//        lifecycle.resolve();
//        log.finest(Arrays.toString(lifecycle.stops().toArray()));
//
//        return lifecycle;
//    }

}
