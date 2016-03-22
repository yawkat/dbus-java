package at.yawk.dbus.client;

import at.yawk.dbus.client.annotation.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Extended integration tests that use non-standard dbus interfaces.
 *
 * @author yawkat
 */
public class DbusClientExtendedIntegrationTest {
    private DbusClient client;

    @BeforeMethod
    public void setUp() throws Exception {
        client = new DbusClient();
    }

    @AfterMethod
    public void tearDown() throws Exception {
        client.close();
    }

    // this is a general listener test using spotify
    @Test(timeOut = 5000L, enabled = false)
    public void testSpotify() throws Exception {
        client.connectSession();

        Spotify itf = client.implement(Spotify.class);

        AtomicInteger changeCounter = new AtomicInteger();
        itf.onPropertiesChanged(changeCounter::incrementAndGet);

        String initial = itf.getPlaybackStatus();
        itf.playPause();
        Assert.assertNotEquals(itf.getPlaybackStatus(), initial);
        itf.playPause();
        Assert.assertEquals(itf.getPlaybackStatus(), initial);

        Assert.assertTrue(changeCounter.get() >= 2, changeCounter.toString());
    }

    // this is a listener test with many changing properties using upower
    @Test(enabled = false)
    public void testPower() throws Exception {
        client.connectSystem();

        Power itf = client.implement(Power.class);

        CountDownLatch latch = new CountDownLatch(1);
        itf.onPropertiesChanged(latch::countDown);
        latch.await();
    }

    @SessionBus
    @Destination("org.mpris.MediaPlayer2.spotify")
    @ObjectPath("/org/mpris/MediaPlayer2")
    @Interface("org.mpris.MediaPlayer2.Player")
    interface Spotify {
        @Call
        @Member("PlayPause")
        void playPause();

        @GetProperty
        @Member("PlaybackStatus")
        String getPlaybackStatus();

        @Interface("org.freedesktop.DBus.Properties")
        @Member("PropertiesChanged")
        @Listener
        void onPropertiesChanged(Runnable listener);
    }

    @SystemBus
    @Destination("org.freedesktop.UPower")
    @ObjectPath("/org/freedesktop/UPower/devices/DisplayDevice")
    @Interface("org.freedesktop.UPower.Device")
    public interface Power {
        @Interface("org.freedesktop.DBus.Properties")
        @Member("PropertiesChanged")
        @Listener
        void onPropertiesChanged(Runnable listener);
    }
}