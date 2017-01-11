package lemming.api.user;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.EnumChoiceRenderer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.ListChoice;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.validation.INullAcceptingValidator;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;

import lemming.api.HomePage;
import lemming.api.auth.SignInPage;
import lemming.api.auth.UserRoles;
import lemming.api.auth.WebSession;
import lemming.api.ui.panel.ModalMessagePanel;

/**
 * A form for editing users.
 */
public class UserEditForm extends Form<User> {
    /**
     * Determines if a deserialized file is compatible with this class.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Creates a new user edit form.
     * 
     * @param id
     *            ID of a user edit form
     * @param model
     *            edited user model
     */
    public UserEditForm(String id, IModel<User> model) {
        super(id, model);
    }

    /**
     * Called when a user edit form is initialized.
     */
    @Override
    protected void onInitialize() {
        super.onInitialize();

        TextField<String> realNameTextField = new RequiredTextField<String>("realName");
        TextField<String> usernameTextField = new RequiredTextField<String>("username");
        ListChoice<UserRoles.Role> roleListChoice = new ListChoice<UserRoles.Role>("role",
                new PropertyModel<UserRoles.Role>(getModelObject(), "role"),
                new ArrayList<UserRoles.Role>(Arrays.asList(UserRoles.Role.values())),
                new EnumChoiceRenderer<UserRoles.Role>(), 3);

        add(realNameTextField.setOutputMarkupId(true));
        add(usernameTextField);
        add(new PasswordTextField("password").setResetPassword(false).setRequired(true));
        add(new CancelButton("cancelButton"));

        if (WebSession.get().getUser().getRole().equals(UserRoles.Role.ADMIN)) {
            CheckBox enabledCheckBox = new CheckBox("enabled");

            add(roleListChoice);
            add(enabledCheckBox);
            add(new DeleteButton("deleteButton", getModel()).setVisible(!(isUserTransient(getModelObject()))));

            if (isUserTransient(getModelObject())) {
                enabledCheckBox.add(AttributeModifier.append("checked","checked"));
            }

            roleListChoice.add(new RequiredRoleValidator());
        }

        realNameTextField.add(new UniqueRealNameValidator(getModel()));
        usernameTextField.add(new UniqueUsernameValidator(getModel()));
    }

    /**
     * Checks if a user is transient.
     * 
     * @param user
     *            user that is checked
     * @return True if a user is transient; false otherwise.
     */
    private Boolean isUserTransient(User user) {
        return new UserDao().isTransient(user);
    }

    /**
     * Called on form submit.
     */
    @Override
    protected void onSubmit() {
        UserDao userDao = new UserDao();
        User user = getModelObject();
        Integer userId = user.getId();
        Boolean passwordChanged = true;

        if (userId instanceof Integer) {
            User persistentUser = userDao.find(userId);

            if (persistentUser instanceof User) {
                passwordChanged = (user.getPassword().equals(persistentUser.getPassword())) ? false : true;
            }
        }

        if (passwordChanged) {
            try {
                byte[] saltBytes = userDao.createRandomSaltBytes();
                String hashedPassword = userDao.hashPassword(user.getPassword(), saltBytes);

                user.setPassword(hashedPassword);
                user.setSalt(saltBytes);
            } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            }
        }

        if (userDao.isTransient(user)) {
            userDao.persist(user);
        } else {
            userDao.merge(user);
        }

