package mz.org.fgh.mentoring.service.session;

import io.micronaut.security.token.jwt.generator.JwtTokenGenerator;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import mz.org.fgh.mentoring.dto.session.SessionRecommendedResourceDTO;
import mz.org.fgh.mentoring.entity.session.Session;
import mz.org.fgh.mentoring.entity.session.SessionRecommendedResource;
import mz.org.fgh.mentoring.entity.tutor.Tutor;
import mz.org.fgh.mentoring.entity.tutored.Tutored;
import mz.org.fgh.mentoring.entity.user.User;
import mz.org.fgh.mentoring.repository.session.SessionRecommendedResourceRepository;
import mz.org.fgh.mentoring.service.tutor.TutorService;
import mz.org.fgh.mentoring.service.tutored.TutoredService;
import mz.org.fgh.mentoring.service.user.UserService;
import mz.org.fgh.mentoring.util.DateUtils;
import mz.org.fgh.mentoring.util.LifeCycleStatus;
import mz.org.fgh.mentoring.util.Utilities;
import mz.org.fgh.util.EmailService;

import javax.mail.MessagingException;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Singleton
public class SessionRecommendedResourceService {

    @Inject
    private SessionService sessionService;
    @Inject
    private TutoredService tutoredService;
    @Inject
    private TutorService tutorService;
    @Inject
    private UserService userService;
    @Inject
    private EmailService emailService;
    @Inject
    private JwtTokenGenerator jwtTokenGenerator;

    private final SessionRecommendedResourceRepository sessionRecommendedResourceRepository;

    private static final String BASE_URL = System.getenv("BASE_URL_BACKEND");

    public SessionRecommendedResourceService(SessionRecommendedResourceRepository sessionRecommendedResourceRepository) {
        this.sessionRecommendedResourceRepository = sessionRecommendedResourceRepository;
    }

    @Transactional
    public SessionRecommendedResource save(SessionRecommendedResource sessionRecommendedResource, Long userId) {
        User user = userService.findById(userId);
        sessionRecommendedResource.setCreatedAt(DateUtils.getCurrentDate());
        sessionRecommendedResource.setCreatedBy(user.getUuid());
        sessionRecommendedResource.setLifeCycleStatus(LifeCycleStatus.ACTIVE);
        return sessionRecommendedResourceRepository.save(sessionRecommendedResource);
    }

    @Transactional
    public void delete(Long id) {
        sessionRecommendedResourceRepository.deleteById(id);
    }

    @Transactional
    public List<SessionRecommendedResource> findAll() {
        Iterable<SessionRecommendedResource> iterable = sessionRecommendedResourceRepository.findAll();
        List<SessionRecommendedResource> list = new ArrayList<>();
        iterable.forEach(list::add);
        return list;
    }

    @Transactional
    public SessionRecommendedResource findByTutoredAndResourceLink(Long tutoredId, String resourceLink) {
        return sessionRecommendedResourceRepository.findByTutoredIdAndResourceLink(tutoredId, resourceLink);
    }

    public SessionRecommendedResource createEntityFromDto(SessionRecommendedResourceDTO dto) {
        Session session = sessionService.findByUuid(dto.getSessionUuid());
        Tutored tutored = tutoredService.findByUuid(dto.getTutoredUuid());
        Tutor tutor = tutorService.findByUuid(dto.getTutorUuid());

        return new SessionRecommendedResource(dto, session, tutored, tutor);
    }

    @Transactional
    public SessionRecommendedResource updateByUuid(String uuid, SessionRecommendedResource updatedResource, Long userId) {
        User user = userService.findById(userId);
        return sessionRecommendedResourceRepository.findByUuid(uuid).map(existingResource -> {
            existingResource.setResourceLink(updatedResource.getResourceLink());
            existingResource.setResourceName(updatedResource.getResourceName());
            existingResource.setSession(updatedResource.getSession());
            existingResource.setTutored(updatedResource.getTutored());
            existingResource.setTutor(updatedResource.getTutor());
            existingResource.setDateRecommended(updatedResource.getDateRecommended());
            existingResource.setNotificationDate(updatedResource.getNotificationDate());
            existingResource.setNotificationStatus(updatedResource.getNotificationStatus());
            existingResource.setUpdatedAt(DateUtils.getCurrentDate());
            existingResource.setUpdatedBy(user.getUuid());
            return sessionRecommendedResourceRepository.update(existingResource);
        }).orElseThrow(() -> new IllegalArgumentException("No SessionRecommendedResource found with UUID: " + uuid));
    }

    @Transactional
    public SessionRecommendedResource findByTutoredUuidAndResourceLink(String tutoredUuid, String resourceLink) {
        return sessionRecommendedResourceRepository.findByTutoredUuidAndResourceLink(tutoredUuid, resourceLink)
                .orElseThrow(() -> new IllegalArgumentException("No SessionRecommendedResource found for the provided tutored UUID and resource link"));
    }

    @Transactional
    public void processPendingResources() throws MessagingException {
        List<SessionRecommendedResource> pendingResources = sessionRecommendedResourceRepository.findByNotificationStatus(SessionRecommendedResource.NotificationStatus.PENDING);
        if (!Utilities.listHasElements(pendingResources)) return;

        try {
        for (SessionRecommendedResource resource : pendingResources) {

            String htmlTampleteResult = emailService.loadHtmlTemplate("emailNotificationRecomendeResource");

            Map<String, Object> attributes = new HashMap<>();
            attributes.put("nuit", resource.getTutored().getEmployee().getNuit());

            String tokenForTutored = this.jwtTokenGenerator.generateToken(attributes).get();
            resource.setToken(tokenForTutored);

            Map<String, String> variables = new HashMap<>();

            variables.put("menteesName", resource.getTutored().getEmployee().getFullName());
            variables.put("mentorName", resource.getTutor().getEmployee().getFullName());
            variables.put("link",BASE_URL+"/resources/load/documento?nuit="+resource.getTutored().getEmployee().getNuit()+"&token="+resource.getToken()+"&fileName="+resource.getResourceName());

            String populationHtml = emailService.populateTemplateVariables(htmlTampleteResult, variables);

            emailService.sendEmail(resource.getTutored().getEmployee().getEmail(), "Acesso aos Recursos no Mentoria Web", populationHtml);

            resource.setNotificationStatus(SessionRecommendedResource.NotificationStatus.SENT);
            sessionRecommendedResourceRepository.update(resource); // Update the status to SENT
        }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<SessionRecommendedResource> saveMany(List<SessionRecommendedResource> resources, Long userId) {
        List<SessionRecommendedResource> list = new ArrayList<>();
        for (SessionRecommendedResource resource : resources) {
            list.add(this.save(resource, userId));
        }
        return list;
    }
}
