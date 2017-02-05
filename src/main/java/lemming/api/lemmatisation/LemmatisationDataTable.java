package lemming.api.lemmatisation;

import lemming.api.context.SelectableContextDataProvider;
import lemming.api.context.SelectableContextWrapper;
import lemming.api.table.NavigationToolbar;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.HeadersToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.NoRecordsToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.table.filter.FilterForm;
import org.apache.wicket.extensions.markup.html.repeater.data.table.filter.FilterToolbar;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;

import java.util.List;

/**
 * A custom data table with toolbars and data provider for context lemmatisation.
 */
public class LemmatisationDataTable extends DataTable<SelectableContextWrapper, String> {
    /**
     * Determines if a deserialized file is compatible with this class.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Default rows per page.
     */
    private static final long DEFAULT_ROWS_PER_PAGE = 100;

    /**
     * Creates a new data table with toolbars.
     *
     * @param id
     *            ID of a data table
     * @param columns
     *            list of columns
     * @param dataProvider
     *            provides data for a table
     */
    public LemmatisationDataTable(String id, List<IColumn<SelectableContextWrapper, String>> columns,
                                  SelectableContextDataProvider dataProvider) {
        super(id, columns, dataProvider, DEFAULT_ROWS_PER_PAGE);
        createTable(dataProvider, null);
    }

    /**
     * Creates a new data table with toolbars.
     *
     * @param id
     *            ID of a data table
     * @param columns
     *            list of columns
     * @param dataProvider
     *            provides data for a table
     * @param filterForm
     *            form that filters data of a table
     */
    public LemmatisationDataTable(String id, List<IColumn<SelectableContextWrapper, String>> columns,
                                  SelectableContextDataProvider dataProvider,
                                  FilterForm<SelectableContextWrapper> filterForm) {
        super(id, columns, dataProvider, DEFAULT_ROWS_PER_PAGE);
        createTable(dataProvider, filterForm);
    }

    /**
     * Builds a new data table with toolbars.
     *
     * @param dataProvider
     *            provides data for a table
     * @param filterForm
     *            form that filters data of a table
     */
    private void createTable(SelectableContextDataProvider dataProvider,
                             FilterForm<SelectableContextWrapper> filterForm) {
        setOutputMarkupId(true);
        add(AttributeModifier.append("class", "table table-hover table-striped"));
        addTopToolbar(new NavigationToolbar<SelectableContextWrapper>(this));
        addTopToolbar(new HeadersToolbar<String>(this, dataProvider));
        addBottomToolbar(new NoRecordsToolbar(this));

        if (filterForm instanceof FilterForm) {
            addTopToolbar(new FilterToolbar(this, filterForm));
        }
    }

    @Override
    protected Item<SelectableContextWrapper> newRowItem(String id, int index, IModel<SelectableContextWrapper> model) {
        Item<SelectableContextWrapper> rowItem = super.newRowItem(id, index, model);
        rowItem.setOutputMarkupId(true);
        return rowItem;
    }
}
