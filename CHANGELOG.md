# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),

## [Unreleased]

* Fixed issue with coding text being set from incorrect place when mapping allergy intolerances.

### Added

* Documented database requirements in [OPERATING.md](/OPERATING.md#database-requirements)
* New docker image published as [nhsdev/nia-ps-db-migration](https://hub.docker.com/r/nhsdev/nia-ps-db-migration)
* Querying SDS service using an addtional endpoint to fetch ODS code

### Fixed

* Fix issue where continue message was not accepted by EMIS.
* Fixed issue where EMIS `cid` references caused large message merging to fail. 
* Fix issue where attachments were given the incorrect object storage URL.
* Fix issue where attachments were not de-compressed.
* Enable logging of migration status updates when PS_LOGGING_LEVEL is set to DEBUG.

## [0.13] - 2023-09-13

### Added

* Add details about object storage to operating documentation.
* Enable AWS Instance Roles to be used for connecting to S3

### Fixed

* Fix issue where attachments with filenames containing special characters causes the transfer to fail.
* Fix issue where some resource types do not contain a degraded code when a SNOMED code has not been provided.
* Fix issue where unnecessary length checks on inline attachments could cause a transfer to fail.

## [0.12] - 2023-08-29

### Changed

* Change the codeable concept mapping to map Egton Codes found in `code` elements.
* Change the codeable concept mapping to map Read Codes (READV2, CTV3) found in `code` elements.
* Change the codeable concept mapping to map unknown code systems found in `code` elements.

### Fixed

* Fix issue where Inbound Adaptor rejects an EHR Extract from EMIS containing inline attachments where the description 
contains only the filename.
* Fix issue where Allergy Intolerances are referenced incorrectly when they are referenced from a list.
* Fix issue where a `Condition` is not correctly mapping the `code` element in all instances.


