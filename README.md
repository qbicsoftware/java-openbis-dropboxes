# OpenBIS dropboxes written in Java/Groovy
In development experimental ETL procedures written in Java for data registration in openBIS.

Overview:

- [Build JAR artifact](#build-jar-artifact)
- [Deployment in openBIS DSS](#deployment-in-openbis-dss)
- [Dataflow](#dataflow)
    * [Overview](#overview)
    * [Structure](#structure)
    * [Add a new dataset parser](#add-a-new-dataset-parser)
    * [Add a new dataset registry](#add-a-new-dataset-registry)

## Build JAR artifact

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

To activate the dropbox, you have to deploy the JAR package in your DSS dropbox in a folder called `lib`. As the openBIS classloader does not load classes dynamically, you need to restart the the DSS to add the classes to the runtime.

## Dataflow 

### Overview
The overall logic that is implemented by this dropbox, follows the process displayed in the following
process diagram:

![Dataset Registration Process](./docs/Dataset_Registration_Process.png)

**Dataset arrives**. That means that a new dataset has been dropped, in the active openBIS datastore server dropbox.

**Identify dataset type**. In this step, several dataset parsers are applied to parse the current dataset structure. If no parser is available, that is able to parse the dataset structure successfully,
then this step exits with an identification exception and manual intervention is necessary.

**Validate dataset structure**. In this step, the dataset is validated. A dataset is valid, if its structure follows a certain specification. Every dataset structure
is expressed in a JSON schema, maintained in the central [data model library](https://github.com/qbicsoftware/data-model-lib). 

**Assign ETL registry**. After the dataset has been identified and is found valid, the ETL registry is assigned. The assignment must be unambiguous, 
meaning that one registry takes care of one dataset type!

**Execute ETL registry**. This is the last crucial step. The registry is triggered and tries to register
the dataset in openBIS.

### Structure

The main entry point is the Groovy class [MainETL](src/main/groovy/life/qbic/registration/MainETL.groovy):

```groovy
@Log4j2
class MainETL extends AbstractJavaDataSetRegistrationDropboxV2 {

    static List<DatasetParser<?>> listOfParsers = [
            new BioinformaticAnalysisParser(),
            new NanoporeParser()
    ] as List<DatasetParser<?>>
    // ...
    @Override
    void process(IDataSetRegistrationTransactionV2 transaction) {}
```

We need to extend from the abstract class `AbstractJavaDataSetRegistrationDropboxV2`, which serves the entry
point we need to define in the [dropbox configuration file](). The abstract method ``process()`` is then called
by the openBIS DSS runtime, one a dataset is dropped in the dropbox and a marker file has been created.



### Add a new dataset parser

### Add a new dataset registry
