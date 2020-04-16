package eu.tsystems.mms.tic.testerra.plugins.video;


import eu.tsystems.mms.tic.testframework.logging.Loggable;
import eu.tsystems.mms.tic.testframework.webdrivermanager.DesktopWebDriverRequest;

/**
 * Extends DesktopWebDriverRequest by adding Video fields
 * <p>
 * Date: 15.04.2020
 * Time: 11:44
 *
 * @author Eric Kubenka
 */
public class VideoRequest implements Loggable {

    public DesktopWebDriverRequest webDriverRequest;
    public String selenoidVideoName;
}
