package de.webis.webarchive.environment.scripts;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.webis.webarchive.common.configuration.Configuration;
import de.webis.webarchive.common.exceptions.ConfigurationException;
import de.webis.webarchive.common.resources.ScriptConfiguration;
import de.webis.webarchive.environment.browsers.Browser;

public abstract class InteractionScript {
  
  //////////////////////////////////////////////////////////////////////////////
  // LOGGING
  //////////////////////////////////////////////////////////////////////////////

  private static final Logger LOG =
      Logger.getLogger(InteractionScript.class.getName());
  
  //////////////////////////////////////////////////////////////////////////////
  // MEMBERS
  //////////////////////////////////////////////////////////////////////////////
  
  private final Path scriptDirectory;
  
  //////////////////////////////////////////////////////////////////////////////
  // CONSTRUCTORS
  //////////////////////////////////////////////////////////////////////////////
  
  public InteractionScript(final Path scriptDirectory)
  throws IOException {
    if (scriptDirectory == null) { throw new NullPointerException(); }
    if (!Files.isDirectory(scriptDirectory)) {
      throw new NotDirectoryException(scriptDirectory.toString());
    }
    if (!Files.isReadable(scriptDirectory)) {
      throw new IOException("Script directory '" + scriptDirectory + "' for "
          + this + " is not readable");
    }
    
    this.scriptDirectory = scriptDirectory;
  }
  
  public static InteractionScript from(
      final Configuration configuration, final Path scriptDirectory)
  throws ConfigurationException {
    final String className =
        configuration.get(ScriptConfiguration.PROPERTY_INTERACTION_SCRIPT_CLASS);
    
    try {
      final Class<?> genericClassObject = Class.forName(className);
      if (!InteractionScript.class.isAssignableFrom(genericClassObject)) {
        throw new IllegalArgumentException("Not an implementation of "
            + InteractionScript.class.getName() + ": " + className);
      }
      final int modifiers = genericClassObject.getModifiers();
      if (Modifier.isAbstract(modifiers)) {
        throw new IllegalArgumentException(className
            + " can not be instantiated as it is abstract");
      }
      if (Modifier.isInterface(modifiers)) {
        throw new IllegalArgumentException(className
            + " can not be instantiated as it is an interface");
      }
      
      @SuppressWarnings("unchecked")
      final Class<? extends InteractionScript> classObject =
          (Class<? extends InteractionScript>) genericClassObject;
      final Constructor<? extends InteractionScript> constructor =
          classObject.getConstructor(Path.class);
      return constructor.newInstance(scriptDirectory);
    } catch (final NoSuchMethodException | SecurityException
        | InstantiationException | IllegalAccessException
        | InvocationTargetException | ClassNotFoundException 
        | IllegalArgumentException e) {
      throw new ConfigurationException(e);
    }
  }
  
  //////////////////////////////////////////////////////////////////////////////
  // GETTER
  //////////////////////////////////////////////////////////////////////////////
  
  protected Path getScriptDirectory() {
    return this.scriptDirectory;
  }
  
  //////////////////////////////////////////////////////////////////////////////
  // FUNCTIONALITY
  //////////////////////////////////////////////////////////////////////////////
  
  /**
   * Runs this script using given browser and URL as entry point.
   * @param browser The browser to use
   * @param startUrl The URL to use as entry point in the script
   * @param outputDirectory The directory to write anything to
   * @return If the run completed successfully
   * @throws IOException If the output directory is not a readable and writable
   * directory
   */
  public boolean run(
      final Browser browser, final String startUrl, final Path outputDirectory) 
  throws IOException {
    if (browser == null) { throw new NullPointerException(); }
    if (startUrl == null) { throw new NullPointerException(); }
    if (outputDirectory == null) { throw new NullPointerException(); }
    
    Files.createDirectories(outputDirectory);
    if (!Files.isDirectory(outputDirectory)) {
      throw new NotDirectoryException(outputDirectory.toString());
    }
    if (!Files.isReadable(outputDirectory)) {
      throw new IOException("Output directory '" + outputDirectory + "' for "
          + this + " is not readable");
    }
    if (!Files.isWritable(outputDirectory)) {
      throw new IOException("Output directory '" + outputDirectory + "' for "
          + this + " is not writable");
    }
    
    String runName = null;
    if (LOG.isLoggable(Level.FINE)) {
      runName = this.makeRunName(browser, startUrl, outputDirectory);
      LOG.log(Level.FINE, "Running " + runName);
    }
    try {
      this.executeInteraction(browser, startUrl, outputDirectory);
    } catch (final Throwable e) {
      if (runName == null) {
        runName = this.makeRunName(browser, startUrl, outputDirectory);
      }
      LOG.log(Level.INFO, "Failed running " + runName, e);
      return false;
    }

    if (LOG.isLoggable(Level.FINE)) {
      LOG.log(Level.FINE, "Successfully completed running " + runName);
    }
    return true;
  }
  
  /**
   * Runs this script using given browser and URL as entry point.
   * @param browser The browser to use
   * @param startUrl The URL to use as entry point in the script
   * @param outputDirectory The directory to write anything to
   * @throws Throwable On any kind of error
   */
  protected abstract void executeInteraction(
      final Browser browser, final String startUrl, final Path outputDirectory)
  throws Throwable;
  
  /**
   * Gets the name of a run for logging purposes.
   */
  protected String makeRunName(
      final Browser browser, final String startUrl, final Path outputDirectory) {
    final StringBuilder nameBuilder = new StringBuilder();
    nameBuilder
      .append("script ").append(this)
      .append(" for browser ").append(browser)
      .append(", start url ").append(startUrl)
      .append(", output at ").append(outputDirectory);
    return nameBuilder.toString();
  }

}
