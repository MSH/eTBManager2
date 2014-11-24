package org.msh.etbm.services.pub;

import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;
import org.msh.tb.application.mail.MailService;
import org.msh.tb.entities.User;
import org.msh.tb.misc.DmSystemHome;
import org.msh.utils.UserUtils;

import javax.persistence.EntityManager;
import java.util.List;

/**
 * Send a new password to the user. The user is identified by a given e-mail address
 * assigned to its account
 *
 * Created by ricardo on 24/11/14.
 */
@Name("sendNewPasswordService")
@AutoCreate
public class SendNewPasswordService {

    @In(create=true) EntityManager entityManager;
    @In(create=true) DmSystemHome dmsystem;

    /**
     * Send a new password to the user
     * @param email user e-mail address
     * @return true if new password was generated and sent to the user
     */
    @Transactional
    public boolean execute(String email) {
        List<User> users = entityManager.createQuery("from User u where u.email = :email")
                .setParameter("email", email)
                .getResultList();

        if (users.size() == 0) {
            return false;
        }

        User user = users.get(0);

        String password = UserUtils.generateNewPassword();
        user.setPassword(UserUtils.hashPassword(password));

        MailService mailer = MailService.instance();

        mailer.addComponent("newpassword", password);
        mailer.addComponent("puser", user);
        mailer.sendMessage("/mail/forgotpassword.xhtml");

        entityManager.persist(user);
        entityManager.flush();

        return true;
    }
}
