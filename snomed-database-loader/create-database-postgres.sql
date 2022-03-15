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
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA snomedct TO application_user;