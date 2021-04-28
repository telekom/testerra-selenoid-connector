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
package eu.tsystems.mms.tic.testerra.plugins.selenoid.collector;

import eu.tsystems.mms.tic.testerra.plugins.selenoid.request.VideoRequest;
import eu.tsystems.mms.tic.testerra.plugins.selenoid.request.VideoRequestStorage;
import eu.tsystems.mms.tic.testerra.plugins.selenoid.utils.SelenoidHelper;
import eu.tsystems.mms.tic.testerra.plugins.selenoid.utils.VideoLoader;
import eu.tsystems.mms.tic.testframework.common.PropertyManager;
import eu.tsystems.mms.tic.testframework.constants.TesterraProperties;
import eu.tsystems.mms.tic.testframework.logging.Loggable;
import eu.tsystems.mms.tic.testframework.report.TestStatusController;
import eu.tsystems.mms.tic.testframework.report.model.context.MethodContext;
import eu.tsystems.mms.tic.testframework.report.model.context.SessionContext;
import eu.tsystems.mms.tic.testframework.report.model.context.Video;
import eu.tsystems.mms.tic.testframework.report.utils.ExecutionContextController;
import eu.tsystems.mms.tic.testframework.webdrivermanager.WebDriverSessionsManager;
import java.util.Optional;
import java.util.function.Consumer;
import org.openqa.selenium.WebDriver;

/**
 * Will collect Videos when Testerra closes a WebDriver session
 * Date: 15.04.2020
 * Time: 11:16
 *
 * @author Eric Kubenka
 */
public class SelenoidEvidenceVideoCollector implements
        Consumer<WebDriver>,
        Loggable
{

    private final SelenoidHelper selenoidHelper = SelenoidHelper.get();
    private final VideoRequestStorage videoRequestStorage = VideoRequestStorage.get();
    private final boolean SCREENCASTER_ACTIVE_ON_SUCCESS = PropertyManager.getBooleanProperty(TesterraProperties.SCREENCASTER_ACTIVE_ON_SUCCESS, false);
    private final boolean SCREENCASTER_ACTIVE_ON_FAILED = PropertyManager.getBooleanProperty(TesterraProperties.SCREENCASTER_ACTIVE_ON_FAILED, true);

    /**
     * This method will be called for each WebDriver that is still active after a Test method
     * Will store the associated {@link VideoRequest} for downloading videos later.
     *
     * @param webDriver @{@link WebDriver}
     */
    @Override
    public void accept(WebDriver webDriver) {

        MethodContext currentMethodContext = ExecutionContextController.getCurrentMethodContext();
        if (currentMethodContext != null) {
            currentMethodContext.getTestNgResult().ifPresent(testResult -> {
                /**
                 * Don't handle session when there is no video
                 */
                if (testResult.getMethod().isTest()) {
                    if (currentMethodContext.isFailed() && SCREENCASTER_ACTIVE_ON_FAILED) {
                        collectVideoForWebDriver(webDriver);
                    } else if (currentMethodContext.isPassed() && SCREENCASTER_ACTIVE_ON_SUCCESS) {
                        collectVideoForWebDriver(webDriver);

                    } else if (currentMethodContext.isSkipped()) {
                        if (currentMethodContext.getStatus() == TestStatusController.Status.FAILED_RETRIED) {
                            collectVideoForWebDriver(webDriver);
                        }
                    }
                }
            });
        } else if (WebDriverSessionsManager.isExclusiveSession(webDriver)) {
            collectVideoForWebDriver(webDriver);
        }
    }

    /**
     * Downloads the video and add its to the {@link SessionContext}
     */
    private Optional<Video> downloadLinkAndCleanVideo(final VideoRequest videoRequest) {
        Video video = new VideoLoader().download(videoRequest);

        if (video != null) {
            videoRequest.sessionContext.setVideo(video);
            selenoidHelper.deleteRemoteVideoFile(videoRequest);
        } else {
            log().warn("Unable to download video");
        }

        return Optional.ofNullable(video);
    }

    protected void collectVideoForWebDriver(WebDriver webDriver) {
        // Check if session exists in general
        WebDriverSessionsManager.getSessionContext(webDriver).ifPresent(closedSessionContext -> {
            // Check is session context is a selenoid session
            if (selenoidHelper.isSelenoidUsed(closedSessionContext)) {
                // Check if there exists a video request for this session
                videoRequestStorage.list().stream()
                        .filter(videoRequest -> videoRequest.sessionContext == closedSessionContext)
                        .findFirst()
                        .ifPresent(videoRequest -> {
                            this.downloadLinkAndCleanVideo(videoRequest);
                            videoRequestStorage.remove(videoRequest);
                        });
            }
        });
    }
}

