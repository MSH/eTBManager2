package org.msh.etbm.services.pub;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.international.Messages;
import org.msh.tb.application.EtbmanagerApp;
import org.msh.tb.application.mail.MailService;
import org.msh.tb.entities.SystemConfig;
import org.msh.tb.entities.User;
import org.msh.tb.entities.UserWorkspace;
import org.msh.tb.entities.enums.UserState;
import org.msh.tb.entities.enums.UserView;
import org.msh.utils.UserUtils;

import javax.persistence.EntityManager;
import java.util.Date;

/**
 * Handle self registration in the public area of e-TB Manager.
 *
 * Created by ricardo on 24/11/14.
 */
@Name("userRegistrationService")
public class UserRegistrationService {

    @In(create=true)
    EtbmanagerApp etbmanagerApp;

    @In
    EntityManager entityManager;

    /**
     * Register a new user in the system. This service is not available if configuration is not enabled
     * to do so
     * @return
     */
    public void register(String name, String login, String email, String organization) {
        SystemConfig systemConfig = etbmanagerApp.getConfiguration();

        if (!systemConfig.isAllowRegPage()) {
            throw new RuntimeException("User registration in public area is not allowed");
        }

        email = email.trim();

        // validate e-mail address
        if (UserUtils.isValidEmail(email)) {
            throw new RuntimeException(Messages.instance().get("validator.email"));
        }

        User user = new User();
        user.setName( name.trim() );
        user.setLogin( login.trim() );
        user.setEmail( email );

        UserWorkspace userWs = new UserWorkspace();
        userWs.setPlayOtherUnits(false);
        userWs.setProfile(systemConfig.getUserProfile());
        userWs.setTbunit(systemConfig.getTbunit());
        userWs.setUser(user);
        userWs.setView(UserView.COUNTRY);
        userWs.setWorkspace(systemConfig.getWorkspace());

        user.setLogin(user.getLogin().toUpperCase());
        user.getWorkspaces().add(userWs);
        user.setRegistrationDate(new Date());
        user.setState(UserState.ACTIVE);

        String password = UserUtils.generateNewPassword();
        user.setPassword(UserUtils.hashPassword(password));
        Contexts.getEventContext().set("password", password);
        Contexts.getEventContext().set("user", user);

        entityManager.persist(user);
        entityManager.flush();

        MailService.instance().sendMessage("/mail/newuser.xhtml");
//        dmsystem.enviarEmail("newuser.xhtml");
    }
}
