/*
 * (C) Copyright T-Systems Multimedia Solutions GmbH 2020
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *     Eric Kubenka <Eric.Kubenka@t-systems.com>
 */
package eu.tsystems.mms.tic.testerra.plugins.selenoid.webdriver;

import eu.tsystems.mms.tic.testerra.plugins.selenoid.utils.SelenoidProperties;
import eu.tsystems.mms.tic.testframework.common.PropertyManager;
import eu.tsystems.mms.tic.testframework.report.utils.ExecutionContextController;
import eu.tsystems.mms.tic.testframework.utils.ReportUtils;
import eu.tsystems.mms.tic.testframework.utils.StringUtils;
import eu.tsystems.mms.tic.testframework.webdrivermanager.DesktopWebDriverRequest;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Provide the capabilities needed for Selenoid video integration
 * <p>
 * Date: 15.04.2020
 * Time: 11:07
 *
 * @author Eric Kubenka
 */
public class SelenoidCapabilityProvider {

    private static final boolean VIDEO_ACTIVE = PropertyManager.getBooleanProperty(SelenoidProperties.VIDEO_ENABLED, SelenoidProperties.Default.VIDEO_ENABLED);
    private static final boolean VNC_ACTIVE = PropertyManager.getBooleanProperty(SelenoidProperties.VNC_ENABLED, SelenoidProperties.Default.VNC_ENABLED);

    public enum Caps {
        videoName
    }

    private static final SelenoidCapabilityProvider INSTANCE = new SelenoidCapabilityProvider();

    private SelenoidCapabilityProvider() {

    }

    public static SelenoidCapabilityProvider get() {
        return INSTANCE;
    }

    /**
     * Provide all capabilities for Selenoid configuration.
     *
     * @param request {@link eu.tsystems.mms.tic.testerra.plugins.selenoid.request.VideoRequest}
     * @return Capabilities
     */
    public Capabilities provide(DesktopWebDriverRequest request) {

        final String reportName = ReportUtils.getReportName();
        final String runConfigName = ExecutionContextController.getCurrentExecutionContext().runConfig.RUNCFG;

        final DesiredCapabilities desiredCapabilities = new DesiredCapabilities();
        desiredCapabilities.setCapability("enableVNC", VNC_ACTIVE);

        desiredCapabilities.setCapability("enableVideo", VIDEO_ACTIVE);
        desiredCapabilities.setCapability("videoFrameRate", 2);
        desiredCapabilities.setCapability("videoName", createVideoName(request.getSessionKey(), reportName, runConfigName));

        final Map<String, String> map = new HashMap<>();
        map.put("ReportName", reportName);
        map.put("RunConfig", runConfigName);

        desiredCapabilities.setCapability("labels", map);

        /*
         * This is the standard way of setting the browser locale for Selenoid based sessions
         * @see https://aerokube.com/selenoid/latest/#_per_session_environment_variables_env
         */
        final Locale browserLocale = Locale.getDefault();
        final String[] localeArray = {
                "LANG=" + browserLocale + ".UTF-8",
                "LANGUAGE=" + browserLocale.getLanguage() + ":en",
                "LC_ALL=" + browserLocale + ".UTF-8"};
        desiredCapabilities.setCapability("env", localeArray);

        return desiredCapabilities;
    }

    private String createVideoName(final String sessionKey, final String reportName, final String runConfigName) {

        String videoName = reportName + runConfigName + Thread.currentThread().getId() + sessionKey + System.currentTimeMillis();
        videoName = StringUtils.removeIllegalCharacters(videoName, "[a-zA-Z0-9]", "");
        return videoName + ".mp4";
    }

}
