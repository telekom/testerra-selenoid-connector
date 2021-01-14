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
package eu.tsystems.mms.tic.testerra.plugins.selenoid.request;

import eu.tsystems.mms.tic.testframework.logging.Loggable;
import eu.tsystems.mms.tic.testframework.webdrivermanager.DesktopWebDriverRequest;
import eu.tsystems.mms.tic.testframework.webdrivermanager.WebDriverRequest;

/**
 * Wraps DesktopWebDriverRequest by adding Video fields
 * <p>
 * Date: 15.04.2020
 * Time: 11:44
 *
 * @author Eric Kubenka
 */
public class VideoRequest implements Loggable {

    public VideoRequest(WebDriverRequest webDriverRequest, String videoName) {
        this.webDriverRequest = webDriverRequest;
        this.selenoidVideoName = videoName;
    }

    public final WebDriverRequest webDriverRequest;
    public final String selenoidVideoName;
}
