package manageBeans.crud;

import entity.Facultad;
import manageBeans.crud.util.JsfUtil;
import manageBeans.crud.util.PaginationHelper;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.util.ResourceBundle;
import javax.ejb.EJB;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.faces.model.SelectItem;
import sessionBeans.FacultadFacadeLocal;

@Named("facultadController")
@SessionScoped
public class FacultadController implements Serializable {

    private Facultad current;
    private DataModel items = null;
    @EJB
    private FacultadFacadeLocal ejbFacade;
    private PaginationHelper pagination;
    private int selectedItemIndex;

    public FacultadController() {
    }

    public Facultad getSelected() {
        if (current == null) {
            current = new Facultad();
            current.setFacultadPK(new entity.FacultadPK());
            selectedItemIndex = -1;
        }
        return current;
    }

    private FacultadFacadeLocal getFacade() {
        return ejbFacade;
    }

    public PaginationHelper getPagination() {
        if (pagination == null) {
            pagination = new PaginationHelper(200) {
                @Override
                public int getItemsCount() {
                    return getFacade().count();
                }

                @Override
                public DataModel createPageDataModel() {
                    return new ListDataModel(getFacade().findRange(new int[]{getPageFirstItem(), getPageFirstItem() + getPageSize()}));
                }
            };
        }
        return pagination;
    }

    public String prepareList() {
        recreateModel();
        return "List";
    }

    public String prepareView(Facultad vari) {
        current = vari;
        //selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return "View";
    }

    public String prepareCreate() {
        current = new Facultad();
        current.setFacultadPK(new entity.FacultadPK());
        selectedItemIndex = -1;
        return "Create";
    }

    public String create() {
        try {
            current.getFacultadPK().setIdUniversidad(current.getUniversidad().getIdUniversidad());
            getFacade().create(current);
            FacesContext facesContext = FacesContext.getCurrentInstance();
            facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Facultad creado", "Se ha creado una Facultad correctamente"));
            return prepareList();
        } catch (Exception e) {
            FacesContext facesContext = FacesContext.getCurrentInstance();
            facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "ERROR: Facultad no creado", "Lo sentimos, intentelo mas tarde"));
            return null;
        }
    }

    public String prepareEdit(Facultad var) {
        current = var;
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return "Edit";
    }

    public String update() {
        try {
            current.getFacultadPK().setIdUniversidad(current.getUniversidad().getIdUniversidad());
            getFacade().edit(current);
            FacesContext facesContext = FacesContext.getCurrentInstance();
            facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Facultad actualizado", "Se ha actualizado correctamente"));
            return "View";
        } catch (Exception e) {
            FacesContext facesContext = FacesContext.getCurrentInstance();
            facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error: Facultad no actualizado", "Lo sentimos, intentelo mas tarde"));

            return null;
        }
    }

    public String destroy(Facultad valor) {
        current = valor;
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        performDestroy();
        recreatePagination();
        recreateModel();
        return "List";
    }

    public String destroyAndView() {
        performDestroy();
        recreateModel();
        updateCurrentItem();
        if (selectedItemIndex >= 0) {
            return "View";
        } else {
            // all items were removed - go back to list
            recreateModel();
            return "List";
        }
    }

    private void performDestroy() {
        try {
            getFacade().remove(current);
            FacesContext facesContext = FacesContext.getCurrentInstance();
            facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Facultad eliminado", "Se ha eliminado una Facultad"));
        } catch (Exception e) {
            FacesContext facesContext = FacesContext.getCurrentInstance();
            facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "ERROR: Facultad no eliminado", "Lo sentimos, intentelo mas tarde"));
        }
    }

    private void updateCurrentItem() {
        int count = getFacade().count();
        if (selectedItemIndex >= count) {
            // selected index cannot be bigger than number of items:
            selectedItemIndex = count - 1;
            // go to previous page if last page disappeared:
            if (pagination.getPageFirstItem() >= count) {
                pagination.previousPage();
            }
        }
        if (selectedItemIndex >= 0) {
            current = getFacade().findRange(new int[]{selectedItemIndex, selectedItemIndex + 1}).get(0);
        }
    }

    public DataModel getItems() {
        if (items == null) {
            items = getPagination().createPageDataModel();
        }
        return items;
    }

    private void recreateModel() {
        items = null;
    }

    private void recreatePagination() {
        pagination = null;
    }

    public String next() {
        getPagination().nextPage();
        recreateModel();
        return "List";
    }

    public String previous() {
        getPagination().previousPage();
        recreateModel();
        return "List";
    }

    public SelectItem[] getItemsAvailableSelectMany() {
        return JsfUtil.getSelectItems(ejbFacade.findAll(), false);
    }

    public SelectItem[] getItemsAvailableSelectOne() {
        return JsfUtil.getSelectItems(ejbFacade.findAll(), true);
    }

    public Facultad getFacultad(entity.FacultadPK id) {
        return ejbFacade.find(id);
    }

    @FacesConverter(forClass = Facultad.class)
    public static class FacultadControllerConverter implements Converter {

        private static final String SEPARATOR = "#";
        private static final String SEPARATOR_ESCAPED = "\\#";

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            FacultadController controller = (FacultadController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "facultadController");
            return controller.getFacultad(getKey(value));
        }

        entity.FacultadPK getKey(String value) {
            entity.FacultadPK key;
            String values[] = value.split(SEPARATOR_ESCAPED);
            key = new entity.FacultadPK();
            key.setIdUniversidad(Integer.parseInt(values[0]));
            key.setNombreFacultad(values[1]);
            return key;
        }

        String getStringKey(entity.FacultadPK value) {
            StringBuilder sb = new StringBuilder();
            sb.append(value.getIdUniversidad());
            sb.append(SEPARATOR);
            sb.append(value.getNombreFacultad());
            return sb.toString();
        }

        @Override
        public String getAsString(FacesContext facesContext, UIComponent component, Object object) {
            if (object == null) {
                return null;
            }
            if (object instanceof Facultad) {
                Facultad o = (Facultad) object;
                return getStringKey(o.getFacultadPK());
            } else {
                throw new IllegalArgumentException("object " + object + " is of type " + object.getClass().getName() + "; expected type: " + Facultad.class.getName());
            }
        }
    }
}
