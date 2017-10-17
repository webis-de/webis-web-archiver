package de.webis.webarchive.environment.browsers;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

public class Windows {
  
  //////////////////////////////////////////////////////////////////////////////
  // LOGGING
  //////////////////////////////////////////////////////////////////////////////

  private static final Logger LOG =
      Logger.getLogger(Windows.class.getName());
  
  //////////////////////////////////////////////////////////////////////////////
  // CONSTRUCTOR
  //////////////////////////////////////////////////////////////////////////////
  
  private Windows() { }
  
  //////////////////////////////////////////////////////////////////////////////
  // FUNCTIONALITY
  //////////////////////////////////////////////////////////////////////////////
  
  // JAVASCRIPT

  public static Object execute(final WebDriver window,
      final String javaScriptCode) {
    final JavascriptExecutor js = ((JavascriptExecutor) window); 
    return js.executeScript(javaScriptCode);
  }

  // GETTERS
  
  public static String getHTML(final WebDriver window) {
    return Windows.execute(window, "return document.documentElement.outerHTML;")
        .toString();
  }
  
  public static String writeHTML(final WebDriver window, final Path file)
  throws IOException {
    final String html = Windows.getHTML(window);
    Files.write(file, html.getBytes());
    return html;
  }
  
  public static int[] getBackgroundColor(final WebDriver window) {
    final String backgroundColorString = window.findElement(By.tagName("body"))
        .getCssValue("background-color");
    final String[] backgroundRGBStrings = backgroundColorString
        .replace("rgba(", "").replace(")", "").split(",");
    final int[] backgroundRGB = new int[backgroundRGBStrings.length];
    for (int i = 0; i < backgroundRGB.length; ++i) {
      backgroundRGB[i] = Integer.parseInt(backgroundRGBStrings[i].trim());
    }
    return backgroundRGB;
  }
  
  public static int getScrollHeight(final WebDriver window) {
    return ((Long) Windows.execute(window,
        "return document.body.scrollHeight;")).intValue();
  }
  
  public static int getScrollYPosition(final WebDriver window) {
    return ((Long) Windows.execute(window,
        "return window.pageYOffset + window.innerHeight;")).intValue();
  }
  
  public static boolean isScrolledToBottom(final WebDriver window) {
    return (Boolean) Windows.execute(window,
        "return (window.pageYOffset + window.innerHeight) >= document.body.scrollHeight;");
  }
  
  public static int getViewportWidth(final WebDriver window) {
    return ((Long) Windows.execute(window, "return window.innerWidth;"))
        .intValue();
  }
  
  public static int getViewportHeight(final WebDriver window) {
    return ((Long) Windows.execute(window, "return window.innerHeight;"))
        .intValue();
  }
  
  // RESIZING
  
  public static void resizeWidth(
      final WebDriver window, final int width) {
    final int height = window.manage().window().getSize().height;
    Windows.resize(window, width, height);
  }
  
  public static void resizeHeight(
      final WebDriver window, final int height) {
    final int width = window.manage().window().getSize().width;
    Windows.resize(window, width, height);
  }
  
  public static void resize(
      final WebDriver window, final int width, final int height) {
    if (LOG.isLoggable(Level.FINE)) {
      LOG.log(Level.FINE, "Resizing browser window to " + width + "x" + height);
    }
    window.manage().window().setSize(new Dimension(width, height));
  }
  
  public static void resizeViewportWidth(
      final WebDriver window, final int width) {
    final int height = Windows.getViewportHeight(window);
    Windows.resizeViewport(window, width, height);
  }
  
  public static void resizeViewportHeight(
      final WebDriver window, final int height) {
    final int width = Windows.getViewportWidth(window);
    Windows.resizeViewport(window, width, height);
  }
  
  public static void resizeViewport(
      final WebDriver window, final int width, final int height) {
    final int windowBorderHorizontal = ((Long) Windows.execute(window,
        "return window.outerWidth - window.innerWidth;")).intValue();
    final int windowBorderVertical = ((Long) Windows.execute(window,
        "return window.outerHeight - window.innerHeight;")).intValue();
    
    if (windowBorderHorizontal < 0 || windowBorderVertical < 0) {
      LOG.log(Level.INFO, "Negative window borders of "
          + windowBorderHorizontal + "x" + windowBorderVertical
          + " maybe due to emulation: ignoring borders for viewport sizing");
      Windows.resize(window, width, height);
    } else {
      final int windowWidth = width + windowBorderHorizontal;
      final int windowHeight = height + windowBorderVertical;
      if (LOG.isLoggable(Level.FINE)) {
        LOG.log(Level.FINE, "Resizing browser viewport to "
            + width + "x" + height + " with border "
            + windowBorderHorizontal + "x" + windowBorderVertical);
      }
      Windows.resize(window, windowWidth, windowHeight);
    }
  }
  
  // SCREENSHOT
  
  public static void screenshotPng(final WebDriver window,
      final Path outputFile)
  throws IOException {
    final TakesScreenshot screenshooter = (TakesScreenshot) window;
    final Path screenshot =
        screenshooter.getScreenshotAs(OutputType.FILE).toPath();
    Files.move(screenshot, outputFile);
  }
  
  public static BufferedImage screenshot(final WebDriver window)
  throws IOException {
    final TakesScreenshot screenshooter = (TakesScreenshot) window;
    final byte[] screenshotBytes =
        screenshooter.getScreenshotAs(OutputType.BYTES);
    return ImageIO.read(new ByteArrayInputStream(screenshotBytes));
  }

  // SCROLLING
  
  public static void scrollToTop(final WebDriver window) {
    Windows.execute(window, "window.scrollTo(0, 0);");
  }
  
  public static void scrollToBottom(final WebDriver window) {
    Windows.execute(window, "window.scrollTo(0, document.body.scrollHeight);");
  }
  
  public static void scrollDownOneWindow(final WebDriver window) {
    Windows.execute(window,
        "window.scrollTo(0, window.pageYOffset + window.innerHeight);");
  }

  public static int scrollToBottom(
      final WebDriver window,
      final int maxTimesScrolled, final long scrollTimeOffsetInSeconds)
  throws InterruptedException {
    long scrollHeight = Windows.getScrollHeight(window);
    for (int timesScrolled = 0; timesScrolled < maxTimesScrolled; ++timesScrolled) {
      Windows.scrollToBottom(window);
      Thread.sleep(scrollTimeOffsetInSeconds * 1000);

      final long newScrollHeight = Windows.getScrollHeight(window);
      if (newScrollHeight == scrollHeight) { return timesScrolled + 1; }
      scrollHeight = newScrollHeight;
    }

    return maxTimesScrolled;
  }

}
