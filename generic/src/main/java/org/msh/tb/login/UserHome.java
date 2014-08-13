package org.msh.tb.login;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.faces.FacesMessages;
import org.msh.tb.EntityHomeEx;
import org.msh.tb.entities.*;
import org.msh.tb.entities.enums.UserState;
import org.msh.tb.entities.enums.UserView;
import org.msh.tb.laboratories.LaboratorySelection;
import org.msh.tb.misc.DmSystemHome;
import org.msh.tb.tbunits.TBUnitSelection;
import org.msh.tb.transactionlog.LogInfo;
import org.msh.tb.userprofile.UserProfilesQuery;
import org.msh.utils.Passwords;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;



@Name("userHome")
@LogInfo(roleName="USERS", entityClass=User.class)
public class UserHome extends EntityHomeEx<User> {
	private static final long serialVersionUID = -1722666162646162093L;
	
	@In(create=true) DmSystemHome dmsystem;
	@In(create=true) UserProfilesQuery profiles;
	@In(required=true) UserLogin userLogin;
	@In(create=true) Map<String, String> messages;
	@In(create=true) FacesMessages facesMessages;
	
	private UserWorkspace userworkspace;
	private boolean sendEmail = true;
	private String password;

	private List<SelectItem> views;
	private String selectedView;
	
	private TBUnitSelection tbunitselection;
    private LaboratorySelection labselection;

	@Factory("user")
	public User getUserInstance() {
		return (User)getInstance();
	}



	/* (non-Javadoc)
	 * @see com.rmemoria.utils.EntityHomeEx#persist()
	 */
	@Override
	public String persist() {
		User user = getInstance();
		UserWorkspace uw = getUserWorkspace();
		
		uw.setTbunit(getTbunitselection().getSelected());
		
		UserWorkspace loginUserWorkspace = (UserWorkspace)Component.getInstance("userWorkspace");
		if (loginUserWorkspace.getHealthSystem() != null)
			uw.setHealthSystem(loginUserWorkspace.getHealthSystem());

		// get user view
		convertView();

        // set user's laboratory
        if (labselection != null) {
            uw.setLaboratory( labselection.getLaboratory() );
        }

		// is a new user ?
		if (!isManaged()) {
			
			Long count = (Long) getEntityManager().createQuery("select count(*) from User where upper(email) = :email")
				.setParameter("email", user.getEmail().toUpperCase())
				.getSingleResult();

			if (count > 0) {
				facesMessages.addFromResourceBundle("form.duplicatedemail");
				return "error";
			}
			
			user.setParentUser(getUser());
			user.setRegistrationDate(new Date());
			user.setState(UserState.PASSWD_EXPIRED);

			// gera senha para o novo usu�rio
			String senha = null;
			if (password != null)
				 senha = password;
			else senha = Passwords.generateNewPassword();
			
			user.setPassword(Passwords.hashPassword(senha));
			Contexts.getEventContext().set("password", senha);
			enviaMailNovoUsuario();

			user.getWorkspaces().add(getUserWorkspace());
			getUserWorkspace().setUser(user);
		}
		else {
			// update the user info in the log
			getEntityManager().createQuery("update UserLog set name = :name where id = :id")
				.setParameter("name", user.getName())
				.setParameter("id", user.getId())
				.executeUpdate();
		}
		
		user.setEmail(user.getEmail().trim());
		user.setLogin(user.getLogin().toUpperCase());
		String ret = super.persist();

		// is there a default workspace ?
		if (user.getDefaultWorkspace() == null) {
			user.setDefaultWorkspace(getUserWorkspace());
			getEntityManager().persist(user);
			getEntityManager().flush();
		}

		return ret;
	}
	

	
	/**
	 * Valida a conta do usu�rio
	 * @param context
	 * @param compConta
	 * @param value
	 */
	public void validaConta(FacesContext context, UIComponent compConta, Object value) {
		UserValidators validators = (UserValidators)Component.getInstance("userValidators");
		validators.validateLogin(context, compConta, value, (Integer)getId());
/*		String conta = (String) value;

		if ((conta.indexOf(" ") >= 0) || (conta.length() < 4)) {
			((UIInput)compConta).setValid(false);

			FacesMessage message = new FacesMessage(messages.get("admin.users.reqlogin"));
			context.addMessage(compConta.getClientId(context), message);			
		}
		
		List<User> lst = getEntityManager()
				.createQuery("from User u where u.login = :conta")
				.setParameter("conta", conta.toUpperCase())
				.getResultList();

		User user = getUserInstance();
		if ((lst.size() > 0) && ((user.getId() == null) || (lst.get(0) != user))) {
			((UIInput)compConta).setValid(false);

			FacesMessage message = new FacesMessage(messages.get("admin.users.uniquelogin"));
			context.addMessage(compConta.getClientId(context), message);
		}
*/	}

