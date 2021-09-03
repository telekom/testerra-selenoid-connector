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
import eu.tsystems.mms.tic.testframework.report.model.context.ExecutionContext;
import eu.tsystems.mms.tic.testframework.report.model.context.SessionContext;
import eu.tsystems.mms.tic.testframework.report.model.context.Video;
import eu.tsystems.mms.tic.testframework.webdrivermanager.WebDriverSessionsManager;
import java.util.Optional;
import java.util.function.Consumer;
import org.openqa.selenium.WebDriver;
import org.testng.ITestResult;

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
        WebDriverSessionsManager.getSessionContext(webDriver).ifPresent(sessionContext -> {
            // Exklusive session
            if (sessionContext.getParentContext() instanceof ExecutionContext) {
                collectVideoForSessionContext(sessionContext);
            } else {
                // Check if any of the methods allows grabbing videos
                if (sessionContext.readMethodContexts().anyMatch(methodContext -> {
                    if (methodContext.getTestNgResult().isPresent()) {
                        ITestResult testResult = methodContext.getTestNgResult().get();
                        if (!testResult.isSuccess() && SCREENCASTER_ACTIVE_ON_FAILED) {
                            return true;
                        } else if (SCREENCASTER_ACTIVE_ON_SUCCESS) {
                            return true;

                        } else if (testResult.getStatus() == ITestResult.SKIP) {
                            if (methodContext.getStatus() == TestStatusController.Status.FAILED_RETRIED) {
                                return true;
                            }
                        }
                    }
                    return false;
                })) {
                    collectVideoForSessionContext(sessionContext);
                };
            }
        });
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

    protected void collectVideoForSessionContext(SessionContext sessionContext) {
        // Check if there exists a video request for this session
        videoRequestStorage.list().stream()
                .filter(videoRequest -> videoRequest.sessionContext == sessionContext)
                .findFirst()
                .ifPresent(videoRequest -> {
                    this.downloadLinkAndCleanVideo(videoRequest);
                    videoRequestStorage.remove(videoRequest);
                });
    }
}

