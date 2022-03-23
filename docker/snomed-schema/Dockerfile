FROM postgres:14.0

RUN apt-get update && apt-get install -y unzip
COPY /snomed-database-loader/ /snomed-database-loader/
CMD ./snomed-database-loader/load_release-postgresql.sh snomed-database-loader/uk_sct2cl_32.10.0_20220216000001Z.zip && ./snomed-immunization-loader/load_immunization_codes.sh snomed-immunization-loader/snomed_immunization_codes.csv