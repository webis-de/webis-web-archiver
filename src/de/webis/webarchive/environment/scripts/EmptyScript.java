package de.webis.webarchive.environment.scripts;

import java.io.IOException;
import java.nio.file.Path;

import org.openqa.selenium.WebDriver;

import de.webis.webarchive.common.Version;
import de.webis.webarchive.environment.browsers.Browser;
import de.webis.webarchive.environment.browsers.Windows;

public class EmptyScript extends InteractionScript {
  
  //////////////////////////////////////////////////////////////////////////////
  // CONSTANTS
  //////////////////////////////////////////////////////////////////////////////
  
  public static final String NAME = "empty";
  
  public static final Version VERSION = new Version(1, 1, 0);
  
  //////////////////////////////////////////////////////////////////////////////
  // CONSTRUCTORS
  //////////////////////////////////////////////////////////////////////////////

  public EmptyScript(final Path scriptDirectory)
  throws IOException {
    super(scriptDirectory);
  }
  
  //////////////////////////////////////////////////////////////////////////////
  // FUNCTIONALITY
  //////////////////////////////////////////////////////////////////////////////

  @Override
  protected void executeInteraction(
      final Browser browser, final String startUrl, final Path outputDirectory)
  throws Throwable {
    final WebDriver window = browser.openWindow(startUrl);
 
    final long quietPeriodInSeconds = 5;
    final long waitTimeoutInSeconds = 10;
    browser.waitForQuiescence(quietPeriodInSeconds, waitTimeoutInSeconds);

    Windows.writeDom(window, outputDirectory.resolve("page.html"));
  }

}
