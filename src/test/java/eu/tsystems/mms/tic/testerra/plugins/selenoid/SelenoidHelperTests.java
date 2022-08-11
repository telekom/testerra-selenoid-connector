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
import eu.tsystems.mms.tic.testframework.pageobjects.UiElement;
import eu.tsystems.mms.tic.testframework.pageobjects.UiElementFinder;
import eu.tsystems.mms.tic.testframework.report.model.context.SessionContext;
import eu.tsystems.mms.tic.testframework.testing.UiElementFinderFactoryProvider;
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
public class SelenoidHelperTests extends AbstractSelenoidTest implements UiElementFinderFactoryProvider {

    @Test
    public void test_SelenoidIsUsed() {
        final WebDriver driver = WEB_DRIVER_MANAGER.getWebDriver();
        driver.get("https://the-internet.herokuapp.com");
        SessionContext currentSessionContext = contextController.getCurrentSessionContext().get();
        boolean selenoidUsed = SelenoidHelper.get().isSelenoidUsed(currentSessionContext);
        Assert.assertTrue(selenoidUsed);
    }

    @Test
    public void test_GetClipboard() {
        final WebDriver driver = WEB_DRIVER_MANAGER.getWebDriver();
        UiElementFinder finder = UI_ELEMENT_FINDER_FACTORY.create(driver);
        driver.get("https://the-internet.herokuapp.com");
        UiElement element = finder.find(By.xpath("//body"));
        element.sendKeys(Keys.CONTROL + "a");
        element.sendKeys(Keys.CONTROL + "c");

        SessionContext currentSessionContext = contextController.getCurrentSessionContext().get();
        String clipboard = SelenoidHelper.get().getClipboard(currentSessionContext);
        Assert.assertNotNull(clipboard);
        Assert.assertTrue(clipboard.contains("Welcome to the-internet"));
    }

    @Test
    public void test_SetClipboard() {
        final WebDriver driver = WEB_DRIVER_MANAGER.getWebDriver();
        UiElementFinder finder = UI_ELEMENT_FINDER_FACTORY.create(driver);
        driver.get("http://the-internet.herokuapp.com/tinymce");
        final String value = "clipboard text";

        SessionContext currentSessionContext = contextController.getCurrentSessionContext().get();
        SelenoidHelper.get().setClipboard(currentSessionContext, value);
        UiElement iframe = finder.find(By.id("mce_0_ifr"));
        UiElement textArea = iframe.find(By.xpath("//body/p"));
        textArea.sendKeys(Keys.CONTROL + "v");

        textArea.assertThat().text().isContaining(value);
    }


}
