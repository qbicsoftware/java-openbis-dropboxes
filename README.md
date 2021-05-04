# OpenBIS dropboxes written in Java/Groovy
In development experimental ETL procedures written in Java for data registration in openBIS.

## Package

Clone the package with 

```
git clone git@github.com:qbicsoftware/java-openbis-dropboxes.git
```

and run 

```
mvn clean package
```

The compiled Java binaries are located under the projects root folder under `./target/<project-name>.jar`

## Deployment in openBIS

To activate the dropbox, you have to deploy the JAR package in your DSS dropbox in a folder called `lib`. Ass the openBIS classloader does not load classes dynamically, you need to restart the the DSS to add the classes to the runtime.

## ETL routine example

The current example dropbox written in Groovy just takes any file that is placed into the dropbox and registers it as dataset to the sample ``/TEST28/QXEGD018AW``.


