-- the conceptIds being checked for below represent the immunization root codes and those immunization codes outside
-- of the root code hierarchy, as described in the snomed-database-loader README.md file

SELECT COUNT(DISTINCT i.concept_or_description_id) = 16 -- total number of codes
FROM "snomedct".immunization_codes i
WHERE i.concept_or_description_id IN (
                      '787859002', '127785005', '304250009', '90351000119108','713404003','2997511000001102',
                      '308101000000104','1036721000000101', '1373691000000102', '945831000000105', '542931000000103',
                      '735981009', '90640007', '571631000119106', '764141000000106', '170399005'
                     );

