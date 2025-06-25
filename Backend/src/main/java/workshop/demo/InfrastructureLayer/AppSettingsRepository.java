package workshop.demo.InfrastructureLayer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import workshop.demo.DomainLayer.AppSettings.AppSettingsEntity;

@Repository
public interface AppSettingsRepository extends JpaRepository<AppSettingsEntity, Long> {

}
