package eu.tsystems.mms.tic.testerra.plugins.video.webdriver;

import eu.tsystems.mms.tic.testframework.report.utils.ExecutionContextController;
import eu.tsystems.mms.tic.testframework.report.utils.ReportUtils;
import eu.tsystems.mms.tic.testframework.utils.StringUtils;
import eu.tsystems.mms.tic.testframework.webdrivermanager.DesktopWebDriverRequest;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.util.HashMap;
import java.util.Map;

/**
 * Provide the capabilities needed for TAP1 integration.
 * <p>
 * Date: 15.04.2020
 * Time: 11:07
 *
 * @author Eric Kubenka
 */
public class SelenoidCapabilityProvider {

    public enum Caps {
        videoName
    }

    private static final SelenoidCapabilityProvider INSTANCE = new SelenoidCapabilityProvider();

    private SelenoidCapabilityProvider() {

    }

    public static SelenoidCapabilityProvider get() {
        return INSTANCE;
    }

    public Capabilities provide(DesktopWebDriverRequest request) {

        final String reportName = ReportUtils.getReportName();
        final String runConfigName = ExecutionContextController.EXECUTION_CONTEXT.runConfig.RUNCFG;

        final DesiredCapabilities desiredCapabilities = new DesiredCapabilities();
        desiredCapabilities.setCapability("enableVNC", true);

        desiredCapabilities.setCapability("enableVideo", true);
        desiredCapabilities.setCapability("videoFrameRate", 2);
        desiredCapabilities.setCapability("videoName", createVideoName(request.sessionKey, reportName, runConfigName));

        final Map<String, String> map = new HashMap<>();
        map.put("ReportName", reportName);
        map.put("RunConfig", runConfigName);

        desiredCapabilities.setCapability("labels", map);

        return desiredCapabilities;
    }

    private String createVideoName(final String sessionKey, final String reportName, final String runConfigName) {

        String videoName = reportName + runConfigName + Thread.currentThread().getId() + sessionKey + System.currentTimeMillis();
        videoName = StringUtils.removeIllegalCharacters(videoName, "[a-zA-Z0-9]", "");
        return videoName + ".mp4";
    }

}
