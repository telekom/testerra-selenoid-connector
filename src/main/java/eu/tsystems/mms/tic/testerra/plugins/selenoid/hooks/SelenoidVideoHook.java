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
package eu.tsystems.mms.tic.testerra.plugins.selenoid.hooks;

import eu.tsystems.mms.tic.testerra.plugins.selenoid.collector.SelenoidEvidenceVideoCollector;
import eu.tsystems.mms.tic.testerra.plugins.selenoid.utils.SelenoidProperties;
import eu.tsystems.mms.tic.testerra.plugins.selenoid.webdriver.VideoDesktopWebDriverFactory;
import eu.tsystems.mms.tic.testframework.common.PropertyManager;
import eu.tsystems.mms.tic.testframework.constants.Browsers;
import eu.tsystems.mms.tic.testframework.hooks.ModuleHook;
import eu.tsystems.mms.tic.testframework.logging.Loggable;
import eu.tsystems.mms.tic.testframework.webdrivermanager.WebDriverManager;
import eu.tsystems.mms.tic.testframework.webdrivermanager.WebDriverSessionsManager;

/**
 * The simple Hook for Testerras {@link ModuleHook}
 * Date: 15.04.2020
 * Time: 10:44
 *
 * @author Eric Kubenka
 */
public class SelenoidVideoHook implements ModuleHook, Loggable {

    private static final boolean VIDEO_ACTIVE = PropertyManager.getBooleanProperty(SelenoidProperties.VIDEO_ENABLED, SelenoidProperties.Default.VIDEO_ENABLED);
    private static final boolean VNC_ACTIVE = PropertyManager.getBooleanProperty(SelenoidProperties.VNC_ENABLED, SelenoidProperties.Default.VNC_ENABLED);

    private static final String[] browsers = {
            Browsers.chrome,
            Browsers.chromeHeadless,
            Browsers.firefox,
            Browsers.edge,
            Browsers.safari
    };

    @Override
    public void init() {

        // VIDEO and VNC disabled by properties. Not doing anything here.
        if (!VIDEO_ACTIVE && !VNC_ACTIVE) {
            log().info(String.format("testerra-selenoid-connector not registered. Neither %s nor %s is set to true.", SelenoidProperties.VIDEO_ENABLED, SelenoidProperties.VNC_ENABLED));
            return;
        }

        // Register a new VideoWebDriverfactory for defined browsers.
        WebDriverManager.registerWebDriverFactory(new VideoDesktopWebDriverFactory(), browsers);
        log().debug("Registered " + VideoDesktopWebDriverFactory.class.getSimpleName());

        // Adding Video Handlers
        if (VIDEO_ACTIVE) {

            SelenoidEvidenceVideoCollector selenoidEvidenceVideoCollector = new SelenoidEvidenceVideoCollector();

            // Register a shutdown handler to get informed about closing WebDriver sessions
            WebDriverSessionsManager.registerWebDriverAfterShutdownHandler(selenoidEvidenceVideoCollector);
        }
    }

    @Override
    public void terminate() {

    }
}
