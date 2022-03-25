FROM postgres:14.0

COPY /snomed-immunization-loader/ /snomed-immunization-loader/
WORKDIR /snomed-immunization-loader
CMD ./load_immunization_codes.sh snomed_immunization_codes.csv