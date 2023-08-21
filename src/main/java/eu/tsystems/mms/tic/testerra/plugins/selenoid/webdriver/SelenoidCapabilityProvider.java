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
import eu.tsystems.mms.tic.testframework.common.PropertyManagerProvider;
import eu.tsystems.mms.tic.testframework.common.Testerra;
import eu.tsystems.mms.tic.testframework.logging.Loggable;
import eu.tsystems.mms.tic.testframework.report.model.context.AbstractContext;
import eu.tsystems.mms.tic.testframework.report.utils.IExecutionContextController;
import eu.tsystems.mms.tic.testframework.webdrivermanager.DesktopWebDriverRequest;
import eu.tsystems.mms.tic.testframework.webdrivermanager.WebDriverRequest;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.MutableCapabilities;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Provide the capabilities needed for Selenoid video integration
 * <p>
 * Date: 15.04.2020
 * Time: 11:07
 *
 * @author Eric Kubenka
 */
public class SelenoidCapabilityProvider implements Consumer<WebDriverRequest>, Loggable, PropertyManagerProvider {

    private final boolean VIDEO_ACTIVE = Testerra.Properties.SCREENCASTER_ACTIVE.asBool();
    private final boolean VNC_ACTIVE = PROPERTY_MANAGER.getBooleanProperty(SelenoidProperties.VNC_ENABLED, SelenoidProperties.Default.VNC_ENABLED);
    private final String VNC_ADDRESS = PROPERTY_MANAGER.getProperty(SelenoidProperties.VNC_ADDRESS, SelenoidProperties.Default.VNC_ADDRESS);

    private static final long VIDEO_FRAMERATE = PROPERTY_MANAGER.getLongProperty(SelenoidProperties.VIDEO_FRAMERATE, SelenoidProperties.Default.VIDEO_FRAMERATE);

    // Maximum framerate to prevent huge files and CPU load
    private static final long VIDEO_FRAMERATE_MAX = 15;

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
        Capabilities capabilities = desktopWebDriverRequest.getCapabilities();
        // determine everything for Selenoid... incl. video name on remote.
        final Map<String, Object> selenoidCaps = createSelenoidOptions(desktopWebDriverRequest);
        capabilities = this.mergeSelenoidCaps(capabilities, selenoidCaps);
        desktopWebDriverRequest.setCapabilities(capabilities);
    }

    private Capabilities mergeSelenoidCaps(Capabilities requestCaps, Map<String, Object> selenoidOptions) {
        Object existingOptions = requestCaps.getCapability(SelenoidCapabilities.SELENOID_OPTIONS);
        if (existingOptions instanceof Map) {
            Map<String, Object> additionalOptions = (Map<String, Object>) existingOptions;
            selenoidOptions.putAll(additionalOptions);
        }
        MutableCapabilities mutableCapabilities = new MutableCapabilities();
        mutableCapabilities.setCapability(SelenoidCapabilities.SELENOID_OPTIONS, selenoidOptions);
        return requestCaps.merge(mutableCapabilities);
    }

    /**
     * Provide all capabilities for Selenoid configuration.
     */
    private Map<String, Object> createSelenoidOptions(DesktopWebDriverRequest request) {

        IExecutionContextController contextController = Testerra.getInjector().getInstance(IExecutionContextController.class);

        final String reportName = contextController.getExecutionContext().getRunConfig().getReportName();
        final String runConfigName = contextController.getExecutionContext().getRunConfig().RUNCFG;

        // Try to find out the current testmethod to add the name to the Selenoid caps
        String methodName = contextController.getCurrentMethodContext().map(AbstractContext::getName).orElse("na.");

        final Map<String, Object> selenoidOptions = new HashMap<>();
        selenoidOptions.put(SelenoidCapabilities.ENABLE_VNC, VNC_ACTIVE);

        selenoidOptions.put(SelenoidCapabilities.ENABLE_VIDEO, VIDEO_ACTIVE);

        final long framerate = Math.max(Math.min(VIDEO_FRAMERATE, VIDEO_FRAMERATE_MAX), 1);
        selenoidOptions.put(SelenoidCapabilities.VIDEO_FRAME_RATE, framerate);

        selenoidOptions.put(SelenoidCapabilities.VIDEO_NAME, createVideoName(request.getSessionKey(), reportName, runConfigName));
        Dimension windowSize = request.getWindowSize();
        selenoidOptions.put(SelenoidCapabilities.SCREEN_RESOLUTION, String.format("%sx%sx24", windowSize.getWidth(), windowSize.getHeight()));

        final Map<String, String> map = new HashMap<>();
        map.put("ReportName", reportName);
        map.put("RunConfig", runConfigName);
        map.put("Testmethod", methodName);
        selenoidOptions.put(SelenoidCapabilities.LABELS, map);

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

        return selenoidOptions;
    }

    private String createVideoName(final String sessionKey, final String reportName, final String runConfigName) {
        String videoName = reportName + runConfigName + Thread.currentThread().getId() + sessionKey + System.currentTimeMillis();
        videoName = videoName.replaceAll("[^a-zA-Z0-9]", "");
        return videoName + ".mp4";
    }

}
