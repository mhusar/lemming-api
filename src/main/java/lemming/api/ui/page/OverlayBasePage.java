package lemming.api.ui.page;

import org.apache.wicket.ajax.IAjaxIndicatorAware;

/**
 * A base page with a header panel which displays a piece of markup when an Ajax
 * request is processing.
 */
public class OverlayBasePage extends BasePage implements IAjaxIndicatorAware {
    /**
     * Determines if a deserialized file is compatible with this class.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Creates an overlay base page.
     */
    public OverlayBasePage() {
        super();
    }

    /**
     * Returns the markup ID of the indicating element.
     * 
     * @return A markup ID attribute value.
     */
    @Override
    public String getAjaxIndicatorMarkupId() {
        return "overlay";
    }
}
