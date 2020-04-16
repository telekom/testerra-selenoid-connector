
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
package eu.tsystems.mms.tic.testerra.plugins.video.request;

import eu.tsystems.mms.tic.testframework.logging.Loggable;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Stores {@link VideoRequest} and associated {@link eu.tsystems.mms.tic.testframework.webdrivermanager.WebDriverRequest}
 * <p>
 * Date: 15.04.2020
 * Time: 11:30
 *
 * @author Eric Kubenka
 */
public class VideoRequestStorage implements Loggable {

    private final static VideoRequestStorage INSTANCE = new VideoRequestStorage();

    private static final ThreadLocal<List<VideoRequest>> VIDEO_WEBDRIVER_REQUESTS = new ThreadLocal<>();
    private static final List<VideoRequest> GLOBAL_VIDEO_WEBDRIVER_REQUESTS = Collections.synchronizedList(new LinkedList<>());

    private VideoRequestStorage() {

    }

    public static VideoRequestStorage get() {
        return INSTANCE;
    }

    /**
     * Returns thread-local list of current valid {@link VideoRequest}
     *
     * @return List
     */
    public List<VideoRequest> list() {
        return VIDEO_WEBDRIVER_REQUESTS.get();
    }

    /**
     * Returns global list of current valid {@link VideoRequest}
     *
     * @return List
     */
    public List<VideoRequest> global() {
        return GLOBAL_VIDEO_WEBDRIVER_REQUESTS;
    }

    /**
     * Stores a {@link VideoRequest} in thread local and global list.
     *
     * @param request {@link VideoRequest}
     */
    public void store(VideoRequest request) {

        // creating storage when not created yet.
        if (VIDEO_WEBDRIVER_REQUESTS.get() == null) {
            VIDEO_WEBDRIVER_REQUESTS.set(new LinkedList<>());
        }

        // adding
        VIDEO_WEBDRIVER_REQUESTS.get().add(request);
        GLOBAL_VIDEO_WEBDRIVER_REQUESTS.add(request);
    }

    /**
     * Removes {@link VideoRequest} from storage. Called when video grabbing is done.
     *
     * @param request {@link VideoRequest}
     */
    public void remove(VideoRequest request) {

        if (VIDEO_WEBDRIVER_REQUESTS.get() != null) {
            VIDEO_WEBDRIVER_REQUESTS.get().remove(request);
        }

        GLOBAL_VIDEO_WEBDRIVER_REQUESTS.remove(request);
    }


    /**
     * Removes a list of {@link VideoRequest} from storage. Called when video grabbing is done.
     *
     * @param requests {@link List} of {@link VideoRequest}
     */
    public void remove(List<VideoRequest> requests) {

        if (VIDEO_WEBDRIVER_REQUESTS.get() != null) {
            VIDEO_WEBDRIVER_REQUESTS.get().removeAll(requests);
        }

        GLOBAL_VIDEO_WEBDRIVER_REQUESTS.removeAll(requests);
    }

    /**
     * Clears ALL Thread-Local and GLOBAL DATA.
     */
    public void clear() {

        if (VIDEO_WEBDRIVER_REQUESTS.get() != null) {
            VIDEO_WEBDRIVER_REQUESTS.get().clear();
        }

        GLOBAL_VIDEO_WEBDRIVER_REQUESTS.clear();
    }

}
