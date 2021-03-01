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
package eu.tsystems.mms.tic.testerra.plugins.selenoid.utils;

import eu.tsystems.mms.tic.testerra.plugins.selenoid.request.VideoRequest;
import eu.tsystems.mms.tic.testframework.common.Testerra;
import eu.tsystems.mms.tic.testframework.logging.Loggable;
import eu.tsystems.mms.tic.testframework.report.Report;
import eu.tsystems.mms.tic.testframework.report.TesterraListener;
import eu.tsystems.mms.tic.testframework.report.model.context.Video;
import java.io.File;

/**
 * Will load Video via Selenoid API
 * Date: 16.04.2020
 * Time: 10:38
 *
 * @author Eric Kubenka
 */
public class VideoLoader implements Loggable {

    private static final SelenoidHelper selenoidHelper = SelenoidHelper.get();
    private final Report report = Testerra.getInjector().getInstance(Report.class);

    /**
     * When Selenoid is uses, then video will be requested, downloaded and linked to report.
     *
     * @param videoRequest {@link VideoRequest}
     * @return Video
     */
    public Video download(VideoRequest videoRequest) {

        Video video = null;
        final String tempVideoFilePath = selenoidHelper.getRemoteVideoFile(videoRequest);

        if (tempVideoFilePath != null) {
            video = report.provideVideo(new File(tempVideoFilePath), Report.FileMode.MOVE);
        }
        // null or video... :)
        return video;
    }
}
