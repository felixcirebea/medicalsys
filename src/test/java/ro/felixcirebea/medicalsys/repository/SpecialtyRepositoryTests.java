package ro.felixcirebea.medicalsys.repository;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ro.felixcirebea.medicalsys.entity.SpecialtyEntity;

import java.util.List;
import java.util.Optional;

@DataJpaTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@Disabled
public class SpecialtyRepositoryTests {

    @Autowired
    private SpecialtyRepository specialtyRepository;

    @Test
    public void SpecialtyRepository_FindByNameAndIsActive_ReturnSpecialty() {
        //Arrange
        String name = "TestSpecialty";
        boolean isActive = true;

        SpecialtyEntity entity = new SpecialtyEntity();
        entity.setName(name);
        entity.setIsActive(isActive);

        specialtyRepository.save(entity);

        //Act
        Optional<SpecialtyEntity> specialtyEntityOptional =
                specialtyRepository.findByNameAndIsActive(name, isActive);

        //Assert
        Assertions.assertThat(specialtyEntityOptional.isPresent()).isTrue();
        Assertions.assertThat(specialtyEntityOptional.get().getName()).isEqualTo(name);
        Assertions.assertThat(specialtyEntityOptional.get().getIsActive()).isEqualTo(isActive);
    }

    @Test
    public void SpecialtyRepository_FindByIdAndIsActive_ReturnSpecialty() {
        //Arrange
        String name = "TestSpecialty";
        boolean isActive = true;
        SpecialtyEntity specialtyEntity = new SpecialtyEntity();
        specialtyEntity.setName(name);
        specialtyEntity.setIsActive(isActive);

        Long entityId = specialtyRepository.save(specialtyEntity).getId();

        //Act
        Optional<SpecialtyEntity> specialtyEntityOptional =
                specialtyRepository.findByIdAndIsActive(entityId, isActive);

        //Assert
        Assertions.assertThat(specialtyEntityOptional.isPresent()).isTrue();
        Assertions.assertThat(specialtyEntityOptional.get().getId()).isEqualTo(entityId);
        Assertions.assertThat(specialtyEntityOptional.get().getName()).isEqualTo(name);
        Assertions.assertThat(specialtyEntityOptional.get().getIsActive()).isEqualTo(isActive);
    }

    @Test
    public void SpecialtyRepository_FindAllByIsActive_ReturnMoreThanOneSpecialty() {
        //Arrange
        boolean isActive = true;

        SpecialtyEntity firstEntity = new SpecialtyEntity();
        firstEntity.setName("TestEntity1");
        firstEntity.setIsActive(isActive);
        SpecialtyEntity secondEntity = new SpecialtyEntity();
        secondEntity.setName("TestEntity2");
        secondEntity.setIsActive(isActive);

        specialtyRepository.save(firstEntity);
        specialtyRepository.save(secondEntity);

        //Act
        List<SpecialtyEntity> specialtyEntities = specialtyRepository.findAllByIsActive(isActive);

        //Assert
        Assertions.assertThat(specialtyEntities).isNotNull();
        Assertions.assertThat(specialtyEntities.size()).isEqualTo(2);
    }
}
