package uk.nhs.adaptors.connector.dao;

import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.locator.UseClasspathSqlLocator;
import org.jdbi.v3.sqlobject.statement.SqlQuery;

import uk.nhs.adaptors.connector.model.SnomedCTDescription;

public interface SnomedCTDao {
    @SqlQuery("select_description_concept_id_and_display_name")
    @UseClasspathSqlLocator
    SnomedCTDescription getSnomedDescriptionUsingConceptIdAndDisplayName(@Bind("conceptId") String conceptId,
        @Bind("displayName") String displayName);

    @SqlQuery("select_description_concept_id")
    @UseClasspathSqlLocator
    SnomedCTDescription getSnomedDescriptionUsingConceptId(@Bind("conceptId") String conceptId);

    @SqlQuery("select_description_description_id")
    @UseClasspathSqlLocator
    SnomedCTDescription getSnomedDescriptionUsingDescriptionId(@Bind("descriptionId") String descriptionId);

    @SqlQuery("select_preferred_term_concept_id")
    @UseClasspathSqlLocator
    SnomedCTDescription getSnomedDescriptionPreferredTermUsingConceptId(@Bind("conceptId") String conceptId);
}
