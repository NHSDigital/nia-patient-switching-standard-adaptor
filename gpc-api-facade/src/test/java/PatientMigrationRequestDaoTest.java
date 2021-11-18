import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import uk.nhs.adaptors.connector.dao.PatientMigrationRequestDao;
import uk.nhs.adaptors.connector.configuration.BaseConfiguration;
import uk.nhs.adaptors.gpc.GpcFacadeApplication;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {GpcFacadeApplication.class, BaseConfiguration.class})
public class PatientMigrationRequestDaoTest {

    @Autowired
    private PatientMigrationRequestDao patientMigrationRequestDao;

    @Test
    public void testInsert() {
        var rowsInserted = patientMigrationRequestDao.insertPatientMigrationRequest("1");

        assertThat(rowsInserted).isEqualTo(1);
        patientMigrationRequestDao.deletePatientMigrationRequest("1");
    }
}
