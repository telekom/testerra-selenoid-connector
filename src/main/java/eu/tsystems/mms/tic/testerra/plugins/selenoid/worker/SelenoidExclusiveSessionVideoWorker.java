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
package eu.tsystems.mms.tic.testerra.plugins.selenoid.worker;

import eu.tsystems.mms.tic.testerra.plugins.selenoid.request.VideoRequest;
import eu.tsystems.mms.tic.testerra.plugins.selenoid.request.VideoRequestStorage;
import eu.tsystems.mms.tic.testerra.plugins.selenoid.utils.SelenoidHelper;
import eu.tsystems.mms.tic.testerra.plugins.selenoid.utils.SelenoidProperties;
import eu.tsystems.mms.tic.testerra.plugins.selenoid.utils.VideoLoader;
import eu.tsystems.mms.tic.testframework.common.PropertyManager;
import eu.tsystems.mms.tic.testframework.execution.testng.worker.GenerateReportsWorker;
import eu.tsystems.mms.tic.testframework.logging.Loggable;
import eu.tsystems.mms.tic.testframework.report.model.context.ClassContext;
import eu.tsystems.mms.tic.testframework.report.model.context.MethodContext;
import eu.tsystems.mms.tic.testframework.report.model.context.SessionContext;
import eu.tsystems.mms.tic.testframework.report.model.context.SuiteContext;
import eu.tsystems.mms.tic.testframework.report.model.context.TestContextModel;
import eu.tsystems.mms.tic.testframework.report.model.context.Video;
import eu.tsystems.mms.tic.testframework.report.utils.ExecutionContextController;

/**
 * Will run with {@link GenerateReportsWorker} of Testerra to download videos that where not fetched yet, e.g Exclusive Session.
 * Will link these videos back to the original method contexts.
 * Date: 15.04.2020
 * Time: 11:16
 *
 * @author Eric Kubenka
 */
public class SelenoidExclusiveSessionVideoWorker extends GenerateReportsWorker implements Loggable {

    private static final boolean VIDEO_ACTIVE = PropertyManager.getBooleanProperty(SelenoidProperties.VIDEO_ENABLED, SelenoidProperties.Default.VIDEO_ENABLED);

    private final VideoRequestStorage videoRequestStorage = VideoRequestStorage.get();

    /**
     * Will run directly before Testerra report generation.
     * Last point to link videos to method contexts.
     */
    @Override
    public void run() {

        if (VIDEO_ACTIVE) {
            for (final VideoRequest videoRequest : videoRequestStorage.global()) {

                final Video video = new VideoLoader().download(videoRequest);
                if (video != null) {
                    linkVideoToMethodContext(videoRequest, video);
                }
                SelenoidHelper.get().deleteRemoteVideoFile(videoRequest);
            }
        }


        videoRequestStorage.clear();
    }

    /**
     * Will search for {@link MethodContext} for givven {@link SessionContext} in {@link VideoRequest} and link {@link Video} to it.
     *
     * @param videoRequest {@link VideoRequest}
     * @param video        {@link Video}
     */
    private void linkVideoToMethodContext(final VideoRequest videoRequest, final Video video) {

        // session context of video.
        final SessionContext sessionContext = videoRequest.webDriverRequest.sessionContext;

        for (SuiteContext suiteContext : ExecutionContextController.EXECUTION_CONTEXT.suiteContexts) {
            for (TestContextModel testContextModel : suiteContext.testContextModels) {
                for (ClassContext classContext : testContextModel.classContexts) {
                    for (MethodContext methodContext : classContext.methodContexts) {
                        if (methodContext.sessionContexts.contains(sessionContext)) {
                            if (!methodContext.videos.contains(video)) {
                                video.errorContextId = methodContext.id;
                                methodContext.videos.add(video);
                            }
                        }
                    }
                }
            }
        }
    }
}

