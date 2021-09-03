/*
 * Testerra
 *
 * (C) 2021, Martin Großmann, T-Systems Multimedia Solutions GmbH, Deutsche Telekom AG
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

import eu.tsystems.mms.tic.testerra.plugins.selenoid.utils.SelenoidHelper;
import eu.tsystems.mms.tic.testframework.pageobjects.GuiElement;
import eu.tsystems.mms.tic.testframework.report.model.context.SessionContext;
import eu.tsystems.mms.tic.testframework.report.utils.ExecutionContextController;
import eu.tsystems.mms.tic.testframework.webdrivermanager.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Created on 02.07.2021
 *
 * @author mgn
 */
public class SelenoidHelperTests extends AbstractSelenoidTest {

    @Test
    public void test_SelenoidIsUsed() {
        final WebDriver driver = WebDriverManager.getWebDriver();
        driver.get("https://the-internet.herokuapp.com");
        SessionContext currentSessionContext = ExecutionContextController.getCurrentSessionContext();
        boolean selenoidUsed = SelenoidHelper.get().isSelenoidUsed(currentSessionContext);
        Assert.assertTrue(selenoidUsed);
    }

    @Test
    public void test_GetClipboard() {
        final WebDriver driver = WebDriverManager.getWebDriver();
        driver.get("https://the-internet.herokuapp.com");
        GuiElement element = new GuiElement(driver, By.xpath("//body"));
        element.sendKeys(Keys.CONTROL + "a");
        element.sendKeys(Keys.CONTROL + "c");

        SessionContext currentSessionContext = ExecutionContextController.getCurrentSessionContext();
        String clipboard = SelenoidHelper.get().getClipboard(currentSessionContext);
        Assert.assertNotNull(clipboard);
        Assert.assertTrue(clipboard.contains("Welcome to the-internet"));
    }

    @Test
    public void test_SetClipboard() {
        WebDriverManager.setGlobalExtraCapability("sessionTimeout", "10m");
        final WebDriver driver = WebDriverManager.getWebDriver();
        driver.get("http://the-internet.herokuapp.com/tinymce");
        final String value = "clipboard text";

        SessionContext currentSessionContext = ExecutionContextController.getCurrentSessionContext();
        SelenoidHelper.get().setClipboard(currentSessionContext, value);
        GuiElement iframe = new GuiElement(driver, By.id("mce_0_ifr"));
        GuiElement textArea = new GuiElement(driver, By.xpath("//body/p"), iframe);
        textArea.sendKeys(Keys.CONTROL + "v");

        textArea.asserts().assertTextContains(value);
    }


}
