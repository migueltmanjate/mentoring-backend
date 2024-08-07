package mz.org.fgh.mentoring.dto.resource;
import io.micronaut.core.annotation.Creator;
import io.micronaut.http.multipart.CompletedFileUpload;
import lombok.AllArgsConstructor;
import lombok.Data;
import mz.org.fgh.mentoring.base.BaseEntityDTO;
import mz.org.fgh.mentoring.entity.earesource.Resource;
import mz.org.fgh.mentoring.util.LifeCycleStatus;
import java.io.Serializable;

/**
 * @author Joao Nuno Mabjaia
 */
@Data
@AllArgsConstructor
public class ResourceDTO extends BaseEntityDTO implements Serializable {
    private String resource;
    private CompletedFileUpload file;
    @Creator
    public ResourceDTO () {
        super();
    }
    public  ResourceDTO(Resource resource, CompletedFileUpload file) {
        super(resource);
        this.setResource(resource.getResource());
        this.setFile(file);
    }

    public Resource convertToReource() {
        Resource resource1 = new Resource();
        resource1.setUuid(this.getUuid());
        resource1.setId(this.getId());
        resource1.setUpdatedAt(this.getUpdatedAt());
        resource1.setCreatedAt(this.getCreatedAt());
        resource1.setResource(this.getResource());
        return resource1;
    }

}

