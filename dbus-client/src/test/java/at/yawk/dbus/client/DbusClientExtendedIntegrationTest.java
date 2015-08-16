package at.yawk.dbus.client;

import at.yawk.dbus.client.annotation.*;
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

    @Test(timeOut = 5000L, enabled = true)
    public void test() throws Exception {
        client.connectSession();

        TestItf itf = client.implement(TestItf.class);

        AtomicInteger changeCounter = new AtomicInteger();
        itf.onPropertiesChanged(changeCounter::incrementAndGet);

        String initial = itf.getPlaybackStatus();
        itf.playPause();
        Assert.assertNotEquals(itf.getPlaybackStatus(), initial);
        itf.playPause();
        Assert.assertEquals(itf.getPlaybackStatus(), initial);

        Assert.assertTrue(changeCounter.get() >= 2, changeCounter.toString());
    }

    @SessionBus
    @Destination("org.mpris.MediaPlayer2.spotify")
    @ObjectPath("/org/mpris/MediaPlayer2")
    @Interface("org.mpris.MediaPlayer2.Player")
    interface TestItf {
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
}