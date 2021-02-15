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
import eu.tsystems.mms.tic.testframework.interop.VideoCollector;
import eu.tsystems.mms.tic.testframework.logging.Loggable;
import eu.tsystems.mms.tic.testframework.report.model.context.SessionContext;
import eu.tsystems.mms.tic.testframework.report.model.context.Video;
import eu.tsystems.mms.tic.testframework.utils.WebDriverUtils;
import eu.tsystems.mms.tic.testframework.webdrivermanager.WebDriverSessionsManager;
import java.util.Optional;
import java.util.function.Consumer;
import org.openqa.selenium.WebDriver;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Will collect Videos when Testerra asks for it via {@link VideoCollector#collectVideos()}
 * Date: 15.04.2020
 * Time: 11:16
 *
 * @author Eric Kubenka
 */
public class SelenoidEvidenceVideoCollector implements
        Consumer<WebDriver>,
        VideoCollector,
        Loggable
{

    private final SelenoidHelper selenoidHelper = SelenoidHelper.get();
    private final VideoRequestStorage videoRequestStorage = VideoRequestStorage.get();
    private final ThreadLocal<List<VideoRequest>> videoRequestsForClosedSessions = new ThreadLocal<>();

    /**
     * This method will be called once per failed testcase
     * And after all related {@link WebDriver} sessions where closed
     */
    @Override
    public List<Video> getVideos() {

        final List<Video> videoList = new ArrayList<>();

        // Iterate over all closed WebDriver sessions during last teardown and get their videos.
        if (videoRequestsForClosedSessions.get() != null) {
            for (final VideoRequest videoRequest : videoRequestsForClosedSessions.get()) {
                Optional<Video> optionalVideo = downloadLinkAndCleanVideo(videoRequest);
                optionalVideo.ifPresent(videoList::add);
            }
        }
        videoRequestsForClosedSessions.remove();
        return videoList;
    }

    /**
     * This method will be called for each WebDriver that is still active after a Test method
     * Will store the associated {@link VideoRequest} for downloading videos later.
     *
     * @param webDriver @{@link WebDriver}
     */
    @Override
    public void accept(WebDriver webDriver) {
        if (WebDriverSessionsManager.isExclusiveSession(webDriver)) {
            for (final VideoRequest videoRequest : videoRequestStorage.global()) {
                downloadLinkAndCleanVideo(videoRequest);
            }
        } else {
            if (videoRequestsForClosedSessions.get() == null) {
                videoRequestsForClosedSessions.set(new LinkedList<>());
            }
            WebDriverSessionsManager.getSessionContext(webDriver).ifPresent(sessionContext -> {
                if (selenoidHelper.isSelenoidUsed(sessionContext)) {
                    videoRequestStorage.list().stream()
                            .filter(videoRequest -> videoRequest.sessionContext == sessionContext)
                            .findFirst()
                            .ifPresent(videoRequest -> videoRequestsForClosedSessions.get().add(videoRequest));
                }
            });
        }
    }

    private Optional<Video> downloadLinkAndCleanVideo(final VideoRequest videoRequest) {
        Video video = new VideoLoader().download(videoRequest);

        if (video != null) {
            videoRequest.sessionContext.setVideo(video);
            selenoidHelper.deleteRemoteVideoFile(videoRequest);
        } else {
            log().warn("Unable to download video");
        }
        videoRequestStorage.remove(videoRequest);

        return Optional.ofNullable(video);
    }
}

