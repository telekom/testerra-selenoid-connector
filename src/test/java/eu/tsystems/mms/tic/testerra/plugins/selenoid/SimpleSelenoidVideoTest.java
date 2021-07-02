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

import eu.tsystems.mms.tic.testframework.execution.testng.AssertCollector;
import eu.tsystems.mms.tic.testframework.webdrivermanager.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import org.testng.annotations.Test;

public class SimpleSelenoidVideoTest extends AbstractSelenoidTest {

    @Test()
    public void testT01_SuccessfulTestCaseWillNotCreateVideo() {
        final WebDriver driver = WebDriverManager.getWebDriver();
        driver.get("https://the-internet.herokuapp.com");
    }

    @Test(dependsOnMethods = "testT01_SuccessfulTestCaseWillNotCreateVideo", alwaysRun = true)
    public void test_VideoIsNotPresent_after_SuccessfulTestCaseWillNotCreateVideo() {
        this.isVideoPresentInMethodContext("testT01_SuccessfulTestCaseWillNotCreateVideo", false);
    }

    @Test
    public void testT02_FailedTestCaseWillCreateVideo() {
        WebDriverManager.setGlobalExtraCapability("sessionTimeout", "5m");
        final WebDriver driver = WebDriverManager.getWebDriver();
        driver.get("https://the-internet.herokuapp.com");
        Assert.fail("must fail");
    }

    @Test(dependsOnMethods = "testT02_FailedTestCaseWillCreateVideo", alwaysRun = true)
    public void test_VideoIsPresent_after_FailedTestCaseWillCreateVideo() {
        this.isVideoPresentInMethodContext("testT02_FailedTestCaseWillCreateVideo", true);
    }

    @Test
    public void testT03_FailedTestWithCollectedAssertions() {
        final WebDriver driver = WebDriverManager.getWebDriver();
        driver.get("https://the-internet.herokuapp.com");
        AssertCollector.assertTrue(false);
    }

    @Test(dependsOnMethods = "testT03_FailedTestWithCollectedAssertions", alwaysRun = true)
    public void test_VideoIsPresent_after_FailedTestWithCollectedAssertions() {
        this.isVideoPresentInMethodContext("testT03_FailedTestWithCollectedAssertions", true);
    }

}
