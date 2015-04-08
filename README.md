
# Resource Monitor plugin for TeamCity

Monitors various resources, such as database servers and web servers, and allows or disallows build
configurations to run based on the availability of the resource.

[![Build Status](https://travis-ci.org/rodm/teamcity-resource-monitor-plugin.svg)](https://travis-ci.org/rodm/teamcity-resource-monitor-plugin)
[![Download](https://api.bintray.com/packages/rodm/teamcity-plugins/teamcity-resource-monitor-plugin/images/download.svg)](https://bintray.com/rodm/teamcity-plugins/teamcity-resource-monitor-plugin/_latestVersion)

## How to install

Download the plugin using the link above and follow the instructions from the TeamCity documentation, [Installing Additional Plugins](https://confluence.jetbrains.com/display/TCD9/Installing+Additional+Plugins)

## How to configure and use the plugin

The plugin adds a Resources page to the Server Configuration section on the Administration page. The page shows a list
of resources, each resource has a name, the host and port that is monitored, its availability, an enabled/disabled
status and a count of the number of running builds using the resource.

The page allows new resources to be added using the 'Create resource' link, modified using the 'Edit' link or removed
using the 'Delete' link.  Each resource must have a unique name and the host and port to be monitored must be unique.

The number of builds that can run simultaneously using the same resource can be limited by specifying a value in
the 'Build Limit' field, a value of zero allows unlimited builds.

Build configurations that use a resource can be linked to the resource by selecting them from the list shown when
clicking on the 'Add dependency' option. A build configuration can only be linked to one resource.

A resource can be disabled and re-enabled. Disabling a resource prevents any of the linked build configurations
from running on a build agent until the resource is re-enabled.

The plugin stores its configuration in the `<TeamCity data directory>/config/resource.xml` file.
No server restart is required if you modify this file, the plugin will detect the change and reload the configuration.

## How to build the plugin

1. [Download](http://www.jetbrains.com/teamcity/download/index.html) and install TeamCity version 4.5 or later.
2. Copy the `example.build.properties` file to `build.properties`
3. Edit the `build.properties` file to set the properties teamcity.home, teamcity.version and teamcity.java.home
4. Run the Ant build, the default is to compile, run unit tests and package the plugin, the plugin is output to
   `dist/resource-monitor.zip`

The Ant build script provides a target to deploy the plugin to a local configuration directory, deploy-plugin. The
TeamCity server can be started using the start-teamcity-server target, likewise the default Build Agent can be started
using the start-teamcity-agent target. The TEAMCITY_DATA_PATH is set by default to use a local directory and not the
`~/.BuildServer` directory.
