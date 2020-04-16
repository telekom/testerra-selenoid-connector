package eu.tsystems.mms.tic.testerra.plugins.video.utils;

import eu.tsystems.mms.tic.testframework.constants.TesterraProperties;

/**
 * Selenoid Connector Properties
 * <p>
 * Date: 16.04.2020
 * Time: 11:12
 *
 * @author Eric Kubenka
 */
public class SelenoidProperties {

    public static final String VNC_ENABLED = "tt.selenoid.vnc.enabled";
    public static final String VNC_ADDRESS = "tt.selenoid.vnc.address";
    public static final String VIDEO_ENABLED = TesterraProperties.SCREENCASTER_ACTIVE;

    public static class Default {

        public static final boolean VNC_ENABLED = true;
        public static final String VNC_ADDRESS = null;
        public static final boolean VIDEO_ENABLED = true;
    }
}
