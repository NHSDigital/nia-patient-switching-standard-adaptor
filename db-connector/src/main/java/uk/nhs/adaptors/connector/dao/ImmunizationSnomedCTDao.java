package uk.nhs.adaptors.connector.dao;

import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.locator.UseClasspathSqlLocator;
import org.jdbi.v3.sqlobject.statement.SqlQuery;

import uk.nhs.adaptors.connector.model.ImmunizationSnomedCT;

public interface ImmunizationSnomedCTDao {
    @SqlQuery("select_immunization_concept_id")
    @UseClasspathSqlLocator
    ImmunizationSnomedCT getImmunizationSnomedUsingConceptOrDescriptionId(@Bind("sctid") String sctId);

    @SqlQuery("verify_immunizations_loaded")
    @UseClasspathSqlLocator
    boolean areImmunizationCodesLoaded();
}