	@Override
	public String remove() {
		UserLogin userLogin = getUserLogin();
		if (userLogin.getUser().equals(getInstance())) {
			facesMessages.addFromResourceBundle("admin.users.delerror1");
			return "error";
		}
		
		UsersQuery users = (UsersQuery)Component.getInstance("users", false);
		if (users != null) {
			UserWorkspace uw = users.getInstanceByUser(getInstance());
			if (uw != null)
				users.getResultList().remove(uw);
		}
		
		return super.remove();
	}


	
	public void enviaMailNovoUsuario() {
		if (!sendEmail)
			return;
		if (dmsystem.enviarEmail("newuser.xhtml"))
			 facesMessages.addFromResourceBundle("mail.success", getUserInstance().getEmail());
		else facesMessages.addFromResourceBundle("mail.fail", getUserInstance().getEmail());
	}

		
	public String sendNewPassword() {
		String senha = Passwords.generateNewPassword();
		getUserInstance().setPassword(Passwords.hashPassword(senha));
		Contexts.getEventContext().set("password", senha);

		User user = getUserInstance();
		getLogService().saveExecuteTransaction("NEWPWD", user.getName(), user.getId(), User.class.getSimpleName(), user);
		
		if (!sendEmail)
			return "mail.success";
		
		if (dmsystem.enviarEmail("newpassword.xhtml")) {
			facesMessages.addFromResourceBundle("mail.success", getUserInstance().getEmail());
			getEntityManager().persist(getUserInstance());
			getEntityManager().flush();
			return "mail.success";
		}
		else {
			facesMessages.addFromResourceBundle("mail.fail", getUserInstance().getEmail());
			return "mail.fail";
		}
	}
	
	
	@Override
	public void setId(Object id) {
		super.setId(id);
	}
	
	public UserWorkspace getUserWorkspace() {
		if (userworkspace == null) {
			createUserWorkspace();
		}
		
		return userworkspace;
	}

	
	protected void createUserWorkspace() {
		Workspace ws = userLogin.getDefaultWorkspace();
		userworkspace = getInstance().getUserWorkspaceByWorkspace(ws);
		if (userworkspace == null) {
			userworkspace = new UserWorkspace();
			userworkspace.setUser(getInstance());
			userworkspace.setWorkspace(ws);
		}
		else getTbunitselection().setSelected(userworkspace.getTbunit());
	}	
	
	/**
	 * @return the sendEmail
	 */
	public boolean isSendEmail() {
		return sendEmail;
	}

	/**
	 * @param sendEmail the sendEmail to set
	 */
	public void setSendEmail(boolean sendEmail) {
		this.sendEmail = sendEmail;
	}

	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * @param password the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/* (non-Javadoc)
	 * @see org.jboss.seam.framework.Home#clearInstance()
	 */
	@Override
	public void clearInstance() {
		super.clearInstance();
		userworkspace = null;
	}
	
	public TBUnitSelection getTbunitselection() {
		if (tbunitselection == null) {
			tbunitselection = new TBUnitSelection("unitid");
//			tbunitselection.setHealthSystem(getUserWorkspace().getHealthSystem());
			getUserWorkspace();
		}
		return tbunitselection;
	}


	/**
	 * Select the options to the user view
	 * @return List of {@link SelectItem}
	 */
	public List<SelectItem> getViews() {
		if (views == null) {
			UserWorkspace uws = getUserWorkspace();
			UserView uv = uws.getView();
			
			views = new ArrayList<SelectItem>();

			// add no label workspace
			views.add(new SelectItem(null, "-"));
			
			// add workspace as the root
			Workspace ws = getWorkspace();
			views.add(new SelectItem("W", ws.getName().toString()));
			if (uv == UserView.COUNTRY)
				selectedView = views.get(views.size() - 1).getValue().toString();

			Tbunit unit = getTbunitselection().getSelected();
			if (unit != null) {
				List<AdministrativeUnit> lst = unit.getAdminUnit().getParentsTreeList(true);

				AdministrativeUnit admunit = uws.getAdminUnit();
				if (admunit == null)
					admunit = unit.getAdminUnit();

				for (AdministrativeUnit au: lst) { 
					views.add(new SelectItem("A" + au.getId().toString(), au.getCountryStructure().getName().toString() + ": " +
							au.getName().toString()));
		
					if ((uv == UserView.ADMINUNIT) && (admunit.equals(au)))
						selectedView = views.get(views.size() - 1).getValue().toString();
				}
				
				// add TB unit
				if (unit != null) {
					views.add(new SelectItem("T", unit.getName().toString()));
					if (uv == UserView.TBUNIT)
						selectedView = views.get(views.size() - 1).getValue().toString();
				}
			}
		}
		return views;
	}
	
	

	/**
	 * Refresh the view list
	 */
	public void refreshViews() {
		views = null;
	}

	/**
	 * @param selectedView the selectedView to set
	 */
	public void setSelectedView(String selectedView) {
		this.selectedView = selectedView;
	}

	/**
	 * @return the selectedView
	 */
	public String getSelectedView() {
		return selectedView;
	}
	
	public void convertView() {
		if (selectedView == null)
			return;
		
		UserWorkspace ws = getUserWorkspace();
		
		if (selectedView.equals("W"))
			ws.setView(UserView.COUNTRY);
		else
		if (selectedView.startsWith("A")) {
			int admid = Integer.valueOf(selectedView.substring(1));
			ws.setAdminUnit(getEntityManager().find(AdministrativeUnit.class, admid));
			ws.setView(UserView.ADMINUNIT);
		}
		else
		if (selectedView.equals("T"))
			ws.setView(UserView.TBUNIT);
	}



	/**
	 */
	public void refreshHealthSystem() {
		tbunitselection = null;
	}


    /**
     * Return the object for laboratory selection
     * @return instance of LaboratorySelection
     */
    public LaboratorySelection getLabselection() {
        if (labselection == null) {
            labselection = new LaboratorySelection("labid");
            labselection.setLaboratory(getUserWorkspace().getLaboratory());
        }
        return labselection;
    }
}
