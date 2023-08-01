package ro.felixcirebea.medicalsys.util;

import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class Contributor implements InfoContributor {

    private Integer failedDeleteOperations = 0;

    @Override
    public void contribute(Info.Builder builder) {
        builder.withDetail("operations-monitor",
                Map.of("failed-delete-operations", String.valueOf(failedDeleteOperations))
        );
    }

    public void incrementFailedDeleteOperations() {
        this.failedDeleteOperations++;
    }
}