        if (WebSession.get().getUser().getRole().equals(UserRoles.Role.ADMIN)) {
            setResponsePage(UserEditPage.class);
        } else {
            setResponsePage(HomePage.class);
        }
    }

    /**
     * A button which cancels the editing of a user.
     */
    private final class CancelButton extends AjaxLink<User> {
        /**
         * Determines if a deserialized file is compatible with this class.
         */
        private static final long serialVersionUID = 1L;

        /**
         * Creates a new cancel button.
         * 
         * @param id
         *            ID of the button
         */
        public CancelButton(String id) {
            super(id);
        }

        /**
         * Called on button click.
         * 
         * @param target
         *            target that produces an Ajax response
         */
        @Override
        public void onClick(AjaxRequestTarget target) {
            if (WebSession.get().getUser().getRole().equals(UserRoles.Role.ADMIN)) {
                setResponsePage(UserEditPage.class);
            } else {
                setResponsePage(HomePage.class);
            }
        }
    }

    /**
     * A button which deletes a user.
     */
    private final class DeleteButton extends AjaxLink<User> {
        /**
         * Determines if a deserialized file is compatible with this class.
         */
        private static final long serialVersionUID = 1L;

        /**
         * Creates a new delete button.
         * 
         * @param id
         *            ID of the button
         * @param model
         *            model which is deleted by the button
         */
        private DeleteButton(String id, IModel<User> model) {
            super(id, model);
        }

        /**
         * Called on button click.
         * 
         * @param target
         *            target that produces an Ajax response
         */
        @Override
        public void onClick(AjaxRequestTarget target) {
            ModalMessagePanel panel = (ModalMessagePanel) getPage().get("userDeleteDeniedPanel");

            if (getModelObject().equals(WebSession.get().getUser())) {
                new UserDao().remove(getModelObject());
                WebSession.get().invalidate();
                setResponsePage(SignInPage.class);
            } else {
                new UserDao().remove(getModelObject());
                setResponsePage(UserEditPage.class);
            }
        }
    }

    /**
     * Checks if a user has a role.
     */
    private class RequiredRoleValidator implements
            INullAcceptingValidator<UserRoles.Role> {
        /**
         * Determines if a deserialized file is compatible with this class.
         */
        private static final long serialVersionUID = 1L;

        /**
         * Validates value of a form component.
         * 
         * @param validatable
         *            IValidatable instance that is validated
         */
        @Override
        public void validate(IValidatable<UserRoles.Role> validatable) {
            ValidationError error = new ValidationError();

            if (!(validatable.getValue() instanceof UserRoles.Role)) {
                error.addKey("UserEditForm.role-is-required");
            }

            if (!(error.getKeys().isEmpty())) {
                validatable.error(error);
            }
        }
    }

    /**
     * Validates a users’s realName against other existent users.
     */
    private class UniqueRealNameValidator implements IValidator<String> {
        /**
         * Determines if a deserialized file is compatible with this class.
         */
        private static final long serialVersionUID = 1L;

        /**
         * User model that is edited.
         */
        private IModel<User> userModel;

        /**
         * Creates a new realName validator.
         * 
         * @param model
         *            user model that is edited
         */
        public UniqueRealNameValidator(IModel<User> model) {
            userModel = model;
        }

        /**
         * Validates value of a form component.
         * 
         * @param validatable
         *            IValidatable instance that is validated
         */
        @Override
        public void validate(IValidatable<String> validatable) {
            ValidationError error = new ValidationError();
            UserDao userDao = new UserDao();
            User user = userDao.findByRealName(validatable.getValue());

            if (userDao.isTransient(userModel.getObject())) {
                if (user instanceof User) {
                    error.addKey("UserEditForm.realName-is-non-unique");
                }
            } else if (user instanceof User) {
                if (!(user.equals(userModel.getObject()))) {
                    error.addKey("UserEditForm.realName-is-non-unique");
                }
            }

            if (!(error.getKeys().isEmpty())) {
                validatable.error(error);
            }
        }
    }

    /**
     * Validates a users’s username against other existent users.
     */
    private class UniqueUsernameValidator implements IValidator<String> {
        /**
         * Determines if a deserialized file is compatible with this class.
         */
        private static final long serialVersionUID = 1L;

        /**
         * User model that is edited.
         */
        private IModel<User> userModel;

        /**
         * Creates a new username validator.
         * 
         * @param model
         *            user model that is edited
         */
        public UniqueUsernameValidator(IModel<User> model) {
            userModel = model;
        }

        /**
         * Validates the value of a form component.
         * 
         * @param validatable
         *            IValidatable instance that is validated
         */
        @Override
        public void validate(IValidatable<String> validatable) {
            ValidationError error = new ValidationError();
            UserDao userDao = new UserDao();
            User user = userDao.findByUsername(validatable.getValue());

            if (userDao.isTransient(userModel.getObject())) {
                if (user instanceof User) {
                    error.addKey("UserEditForm.username-is-non-unique");
                }
            } else if (user instanceof User) {
                if (!(user.equals(userModel.getObject()))) {
                    error.addKey("UserEditForm.username-is-non-unique");
                }
            }

            if (!(error.getKeys().isEmpty())) {
                validatable.error(error);
            }
        }
    }
}