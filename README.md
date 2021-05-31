webis-web-archiver
==================
Source code and scripts for the Webis Web Archiver.

If you use the archiver, please cite [the paper that describes it in detail](https://webis.de/publications.html?q=webis-web-archive#stein_2018y).

Quickstart
----------
You need to have [Docker](https://www.docker.com/) installed.

Then, on a Linux machine:
  - run src-bash/archive.sh for archiving web pages. It will display usage hints.
  - run src-bash/reproduce.sh for reproducing from an archive. It will display usage hints.

The scripts will automatically download and run the image (2GB+ due to all the fonts).

For other OSes, have a look at the shell scripts and adjust the call to docker run accordingly.

Custom user simulation scripts
------------------------------
  - Write a class that extends [InteractionScript](https://github.com/webis-de/webis-web-archiver/blob/master/src/de/webis/webarchive/environment/scripts/InteractionScript.java).
  - You can use the [ScrollDownScript](https://github.com/webis-de/webis-web-archiver/blob/master/src/de/webis/webarchive/environment/scripts/ScrollDownScript.java) as an example, or extend it.
  - The utility class [Windows](https://github.com/webis-de/webis-web-archiver/blob/master/src/de/webis/webarchive/environment/browsers/Windows.java) offers static helper methods for frequently used interactions.
  - Compile your script with the [binaries](https://github.com/webis-de/webis-web-archiver/releases/download/0.1.0/webis-web-archiver.jar) in the class path and create a JAR from it.
  - Place the JAR into a directory named "scriptname-1.0.0", where you replace "scriptname" by the name of your script.
  - Create a file "script.conf" with the following content and put it into the same directory
  
        script = packages.of.your.ScriptClass;
        environment.name = de.webis.java
        environment.version = 1.0.0
      
    where you replace "packages.of.your.ScriptClass" accordingly. For the example ScrollDownScript, that would be
    
        script = de.webis.webarchive.environment.scripts.ScrollDownScript
  - The [src-bash/compile-scroll-down-script.sh](src-bash/compile-scroll-down-script.sh) illustrates the complete compilation process for the ScrollDownScript. Adapt it for your own script.
  - When running archive.sh or reproduce.sh, specify the directory that contains the new directory with "--scriptsdirectory" and give the script name (as in the new directory) with "--script".

