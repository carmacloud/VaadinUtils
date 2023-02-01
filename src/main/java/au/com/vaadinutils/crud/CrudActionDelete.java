package au.com.vaadinutils.crud;

import org.apache.logging.log4j.Logger;

import com.vaadin.addon.jpacontainer.EntityItem;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;

import au.com.vaadinutils.flow.helper.VaadinHelper;
import au.com.vaadinutils.flow.helper.VaadinHelper.NotificationType;

/**
 * @deprecated Replaced in V14 migration.
 */
public class CrudActionDelete<E extends CrudEntity> implements CrudAction<E> {
    private static final long serialVersionUID = 1L;
    private boolean isDefault = true;
    private DeleteAction<E> action;
    private String message;

    Logger logger = org.apache.logging.log4j.LogManager.getLogger();

    public CrudActionDelete() {
        this("");
    }

    /**
     * 
     * @param message - provide additional information to be displayed in the delete
     *                confirm dialog
     */
    public CrudActionDelete(String message) {
        this(message, null);
    }

    /**
     * 
     * @param message - provide additional information to be displayed in the delete
     *                confirm dialog
     * @param action  - perform additional cleanup like deleting files, called
     *                before the entity is deleted.
     */
    public CrudActionDelete(String message, DeleteAction<E> action) {
        this.action = action;
        this.message = message;
    }

    @Override
    public void exec(final BaseCrudView<E> crud, final EntityItem<E> entity) {
        if (entity == null || entity.getEntity() == null) {
            VaadinHelper.notificationDialog("No record selected to delete.", NotificationType.ERROR);
            return;
        }
        DeleteVetoResponseData response = crud.canDelete(entity.getEntity());
        if (response.canDelete) {

            String titleText = crud.getTitleText();
            if (titleText.contains("getTitleText()")) {
                titleText = entity.getEntity().getClass().getSimpleName() + " (getTitleText() not implemented)";
            }
            String name = entity.getEntity().getName();
            if (name == null) {
                name = "" + entity.getEntity().getId();
            }
            new ConfirmDialog("Confirm Delete",
                    "Are you sure you want to delete " + titleText + " - '" + name + "' ? " + message, "Delete",
                    delete -> {
                        if (action != null) {
                            try {
                                action.delete(entity);
                            } catch (Exception e) {
                                logger.error(e, e);
                                VaadinHelper.notificationDialog("Errors occurred when deleting " + e.getMessage(),
                                        NotificationType.ERROR);
                            }
                        }
                        crud.delete();
                    }, "Cancel", cancel -> {
                    }).open();
        } else {
            VaadinHelper.notificationDialog(response.getMessage(), NotificationType.ERROR);

        }

    }

    @Override
    public String toString() {
        return "Delete";
    }

    @Override
    public boolean isDefault() {
        return isDefault;
    }

    public void setIsDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }

    @Override
    public boolean showPreparingDialog() {
        return false;
    }
}
