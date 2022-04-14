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
import eu.tsystems.mms.tic.testframework.common.Testerra;
import eu.tsystems.mms.tic.testframework.logging.Loggable;
import eu.tsystems.mms.tic.testframework.report.utils.DefaultExecutionContextController;
import eu.tsystems.mms.tic.testframework.report.utils.ExecutionContextUtils;
import eu.tsystems.mms.tic.testframework.report.utils.IExecutionContextController;
import eu.tsystems.mms.tic.testframework.webdrivermanager.DesktopWebDriverRequest;
import eu.tsystems.mms.tic.testframework.webdrivermanager.WebDriverRequest;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Provide the capabilities needed for Selenoid video integration
 * <p>
 * Date: 15.04.2020
 * Time: 11:07
 *
 * @author Eric Kubenka
 */
public class SelenoidCapabilityProvider implements Consumer<WebDriverRequest>, Loggable {

    private final boolean VIDEO_ACTIVE = Testerra.Properties.SCREENCASTER_ACTIVE.asBool();
    private final boolean VNC_ACTIVE = PropertyManager.getBooleanProperty(SelenoidProperties.VNC_ENABLED, SelenoidProperties.Default.VNC_ENABLED);
    private final String VNC_ADDRESS = PropertyManager.getProperty(SelenoidProperties.VNC_ADDRESS, SelenoidProperties.Default.VNC_ADDRESS);

    private static final int VIDEO_FRAMERATE = PropertyManager.getIntProperty(SelenoidProperties.VIDEO_FRAMERATE, SelenoidProperties.Default.VIDEO_FRAMERATE);

    // Maximum framerate to prevent huge files and CPU load
    private static final int VIDEO_FRAMERATE_MAX = 15;

    @Override
    public void accept(WebDriverRequest webDriverRequest) {
        // Only accept webdrivers for desktop
        if (!(webDriverRequest instanceof DesktopWebDriverRequest)) {
            return;
        }

        if (VNC_ACTIVE && org.apache.commons.lang3.StringUtils.isBlank(VNC_ADDRESS)) {
            log().warn(String.format("%s is set to true, but vnc host property %s was not set.", SelenoidProperties.VNC_ENABLED, SelenoidProperties.VNC_ADDRESS));
        }

        DesktopWebDriverRequest desktopWebDriverRequest = (DesktopWebDriverRequest) webDriverRequest;
        // determine everything for selenoid... incl. video name on remote.
        final Capabilities videoCaps = provide(desktopWebDriverRequest);
        desktopWebDriverRequest.getDesiredCapabilities().merge(videoCaps);
    }

    /**
     * Provide all capabilities for Selenoid configuration.
     *
     * @param request {@link eu.tsystems.mms.tic.testerra.plugins.selenoid.request.VideoRequest}
     * @return Capabilities
     */
    private Capabilities provide(DesktopWebDriverRequest request) {

        IExecutionContextController contextController = new DefaultExecutionContextController();

        final String reportName = contextController.getExecutionContext().getRunConfig().getReportName();
        final String runConfigName = contextController.getExecutionContext().getRunConfig().RUNCFG;

        // Try to find out the current testmethod to add the name to the Selenoid caps
        String methodName = "";
        final Optional<Method> optional = ExecutionContextUtils.getInjectedMethod(contextController.getCurrentTestResult().get());
        methodName = optional.map(Method::getName).orElseGet(() -> contextController.getCurrentMethodContext().get().getName());

        final DesiredCapabilities desiredCapabilities = new DesiredCapabilities();
        desiredCapabilities.setCapability(SelenoidCapabilities.ENABLE_VNC, VNC_ACTIVE);

        desiredCapabilities.setCapability(SelenoidCapabilities.ENABLE_VIDEO, VIDEO_ACTIVE);

        final int framerate = Math.max(Math.min(VIDEO_FRAMERATE, VIDEO_FRAMERATE_MAX), 1);
        desiredCapabilities.setCapability(SelenoidCapabilities.VIDEO_FRAME_RATE, framerate);

        desiredCapabilities.setCapability(SelenoidCapabilities.VIDEO_NAME, createVideoName(request.getSessionKey(), reportName, runConfigName));
        Dimension windowSize = request.getWindowSize();
        desiredCapabilities.setCapability(SelenoidCapabilities.SCREEN_RESOLUTION, String.format("%sx%sx24", windowSize.getWidth(), windowSize.getHeight()));

        final Map<String, String> map = new HashMap<>();
        map.put("ReportName", reportName);
        map.put("RunConfig", runConfigName);
        map.put("Testmethod", methodName);

        desiredCapabilities.setCapability(SelenoidCapabilities.LABELS, map);

        /*
         * DEPRECATED: This part of code was removed, because it is not valid for all selenoid images.
         * e.g.: firefox was not able to start sessions when this env settings were provided
         *
         * This is the standard way of setting the browser locale for Selenoid based sessions
         * @see https://aerokube.com/selenoid/latest/#_per_session_environment_variables_env
         */
        //        final Locale browserLocale = Locale.getDefault();
        //        final String[] localeArray = {
        //                "LANG=" + browserLocale + ".UTF-8",
        //                "LANGUAGE=" + browserLocale.getLanguage() + ":en",
        //                "LC_ALL=" + browserLocale + ".UTF-8"};
        //        desiredCapabilities.setCapability("env", localeArray);

        return desiredCapabilities;
    }

    private String createVideoName(final String sessionKey, final String reportName, final String runConfigName) {
        String videoName = reportName + runConfigName + Thread.currentThread().getId() + sessionKey + System.currentTimeMillis();
        videoName = videoName.replaceAll("[^a-zA-Z0-9]", "");
        return videoName + ".mp4";
    }

}
