FROM postgres:14.0

RUN apt-get update && apt-get install -y unzip
COPY /snomed-database-loader/ /snomed-database-loader/
WORKDIR /snomed-database-loader
CMD ./load_release-postgresql.sh uk_sct2mo_36.0.0_20230412000001Z.zip