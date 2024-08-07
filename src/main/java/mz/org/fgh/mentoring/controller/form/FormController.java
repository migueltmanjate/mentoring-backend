package mz.org.fgh.mentoring.controller.form;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Patch;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.Put;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.rules.SecurityRule;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.inject.Inject;
import mz.org.fgh.mentoring.api.RESTAPIMapping;
import mz.org.fgh.mentoring.api.RestAPIResponse;
import mz.org.fgh.mentoring.base.BaseController;
import mz.org.fgh.mentoring.dto.form.FormDTO;
import mz.org.fgh.mentoring.entity.form.Form;
import mz.org.fgh.mentoring.error.MentoringAPIError;
import mz.org.fgh.mentoring.service.form.FormService;
import mz.org.fgh.mentoring.util.Utilities;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Secured(SecurityRule.IS_AUTHENTICATED)
@Controller(RESTAPIMapping.FORM_CONTROLLER)
public class FormController extends BaseController {

    @Inject
    FormService formService;
   public FormController(){
    }

    public static final Logger LOG = LoggerFactory
            .getLogger(FormController.class);

    @ApiResponse(content = @Content(mediaType = MediaType.APPLICATION_JSON))
    @Get("/form/{limit}/{offset}")
    public List<FormDTO> getAll(@PathVariable("limit") long limit, @PathVariable("offset") long offset) {
        LOG.debug("Searching forms version 2");
        return formService.findAll(limit, offset);
    }

    @Get("/getById/{id}")
    public Optional<Form> findById(@PathVariable("id") Long id){
        return formService.findById(id);
    }

    @Get("/sample-forms")
    public List<FormDTO> findSampleIndicatorForms(){
      return  this.formService.findSampleIndicatorForms();
    }

    @Operation(summary = "Return a list of forms for the listed params")
    @ApiResponse(content = @Content(mediaType = MediaType.APPLICATION_JSON))
    @Get("/search")
    @Tag(name = "Form")
    public List<FormDTO> findBySelectedFilter(@Nullable @QueryValue("code") String code,
                                              @Nullable @QueryValue("name") String name,
                                              @Nullable @QueryValue("program") String program,
                                              @Nullable @QueryValue("programmaticAreaCode") String programmaticAreaCode){
        return this.formService.findBySelectedFilter(code==null? StringUtils.EMPTY:code,
                                                    name==null? StringUtils.EMPTY:name,
                                                    programmaticAreaCode==null? StringUtils.EMPTY:programmaticAreaCode,
                                                    program==null?StringUtils.EMPTY:program);
    }

    @Get("/programaticarea/{progrArea}")
    public List<FormDTO> findFormByProgrammaticAreaUuid(@PathVariable("progrArea") String progrArea){

        return this.formService.findFormByProgrammaticAreaUuid(progrArea);
    }

    @Get("/getByTutorUuidd/{tutorUuid}")
    public List<FormDTO> getByTutorUuidd(@PathVariable("tutorUuid") String tutorUuid){

        List<Form> forms = this.formService.getByTutorUuid(tutorUuid);
        if (Utilities.listHasElements(forms)) {
            try {
                return Utilities.parseList(forms, FormDTO.class);
            } catch (InstantiationException | IllegalAccessException | NoSuchMethodException |
                     InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
        else return new ArrayList<>();
    }



    @Post(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON
    )
    public Form create (@Body Form form) {
        LOG.debug("Created form {} ", form);
        return this.formService.create(form);
    }

    @Put(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON
    )
    public Form update(@Body Form form){
        LOG.debug("update form {} ", form);
        return this.formService.update(form);
    }

    @ApiResponse(content = @Content(mediaType = MediaType.APPLICATION_JSON))
    @Get("/getformsbycodeornameorprogrammaticarea")
    public List<FormDTO> getFormsByCodeAndNameAndProgrammaticAreaName(
            @Nullable @QueryValue("code") String code,
            @Nullable @QueryValue("name") String name,
            @Nullable @QueryValue("programmaticArea") String programmaticArea
    ) {
        List<FormDTO> forms = formService.search(code, name, programmaticArea);;
        return forms;
    }

    @Operation(summary = "Save or update a form and its associated objects")
    @ApiResponse(content = @Content(mediaType = MediaType.APPLICATION_JSON))
    @Post("/saveOrUpdate")
    @Tag(name = "Form")
    public HttpResponse<RestAPIResponse> saveOrUpdate(@NonNull @Body FormDTO formDTO, Authentication authentication){
        try {
            formDTO.setCode(null);
            FormDTO dto = this.formService.saveOrUpdate((Long) authentication.getAttributes().get("userInfo"), formDTO);
            return HttpResponse.created(dto);
        } catch (Exception e) {
            LOG.error(e.getMessage());
            return HttpResponse.badRequest().body(MentoringAPIError.builder()
                    .status(HttpStatus.BAD_REQUEST.getCode())
                    .error(e.getLocalizedMessage())
                    .message(e.getMessage()).build());
        }
    }


    @Operation(summary = "Update the form LifeCicleStatus")
    @ApiResponse(content = @Content(mediaType = MediaType.APPLICATION_JSON))
    @Patch("/changeLifeCicleStatus")
    @Tag(name = "Form")
    public FormDTO changeLifeCicleStatus(@NonNull @Body FormDTO formDTO, Authentication authentication){
        Form f = this.formService.updateLifeCycleStatus(formDTO.toForm(), (Long) authentication.getAttributes().get("userInfo"));
        return new FormDTO(f);
    }

    @Operation(summary = "Return a list of forms for the listed params")
    @ApiResponse(content = @Content(mediaType = MediaType.APPLICATION_JSON))
    @Get("/getall")
    @Tag(name = "Form")
    public List<FormDTO> getFormsByPartner(@Nullable @QueryValue("partnerId") Long partnerId){
        return this.formService.getFormsByPartnerId(partnerId);
    }

}
