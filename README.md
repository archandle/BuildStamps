# BuildStamps

A very simple plugin for injecting local incremental build numbers and timestamps into
your Maven Project properties, without requiring an SCM.

## Goals

### :buildRevision

Provides an incremental build number property from a properties file.

If the properties file contains an integer build number, it increments it in preparation of the subsequent build. Otherwise, the revision remains unchanged.

#### Configuration

* `buildNumberPropertiesFile` - Path to the properties file that stores the revision.
  * Default: `${baseDir}/buildNumber.props`
* `revisionPropertyName` - The property name in your Maven Project that is populated by the fetched revision.
  * Default: `buildStamps.buildNumber`

### :buildTimestamp

Provides a formatted timestamp.

#### Configuration

* `timestampFormat` - A timestamp format, as defined by `java.time.format.DateTimeFormatter`.
  * Default: `yyyy-MM-dd-HH-mm-ss`
* `timestampPropertyName` - The property name in your Maven Project that is populated by the fetched revision.
  * Default: `buildStamps.timestamp`

## Build Instructions

Run `mvn clean install` on this project.