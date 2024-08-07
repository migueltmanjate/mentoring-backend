package mz.org.fgh.mentoring.dto.form;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.core.annotation.Creator;
import lombok.AllArgsConstructor;
import lombok.Data;
import mz.org.fgh.mentoring.base.BaseEntityDTO;
import mz.org.fgh.mentoring.dto.partner.PartnerDTO;
import mz.org.fgh.mentoring.dto.programmaticarea.ProgrammaticAreaDTO;
import mz.org.fgh.mentoring.entity.form.Form;
import mz.org.fgh.mentoring.entity.formQuestion.FormQuestion;
import mz.org.fgh.mentoring.entity.partner.Partner;
import mz.org.fgh.mentoring.entity.programaticarea.ProgrammaticArea;
import mz.org.fgh.mentoring.util.LifeCycleStatus;
import mz.org.fgh.mentoring.util.Utilities;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
public class FormDTO extends BaseEntityDTO {

    private String code;

    private String name;

    private String description;

    @JsonProperty(value = "partner")
    private PartnerDTO partnerDTO;

    @JsonProperty(value = "programmaticArea")
    private ProgrammaticAreaDTO programmaticAreaDTO;

    @JsonProperty(value = "formQuestions")
    private List<FormQuestionDTO> formQuestions = new ArrayList<FormQuestionDTO>();

    private Integer targetPatient;

    private Integer targetFile;

    private Date createdAt;

    private String createdBy;

    @Creator
    public FormDTO() {
        super();
    }

    public FormDTO(Form form) {
        super(form);
        this.code = form.getCode();
        this.name = form.getName();
        this.description = form.getDescription();
        this.createdAt = form.getCreatedAt();
        this.createdBy = form.getCreatedBy();
        if (Utilities.stringHasValue(this.getLifeCycleStatus())) form.setLifeCycleStatus(LifeCycleStatus.valueOf(this.getLifeCycleStatus()));
        try {
            if(form.getPartner()!=null) {
                this.partnerDTO = new PartnerDTO(form.getPartner());
            }
            if(form.getProgrammaticArea()!=null) {
                this.programmaticAreaDTO = new ProgrammaticAreaDTO(form.getProgrammaticArea());
            }
            if(form.getFormQuestions()!=null && !form.getFormQuestions().isEmpty()) {
                List<FormQuestion> formQuestionList = form.getFormQuestions();
                for (FormQuestion formQuestion: formQuestionList) {
                    FormQuestionDTO formQuestionDTO = new FormQuestionDTO(formQuestion);
                    formQuestions.add(formQuestionDTO);
                }
            }
            this.targetPatient = form.getTargetPatient();
            this.targetFile = form.getTargetFile();
        } catch (Exception e) {

        }
    }

    public Form toForm() {
        Form form = new Form();
        form.setUuid(this.getUuid());
        form.setId(this.getId());
        form.setCreatedAt(this.getCreatedAt());
        form.setUpdatedAt(this.getUpdatedAt());
        form.setDescription(this.getDescription());
        form.setName(this.getName());
        form.setCode(this.getCode());
        form.setTargetFile(this.getTargetFile());
        form.setTargetPatient(this.getTargetPatient());
        if (Utilities.stringHasValue(this.getLifeCycleStatus())) form.setLifeCycleStatus(LifeCycleStatus.valueOf(this.getLifeCycleStatus()));
        if(this.getPartnerDTO()!=null) form.setPartner(new Partner(this.getPartnerDTO()));
        if(this.getProgrammaticAreaDTO()!=null) form.setProgrammaticArea(new ProgrammaticArea(this.getProgrammaticAreaDTO()));
        return form;
    }
}
