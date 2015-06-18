package org.msh.tb.ng.cases;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Transactional;
import org.msh.tb.application.App;
import org.msh.tb.cases.CaseHome;
import org.msh.tb.cases.TbContactHome;
import org.msh.tb.entities.TbCase;
import org.msh.tb.entities.TbContact;
import org.msh.tb.ng.entities.TbContactNG;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Editing of contacts in a new notification form
 * Created by rmemoria on 17/6/15.
 */
@Name("contactsActionNG")
@Scope(ScopeType.CONVERSATION)
public class ContactsActionNG {
    private List<TbContact> contacts = new ArrayList<TbContact>();

    @In CaseHome caseHome;
    @In EntityManager entityManager;

    private Integer removeIndex;

    /**
     * Return the list of contacts being edited
     * @return
     */
    public List<TbContact> getContacts() {
        if (contacts == null) {
            contacts = new ArrayList<TbContact>();
            Collections.copy(contacts, caseHome.getInstance().getContacts());
        }
        return contacts;
    }


    /**
     * Add a new contact to the list of contacts being edited
     * @return List of {@link TbContact}
     */
    public TbContact newContact() {
        TbContact cont = new TbContactNG();
        getContacts().add(cont);
        return cont;
    }

    /**
     * Remove a contact from the list of contacts
     * @param cont
     */
    public void removeContact(TbContact cont) {
        getContacts().remove(cont);
    }

    public void remove() {
        if (removeIndex == null) {
            return;
        }
        getContacts().remove(removeIndex.intValue());
    }


    /**
     * Save the changes done to the list of contacts
     */
    @Transactional
    public void save() {
        TbCase tbcase = caseHome.getInstance();
        TbContactHome contactHome = (TbContactHome) App.getComponent("tbContactHome");

        contactHome.setTransactionLogActive(false);
        for (TbContact cont: getContacts()) {
            cont.setTbcase(tbcase);

            contactHome.setInstance(cont);
            contactHome.persist();
        }
    }

    public Integer getRemoveIndex() {
        return removeIndex;
    }

    public void setRemoveIndex(Integer removeIndex) {
        this.removeIndex = removeIndex;
    }
}
