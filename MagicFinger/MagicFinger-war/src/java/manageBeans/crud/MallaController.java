package manageBeans.crud;

import entity.Malla;
import manageBeans.crud.util.JsfUtil;
import manageBeans.crud.util.PaginationHelper;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.util.ResourceBundle;
import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import manageBeans.LoginSessionMB;
import sessionBeans.MallaFacadeLocal;

@Named("mallaController")
@RequestScoped
public class MallaController implements Serializable {

    private Malla current;
    private DataModel items = null;
    @EJB
    private MallaFacadeLocal ejbFacade;
    private PaginationHelper pagination;
    private int selectedItemIndex;
    
    @Inject
    LoginSessionMB session;
    
    public MallaController() {
    }

    public Malla getSelected() {
        if (current == null) {
            current = new Malla();
            selectedItemIndex = -1;
        }
        return current;
    }

    private MallaFacadeLocal getFacade() {
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

    public String prepareView(Malla vari) {
        current = vari;
        //selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return "View";
    }

    public String prepareCreate() {
        current = new Malla();
        selectedItemIndex = -1;
        return "Create";
    }

    public String create() {
        try {
            getFacade().create(current);
            FacesContext facesContext = FacesContext.getCurrentInstance();
            facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Malla creado", "Se ha creado una Malla correctamente"));
            return prepareList();
        } catch (Exception e) {
            FacesContext facesContext = FacesContext.getCurrentInstance();
             
            facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Malla no creada", "Lo sentimos, los datos que ingresó ya existen"));
            return null;
        }
    }

    public String prepareEdit(Malla var) {
        current = var;
        //selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return "Edit";
    }

    public String update() {
        try {
            getFacade().edit(current);
            FacesContext facesContext = FacesContext.getCurrentInstance();
            facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Malla actualizado", "Se ha actualizado correctamente"));
            return "View";
        } catch (Exception e) {
            FacesContext facesContext = FacesContext.getCurrentInstance();
            
            facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Malla no actualizado", "Lo sentimos, los datos que ingresó ya existen"));
            return null;
        }
    }

    public String destroy(Malla valor) {
        current = valor;
       // selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
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
            facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Malla eliminado", "Se ha eliminado una Malla"));
        } catch (Exception e) {
            FacesContext facesContext = FacesContext.getCurrentInstance();
            facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "ERROR: Malla no eliminado", "Lo sentimos, intentelo mas tarde"));
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
            items = new ListDataModel(ejbFacade.BuscarPorIdUniversidad(session.getIdUniversidad()));
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
        return JsfUtil.getSelectItems(ejbFacade.BuscarPorIdUniversidad(session.getIdUniversidad()), false);
    }

    public SelectItem[] getItemsAvailableSelectOne() {
        return JsfUtil.getSelectItems(ejbFacade.BuscarPorIdUniversidad(session.getIdUniversidad()), true);
    }

    public Malla getMalla(java.lang.Integer id) {
        return ejbFacade.find(id);
    }

    @FacesConverter(forClass = Malla.class)
    public static class MallaControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            MallaController controller = (MallaController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "mallaController");
            return controller.getMalla(getKey(value));
        }

        java.lang.Integer getKey(String value) {
            java.lang.Integer key;
            key = Integer.valueOf(value);
            return key;
        }

        String getStringKey(java.lang.Integer value) {
            StringBuilder sb = new StringBuilder();
            sb.append(value);
            return sb.toString();
        }

        @Override
        public String getAsString(FacesContext facesContext, UIComponent component, Object object) {
            if (object == null) {
                return null;
            }
            if (object instanceof Malla) {
                Malla o = (Malla) object;
                return getStringKey(o.getIdMalla());
            } else {
                throw new IllegalArgumentException("object " + object + " is of type " + object.getClass().getName() + "; expected type: " + Malla.class.getName());
            }
        }
    }
}
