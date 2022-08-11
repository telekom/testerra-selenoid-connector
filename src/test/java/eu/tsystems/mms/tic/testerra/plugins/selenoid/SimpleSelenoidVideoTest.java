/*
 * Testerra
 *
 * (C) 2020, Eric Kubenka, T-Systems Multimedia Solutions GmbH, Deutsche Telekom AG
 *
 * Deutsche Telekom AG and all other contributors /
 * copyright owners license this file to you under the Apache
 * License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package eu.tsystems.mms.tic.testerra.plugins.selenoid;

import eu.tsystems.mms.tic.testframework.report.model.context.MethodContext;
import eu.tsystems.mms.tic.testframework.report.model.context.SessionContext;
import eu.tsystems.mms.tic.testframework.report.model.context.Video;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.json.JsonException;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public class SimpleSelenoidVideoTest extends AbstractSelenoidTest {

    @Test()
    public void testT01_SuccessfulTestCaseWillNotCreateVideo() {
        WebDriver driver = WEB_DRIVER_MANAGER.getWebDriver();
        driver.get("https://the-internet.herokuapp.com");
    }

    @Test(dependsOnMethods = "testT01_SuccessfulTestCaseWillNotCreateVideo", alwaysRun = true)
    public void testT02_VideoIsNotPresent_after_SuccessfulTestCaseWillNotCreateVideo() {
        this.assertVideoIsPresentInMethodContext("testT01_SuccessfulTestCaseWillNotCreateVideo", false);
    }

    @Test
    public void testT03_FailedTestCaseWillCreateVideo() {
        final WebDriver driver = WEB_DRIVER_MANAGER.getWebDriver();
        driver.get("https://the-internet.herokuapp.com");
        Assert.fail("must fail");
    }

    AtomicInteger counter = new AtomicInteger(0);

    @Test
    public void testT04_FailedTestWithRetryWillCreateVideo() {
        this.counter.incrementAndGet();
        final WebDriver driver = WEB_DRIVER_MANAGER.getWebDriver();
        driver.get("https://the-internet.herokuapp.com");
        if (counter.get() == 1) {
            throw new JsonException("Expected to read a START_MAP but instead have: END");
        }
    }

    @Test(dependsOnMethods = "testT02_VideoIsNotPresent_after_SuccessfulTestCaseWillNotCreateVideo", alwaysRun = true)
    public void testT05_VideoIsPresent_after_FailedTestCaseWillCreateVideo() {
        //this.isVideoPresentInMethodContext("testT02_FailedTestCaseWillCreateVideo", true);
        String methodName = "testT02_FailedTestCaseWillCreateVideo";
        Optional<Video> optionalVideo = this.findMethodContext(methodName)
                .flatMap(MethodContext::readSessionContexts)
                .map(SessionContext::getVideo)
                .filter(Optional::isPresent)
                .flatMap(video -> Stream.of(video.get()))
                .findFirst();

        assertVideoIsPresent(methodName, optionalVideo, true);
    }

    @Test
    public void testT06_FailedTestWithCollectedAssertions() {
        final WebDriver driver = WEB_DRIVER_MANAGER.getWebDriver();
        driver.get("https://the-internet.herokuapp.com");
        CONTROL.collectAssertions(() -> {
            ASSERT.assertTrue(false);
        });

    }

    @Test(dependsOnMethods = "testT03_FailedTestCaseWillCreateVideo", alwaysRun = true)
    public void testT07_VideoIsPresent_after_FailedTestWithCollectedAssertions() {
        this.assertVideoIsPresentInMethodContext("testT03_FailedTestWithCollectedAssertions", true);
    }

}
