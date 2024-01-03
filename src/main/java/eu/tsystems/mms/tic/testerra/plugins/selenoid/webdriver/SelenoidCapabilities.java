package eu.tsystems.mms.tic.testerra.plugins.selenoid.webdriver;

/**
 * Created on 06.01.2022
 *
 * @author mgn
 */
public class SelenoidCapabilities {

    /**
     * See a full list of all Selenoid caps here
     * https://aerokube.com/selenoid/latest/#_special_capabilities
     */

    public static String SELENOID_OPTIONS = "selenoid:options";

    public static String GGR_OPTIONS = "ggr:options";

    public static String ENABLE_VNC = "enableVNC";

    public static String ENABLE_VIDEO = "enableVideo";

    public static String VIDEO_FRAME_RATE = "videoFrameRate";

    public static String VIDEO_NAME = "videoName";

    public static String SCREEN_RESOLUTION = "screenResolution";

    public static String LABELS = "labels";

    private SelenoidCapabilities() {

    }

}
