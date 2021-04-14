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

import eu.tsystems.mms.tic.testerra.plugins.selenoid.request.VideoRequest;
import eu.tsystems.mms.tic.testerra.plugins.selenoid.request.VideoRequestStorage;
import eu.tsystems.mms.tic.testerra.plugins.selenoid.utils.SelenoidHelper;
import eu.tsystems.mms.tic.testerra.plugins.selenoid.utils.SelenoidProperties;
import eu.tsystems.mms.tic.testframework.common.PropertyManager;
import eu.tsystems.mms.tic.testframework.common.Testerra;
import eu.tsystems.mms.tic.testframework.constants.Browsers;
import eu.tsystems.mms.tic.testframework.logging.Loggable;
import eu.tsystems.mms.tic.testframework.report.model.context.SessionContext;
import eu.tsystems.mms.tic.testframework.webdrivermanager.DesktopWebDriverRequest;
import eu.tsystems.mms.tic.testframework.webdrivermanager.IWebDriverManager;
import eu.tsystems.mms.tic.testframework.webdrivermanager.WebDriverRequest;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

/**
 * Extends DesktopWebDriverFactory by using selenoid video capabilities.
 * <p>
 * Date: 15.04.2020
 * Time: 11:49
 *
 * @author Eric Kubenka
 */
public class VideoDesktopWebDriverFactory implements
        Loggable,
        Consumer<WebDriver>,
        BiConsumer<WebDriverRequest, SessionContext>
{

    private static final boolean VNC_ACTIVE = PropertyManager.getBooleanProperty(SelenoidProperties.VNC_ENABLED, SelenoidProperties.Default.VNC_ENABLED);
    private static final String VNC_ADDRESS = PropertyManager.getProperty(SelenoidProperties.VNC_ADDRESS, SelenoidProperties.Default.VNC_ADDRESS);

    private final SelenoidCapabilityProvider selenoidCapabilityProvider = SelenoidCapabilityProvider.get();
    private final SelenoidHelper selenoidHelper = SelenoidHelper.get();

    private final VideoRequestStorage videoRequestStorage = VideoRequestStorage.get();

    @Override
    public void accept(WebDriver webDriver) {
        IWebDriverManager webDriverManager = Testerra.getInjector().getInstance(IWebDriverManager.class);
        webDriverManager.getSessionContext(webDriver).ifPresent(sessionContext -> {
    if (rawWebDriver instanceof RemoteWebDriver && request.getSeleniumServerUrl() != null) {
            sessionContext.setNodeInfo(SelenoidHelper.get().getNodeInfo(request.getSeleniumServerUrl(), ((RemoteWebDriver) rawWebDriver).getSessionId().toString()));
        }        // create a VideoRequest with request and videoName
            final VideoRequest videoRequest = new VideoRequest(sessionContext, sessionContext.getWebDriverRequest().getCapabilities().get(SelenoidCapabilityProvider.Caps.videoName.toString()).toString());

            // store it.
            videoRequestStorage.store(videoRequest);
            log().info("VNC Streaming URL: " + selenoidHelper.getRemoteVncUrl(videoRequest));
        });
    }

    @Override
    public void accept(WebDriverRequest webDriverRequest, SessionContext sessionContext) {

        // Only accept webdrivers for desktop
        if (!(webDriverRequest instanceof DesktopWebDriverRequest)) {
            return;
        }

        if (VNC_ACTIVE && StringUtils.isBlank(VNC_ADDRESS)) {
            log().warn(String.format("%s is set to true, but vnc host property %s was not set.", SelenoidProperties.VNC_ENABLED, SelenoidProperties.VNC_ADDRESS));
        }

        DesktopWebDriverRequest desktopWebDriverRequest = (DesktopWebDriverRequest)webDriverRequest;
        // determine everything for selenoid... incl. video name on remote.
        final Capabilities videoCaps = selenoidCapabilityProvider.provide(desktopWebDriverRequest);
        desktopWebDriverRequest.getDesiredCapabilities().merge(videoCaps);
    }
}
