package mz.org.fgh.mentoring.auth;

import io.micronaut.http.HttpRequest;
import io.micronaut.security.authentication.AuthenticationException;
import io.micronaut.security.authentication.AuthenticationFailed;
import io.micronaut.security.authentication.AuthenticationProvider;
import io.micronaut.security.authentication.AuthenticationRequest;
import io.micronaut.security.authentication.AuthenticationResponse;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import jakarta.inject.Singleton;
import mz.org.fgh.mentoring.entity.user.User;
import mz.org.fgh.mentoring.repository.user.UserRepository;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

@Singleton
public class MentoringAuthenticationProvider implements AuthenticationProvider {
    private static final Logger LOG = LoggerFactory.getLogger(MentoringAuthenticationProvider.class);

    final UserRepository users;

    public MentoringAuthenticationProvider(UserRepository users) {
        this.users = users;
    }

    @Override
    public Publisher<AuthenticationResponse> authenticate(HttpRequest<?> httpRequest, AuthenticationRequest<?, ?> authenticationRequest) {
        final String identity = (String) authenticationRequest.getIdentity();

        LOG.debug("User {} tries to login...", identity);

        return Flowable.create(emitter -> {
            Optional<User> possibleUser = users.findByUsername(identity);

            if (possibleUser.isPresent()) {

                String secret = (String) authenticationRequest.getSecret();

                if (possibleUser.get().getPassword().equals(secret)) {
                    this.getRelatedUserIndivudual(possibleUser.get());
                    LOG.debug("User {} logged in...", identity);
                    emitter.onNext(AuthenticationResponse.success((String) identity, Collections.singletonList("USER_ROLE")));
                    emitter.onComplete();
                    return;
                } else {
                    LOG.debug("Wrong password for user {} ...", identity);
                }
            } else {
                LOG.debug("No user {} found in the system...", identity);
            }
            emitter.onError(new AuthenticationException(new AuthenticationFailed("Wrong username or password")));
        }, BackpressureStrategy.ERROR);
    }

    private void getRelatedUserIndivudual(User user) {

    }

    private Collection<String> getAutorities(User user) {
        return null;
    }
}
