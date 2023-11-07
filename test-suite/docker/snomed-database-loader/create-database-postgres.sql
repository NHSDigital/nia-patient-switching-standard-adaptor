drop schema if exists snomedct cascade;
create schema snomedct;
set schema 'snomedct';

create table description_s(
                              id varchar(18) not null,
                              effectivetime char(8) not null,
                              active char(1) not null,
                              moduleid varchar(18) not null,
                              conceptid varchar(18) not null,
                              languagecode varchar(2) not null,
                              typeid varchar(18) not null,
                              term text not null,
                              casesignificanceid varchar(18) not null,
                              PRIMARY KEY(id, effectivetime)
);
CREATE INDEX description_conceptid_idx ON snomedct.description_s
    USING btree (conceptid COLLATE pg_catalog."default");

create table langrefset_s(
                             id uuid not null,
                             effectivetime char(8) not null,
                             active char(1) not null,
                             moduleid varchar(18) not null,
                             refsetid varchar(18) not null,
                             referencedcomponentid varchar(18) not null,
                             acceptabilityid varchar(18) not null,
                             PRIMARY KEY(id, effectivetime)
);
CREATE INDEX langrefset_referencedcomponentid_idx ON snomedct.langrefset_s
    USING btree (referencedcomponentid);

CREATE table relationship_s(
                               id varchar(18) not null,
                               effectivetime char(8) not null,
                               active char(1) not null,
                               moduleid varchar(18) not null,
                               sourceId varchar(18) not null,
                               destinationId varchar(18) not null,
                               relationshipGroup char(8) not null,
                               typeId varchar(18) not null,
                               characteristicTypeId varchar(18) not null,
                               modifierId varchar(18) not null,
                               PRIMARY KEY(id, effectivetime)
);
CREATE INDEX relationship_sourceId_idx ON snomedct.relationship_s
    USING btree (sourceId);
CREATE INDEX relationship_destinationId_idx ON snomedct.relationship_s
    USING btree (destinationId);
CREATE INDEX relationship_typeId_idx ON snomedct.relationship_s
    USING btree (typeId);

CREATE MATERIALIZED VIEW immunization_codes AS
WITH RECURSIVE relationship_cte AS (
    -- Start with the root conceptIds for immunization (procedure & product) where relationshipType is 'IsA' (child of)
    SELECT DISTINCT sourceId
    FROM snomedct.relationship_s
    WHERE typeId = '116680003'
      AND (destinationId = '787859002' OR destinationId = '127785005')

    UNION

    -- Recursively find child records where relationshipType is 'IsA' (child of)
    -- if no records are found then the maximum depth for this recursion has been reached
    SELECT r.sourceId
    FROM snomedct.relationship_s r
             JOIN relationship_cte d ON r.destinationId = d.sourceId
    WHERE r.typeId = '116680003'
)
SELECT sourceid AS conceptId
FROM relationship_cte;

GRANT USAGE ON SCHEMA snomedct TO application_user;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA snomedct TO application_user;