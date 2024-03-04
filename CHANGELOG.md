# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),

## [Unreleased]

## [1.4.5] - 2024-03-01

### Fixed

* Removed the `EhrExtract / AvailabilityTime` field as a fallback value from:
  - `Condition.assertedDate`
  - `List.date`
  - `DiagnosticReport.issued`
  - `Observation.issuedDate`

* Replaced the `EhrExtract / AvailabilityTime` field as a fallback value with `EhrComposition / author / time[@value]` for:
  - `AllergyIntolerance.assertedDate`
  - `MedicationRequest.authoredOn`

## [1.4.4] - 2024-02-28

### Fixed
* Plan Statements (Recalls) were previously mapped with a fixed value of `active` for the Status field.
  The adaptor will now interrogate the text field to see if a status has been provided, otherwise defaulting to `unknown`.

## [1.4.3] - 2024-02-15

### Added
* Added Materialised View in the SnomedCT database to be used when querying for preferred terms.

> [!NOTE]
> **Upgrade information** This release includes an update to the SNOMED database
> Users will need to perform an [update of their patient switching SNOMED database](OPERATING.md#updating-the-snomed-database).
> This will need to be performed first, followed by deploying the updated version of the translator image.

## [1.4.2] - 2024-01-31

### Added
* Added Episodicity information to `comment` property of Observations.

### Fixed
* Fixed malformed role coding defect introduced in version 1.4.1

## [1.4.1] - 2024-01-17

### Added
* Assigning a role to a practitioner who records vaccinations

### Fixed
* Fixed an issue where `Observation Test Group` or `Observation Test Results` were incorrectly creating a relationship to `Filing Comments` using the `has-member` relationship
* `ProcedureRequestMapper.authoredOn` is no longer populated with `EhrExtract / availabilityTime` as a fallback,
  but does use `EhrComposition / author / time` as a fallback instead now.

## [1.4.0] - 2024-01-10

### Changed
* REST buffer size has been set to 150Mb

### Fixed
* Fixed issue where mapping failed due to a Referral Request Priority not being found.
* Codings are now provided (code, display and system) in `PractionionerRole.code` and `Organization.type` fields,
  where only the `text` attribute was provided previously.
* Fixed a bug which could lead to medication resource not being mapped if a failure had occurred when processing the previous EhrExtract during the medication mapping stage


## [1.3.0] - 2023-12-11

### Added 
* In the event of a GP2GP failure, the raw error code is now available in the `/Patient/$gpc.migratestructuredrecord` response section with code system `2.16.840.1.113883.2.1.3.2.4.17.101`.

### Changed
* Removed surplus references to Medication Statements from the Topic and Category entry arrays. The
  [GP Connect documentation](https://developer.nhs.uk/apis/gpconnect-1-6-0/accessrecord_structured_development_consultation_guidance.html#clinical-item-references) states Medications should be referenced using the Medication Request resource, which is already done.
* Updated postman collection to include attachment tests.

## [1.2.1] - 2023-11-23

### Fixed

* Swapped the mixed-up population of 'author' and 'practitioner' fields in AllergyIntolerance.
* A SystmOne attachment split into multiple chunks can arrive with the same filename "Untitled".
  Previously each fragment would be stored using the filename provided, which in the case of SystmOne caused chunks to overwrite each other.
  The adaptor now generates a UUID which is prepended to the filename of a provided chunk to ensure uniqueness.

## [1.2.0] - 2023-11-17

### Added

* Populate `identifier` field for `Practitioner` resources using the General Practitioner (GMP) number if available.

### Fixed

* Prevent `NarrativeStatement / text` sat within a blood pressure `CompoundStatement` from being mapped
  into separate "Comment note" Observation resources. This information is already populated within
  the blood pressure `Observation.comment` field, so creating a separate resource was duplicating this info.

### Changed

* Changed `ReferralRequest` mapping to use `ASAP` instead of the `Stat` value.

## [1.1.0] - 2023-11-09

### Changed

* Updated the way immunization codes are loaded to build these from the relationships reference set in the SNOMED CT 
  release rather than a static set of values.
  The previous static list of values was incomplete and included invalid codes.
  More information about how this improved functionality works can be found in the [SNOMED database documentation](snomed-database-loader/README.md#immunization-codes).

> [!NOTE]
> **Upgrade information** This release includes a [database migration](OPERATING.md#updating-the-application-schema).
> This database migration will need to be performed first, followed by deploying the updated version of the facade and translator images.
> Finally users will need to perform an [update of their patient switching SNOMED database](OPERATING.md#updating-the-snomed-database).

## [1.0.1] - 2023-11-21

This release backported the following fix to the v1.0 release.

### Fixed

* A SystmOne attachment split into multiple chunks can arrive with the same filename "Untitled".
  Previously each fragment would be stored using the filename provided, which in the case of SystmOne caused chunks to overwrite each other.
  The adaptor now generates a UUID which is prepended to the filename of a provided chunk to ensure uniqueness.

## [1.0.0] - 2023-11-01 🎉

### Added

* Added functionality to include episodicity qualifiers when mapping allergy intolerances.

### Changed

* Preserve the ordering of code / translation elements when mapping Codeable Concepts.

### Fixed

* Fix invalid state transition bug which caused the adaptor to move from a failed state to an in-progress state when a
  positive acknowledgement was received.

## [0.16] - 2023-10-31

### Fixed

* Add additional error handling for exceptions raised when processing PSS queue, and MHS queue messages.
* Fix bug in some SQL statements which caused excessively large amounts of data to be returned, sometimes resulting in 
a PostgresSQL Out of Memory error.

## [0.15] - 2023-10-24

* Fixed issue with some `ObservationStatement` coded as blood pressure readings not being output into Bundle.
* Fixed issue with `AllergyIntolerance` recorder and asserter fields mapping.

## [0.14] - 2023-10-17

### Added

* Documented database requirements in [OPERATING.md](/OPERATING.md#database-requirements)
* New docker image published as [nhsdev/nia-ps-db-migration](https://hub.docker.com/r/nhsdev/nia-ps-db-migration)
* Querying SDS service using an addtional endpoint to fetch ODS code

### Fixed

* Fix issue where continue message was not accepted by EMIS
* Fixed issue where EMIS `cid` references caused large message merging to fail 
* Fix issue where attachments were given the incorrect object storage URL
* Fix issue where attachments were not de-compressed
* Enable logging of migration status updates when PS_LOGGING_LEVEL is set to DEBUG
* Fix acknowledgement message as it was not accepted by system one 
* Fix SDS failing as part of GP2GP transfer process

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


