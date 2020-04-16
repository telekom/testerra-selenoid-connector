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
package eu.tsystems.mms.tic.testerra.plugins.video.utils;

import eu.tsystems.mms.tic.testerra.plugins.video.request.VideoRequest;
import eu.tsystems.mms.tic.testframework.logging.Loggable;
import eu.tsystems.mms.tic.testframework.report.model.context.Video;
import eu.tsystems.mms.tic.testframework.report.model.context.report.Report;

import java.io.File;
import java.io.IOException;

/**
 * Will load Video via Selenoid API
 * Date: 16.04.2020
 * Time: 10:38
 *
 * @author Eric Kubenka
 */
public class VideoLoader implements Loggable {

    private static final SelenoidHelper selenoidHelper = SelenoidHelper.get();

    /**
     * When Selenoid is uses, then video will be requested, downloaded and linked to report.
     *
     * @param videoRequest {@link VideoRequest}
     * @return Video
     */
    public Video download(VideoRequest videoRequest) {

        Video video = null;

        if (selenoidHelper.isSelenoidUsed(videoRequest.webDriverRequest)) {

            final String tempVideoFilePath = selenoidHelper.getRemoteVideoFile(videoRequest);

            if (tempVideoFilePath != null) {
                try {
                    video = Report.provideVideo(new File(tempVideoFilePath), Report.Mode.MOVE);
                } catch (IOException e) {
                    log().error("Error providing video to report.", e);
                }
            }
        }

        // null or video... :)
        return video;
    }
}
