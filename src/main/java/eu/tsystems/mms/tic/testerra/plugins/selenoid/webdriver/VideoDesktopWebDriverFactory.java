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
import eu.tsystems.mms.tic.testframework.logging.Loggable;
import eu.tsystems.mms.tic.testframework.testing.WebDriverManagerProvider;
import eu.tsystems.mms.tic.testframework.webdrivermanager.WebDriverRequest;
import org.openqa.selenium.WebDriver;

import java.util.function.Consumer;

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
        WebDriverManagerProvider {
    private final SelenoidHelper selenoidHelper = SelenoidHelper.get();
    private final VideoRequestStorage videoRequestStorage = VideoRequestStorage.get();

    // After startup
    @Override
    public void accept(WebDriver webDriver) {
        WEB_DRIVER_MANAGER.getSessionContext(webDriver).ifPresent(sessionContext -> {
            WebDriverRequest webDriverRequest = sessionContext.getWebDriverRequest();

            sessionContext.getRemoteSessionId().ifPresent(remoteSessionId -> {
                webDriverRequest.getServerUrl().ifPresent(url -> {
                    if (selenoidHelper.updateNodeInfo(url, remoteSessionId, sessionContext)) {
                        // create a VideoRequest with request and videoName
                        final VideoRequest videoRequest = new VideoRequest(sessionContext, webDriverRequest.getCapabilities().get(SelenoidCapabilities.VIDEO_NAME).toString());

                        // store it.
                        videoRequestStorage.store(videoRequest);
                        log().info("VNC Streaming URL: " + selenoidHelper.getRemoteVncUrl(videoRequest, remoteSessionId));
                    }
                });
            });
        });
    }
}
