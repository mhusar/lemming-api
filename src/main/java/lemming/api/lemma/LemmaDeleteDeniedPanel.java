package lemming.api.lemma;

import org.apache.wicket.Component;
import org.apache.wicket.authorization.Action;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeAction;
import org.apache.wicket.model.StringResourceModel;

import lemming.api.table.GenericDataTable;
import lemming.api.ui.panel.ModalMessagePanel;

/**
 * A panel containing a modal window dialog stating that a lemma could not be
 * deleted.
 */
@AuthorizeAction(action = Action.RENDER, roles = { "SIGNED_IN" })
public class LemmaDeleteDeniedPanel extends ModalMessagePanel {
    /**
     * Determines if a deserialized file is compatible with this class.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Creates a panel.
     * 
     * @param id
     *            ID of the panel
     */
    public LemmaDeleteDeniedPanel(String id) {
        super(id, DialogType.OKAY);
    }

    /**
     * Creates a panel.
     * 
     * @param id
     *            ID of the panel
     * @param dataTable
     *            data table that is refreshed
     */
    public LemmaDeleteDeniedPanel(String id, GenericDataTable<Lemma> dataTable) {
        super(id, DialogType.OKAY, dataTable);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTitleString() {
        return getString("LemmaDeleteDeniedPanel.title");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public StringResourceModel getMessageModel() {
        Lemma lemma = (Lemma) getDefaultModelObject();

        return new StringResourceModel("LemmaDeleteDeniedPanel.message",
                (Component) this, getDefaultModel()).setParameters("<b>" + lemma.getName() + "</b>");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getConfirmationString() {
        return getString("LemmaDeleteDeniedPanel.confirm");
    }

    /**
     * Does nothing.
     */
    @Override
    public void onCancel() {
    }

    /**
     * Does nothing.
     */
    @Override
    public void onConfirm() {
    }
}
