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

import com.google.common.eventbus.Subscribe;
import eu.tsystems.mms.tic.testerra.plugins.selenoid.request.VideoRequest;
import eu.tsystems.mms.tic.testerra.plugins.selenoid.request.VideoRequestStorage;
import eu.tsystems.mms.tic.testerra.plugins.selenoid.utils.SelenoidHelper;
import eu.tsystems.mms.tic.testerra.plugins.selenoid.utils.VideoLoader;
import eu.tsystems.mms.tic.testframework.events.MethodEndEvent;
import eu.tsystems.mms.tic.testframework.execution.testng.worker.MethodWorker;
import eu.tsystems.mms.tic.testframework.execution.worker.finish.AbstractEvidencesWorker;
import eu.tsystems.mms.tic.testframework.execution.worker.finish.WebDriverSessionHandler;
import eu.tsystems.mms.tic.testframework.interop.VideoCollector;
import eu.tsystems.mms.tic.testframework.logging.Loggable;
import eu.tsystems.mms.tic.testframework.report.model.context.Video;
import eu.tsystems.mms.tic.testframework.webdrivermanager.DesktopWebDriverRequest;
import eu.tsystems.mms.tic.testframework.webdrivermanager.WebDriverManager;
import eu.tsystems.mms.tic.testframework.webdrivermanager.WebDriverRequest;
import eu.tsystems.mms.tic.testframework.webdrivermanager.desktop.WebDriverMode;
import org.openqa.selenium.WebDriver;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Will collect Videos when Testerra asks for it via {@link AbstractEvidencesWorker#run()}
 * Date: 15.04.2020
 * Time: 11:16
 *
 * @author Eric Kubenka
 */
public class SelenoidEvidenceVideoCollector extends MethodWorker implements
        WebDriverSessionHandler,
        VideoCollector,
        Loggable,
        MethodEndEvent.Listener
{

    private final SelenoidHelper selenoidHelper = SelenoidHelper.get();
    private final VideoRequestStorage videoRequestStorage = VideoRequestStorage.get();
    private static final ThreadLocal<List<VideoRequest>> closedWebDriverSessions = new ThreadLocal<>();

    /**
     * This method will be called once per failed testcase by {@link eu.tsystems.mms.tic.testframework.execution.worker.finish.AbstractEvidencesWorker}
     * And after all related {@link WebDriver} sessions where closed
     */
    @Override
    public List<Video> getVideos() {

        final List<Video> videoList = new ArrayList<>();

        // Iterate over all closed WebDriver sessions during last teardown and get their videos.
        if (closedWebDriverSessions.get() != null) {
            for (final VideoRequest videoRequest : closedWebDriverSessions.get()) {
                final Video video = new VideoLoader().download(videoRequest);
                if (video != null) {
                    videoList.add(video);
                }
            }
        }

        return videoList;
    }

    /**
     * Remove all closed video requests.
     * Runs after {@link #getVideos()}
     */
    @Override
    @Subscribe
    public void onMethodEnd(MethodEndEvent event) {
        if (closedWebDriverSessions.get() != null) {

            // delete every video on remote if not already done.
            for (VideoRequest videoRequest : closedWebDriverSessions.get()) {
                selenoidHelper.deleteRemoteVideoFile(videoRequest);
            }
            videoRequestStorage.remove(closedWebDriverSessions.get());
        }

        closedWebDriverSessions.remove();
    }

    /**
     * This method will be called for each WebDriver that is still active after a Test method
     * Will store the associated {@link VideoRequest} for downloading videos later.
     *
     * @param webDriver @{@link WebDriver}
     */
    @Override
    public void run(WebDriver webDriver) {

        if (closedWebDriverSessions.get() == null) {
            closedWebDriverSessions.set(new LinkedList<>());
        }

        final WebDriverRequest relatedWebDriverRequest = WebDriverManager.getRelatedWebDriverRequest(webDriver);

        if (relatedWebDriverRequest instanceof DesktopWebDriverRequest) {
            final DesktopWebDriverRequest r = (DesktopWebDriverRequest) relatedWebDriverRequest;
            if (r.webDriverMode == WebDriverMode.remote && selenoidHelper.isSelenoidUsed(r)) {
                for (final VideoRequest videoRequest : videoRequestStorage.list()) {
                    if (videoRequest.webDriverRequest == r) {
                        closedWebDriverSessions.get().add(videoRequest);
                    }
                }
            }
        }
    }
}

