==========
Changelog
==========

This project adheres to `Semantic Versioning <https://semver.org/>`_.

1.3.0 (2022-05-18)
------------------

**Added**

* Handles change in maxquant data structure, uses latest data model and core utils libraries

**Fixed**

**Dependencies**

**Deprecated**


1.2.0 (2022-01-21)
------------------

**Added**

* New DatasetLocator, that helps with the identification of the actual dataset folder in cases of complex nested structures
* Increased verbosity in logging parser errors

**Fixed**

**Dependencies**

**Deprecated**


1.1.5 (2021-09-14)
------------------

**Added**

**Fixed**

**Dependencies**

* Increase version for ``life.qbic:core-utils-lib`` from ``1.9.2-> 1.10.0``

**Deprecated**

1.1.4 (2021-07-20)
------------------

**Added**

**Fixed**

* Fix a misleading NPE, that is thrown during a wrong type conversion in a for loop.

**Dependencies**

**Deprecated**

1.1.3 (2021-07-20)
------------------

**Added**

**Fixed**

* More verbose exception logging due to difficult debugging

**Dependencies**

**Deprecated**

1.1.2 (2021-07-20)
------------------

**Added**

**Fixed**

* Fix ``java.lang.reflect.UndeclaredThrowableException``, when the dataset could not parsed by any parser successfully.

**Dependencies**

* Increase version for ``life.qbic:core-utils-lib`` from ``1.9.1-> 1.9.2``

**Deprecated**


1.1.1 (2021-07-20)
------------------

**Added**

**Fixed**

* Remove NanoporeParser to fix ``GroovyCastException`` (`#17 <https://github.com/qbicsoftware/java-openbis-dropboxes/pull/17>`_)

**Dependencies**

* Increase version for ``life.qbic:core-utils-lib`` from ``1.9.0-> 1.9.1``

**Deprecated**


1.1.0 (2021-07-19)
------------------

**Added**

* Add MaxQuantResultRegistry for MaxQuant analysis result datasets (`#12 <https://github.com/qbicsoftware/java-openbis-dropboxes/pull/12>`_)

* Route MaxQuant result datasets to the `MaxQuantResultRegistry` (`#13 <https://github.com/qbicsoftware/java-openbis-dropboxes/pull/13>`_)

* Learn MaxQuant analysis result dataset structure (`<https://github.com/qbicsoftware/java-openbis-dropboxes/pull/14>`_)

**Fixed**

**Dependencies**

* Increase version for ``life.qbic:data-model-lib`` from ``2.8.1 -> 2.10.0``

* Increase version for ``life.qbic:core-utils-lib`` from ``1.8.0 -> 1.9.0``

**Deprecated**


1.0.1 (2021-06-28)
------------------

**Added**

* Some minor class refactoring
* Project documentation [#10](https://github.com/qbicsoftware/java-openbis-dropboxes/pull/10)

**Fixed**

**Dependencies**

**Deprecated**

1.0.0 (2021-06-25)
------------------

**Added**

 * Basic ETL layout with a main dataset entry point, the dataset type determination and registry assignment (`#1 <https://github.com/qbicsoftware/java-openbis-dropbox/pull/1>`_)

 * Nf-core pipeline result registry (`#3 <https://github.com/qbicsoftware/java-openbis-dropbox/pull/3>`_)

 * Support for a lot of nf-core and qbic-pipeline bioinformatic pipelines (`#4 <https://github.com/qbicsoftware/java-openbis-dropbox/issues/4>`_)

**Fixed**

**Dependencies**

**Deprecated**
