package lemming.api.ui;

import org.apache.wicket.authorization.Action;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeAction;
import org.apache.wicket.markup.html.WebMarkupContainer;

/**
 * A markup container visible to users and admins.
 */
@AuthorizeAction(action = Action.RENDER, roles = { "USER", "ADMIN" })
public class UserWebMarkupContainer extends WebMarkupContainer {
    /**
     * Determines if a deserialized file is compatible with this class.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Creates a markup container.
     * 
     * @param id
     *            ID of a markup container
     */
    public UserWebMarkupContainer(String id) {
        super(id);
    }
}
