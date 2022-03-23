FROM postgres:14.0

RUN apt-get update && apt-get install -y unzip
COPY /snomed-database-loader/ /snomed-database-loader/
WORKDIR /snomed-database-loader
CMD ./load_release-postgresql.sh uk_sct2cl_32.10.0_20220216000001Z.zip && cd ../snomed-immunization-loader && ./load_immunization_codes.sh snomed_immunization_codes.csv