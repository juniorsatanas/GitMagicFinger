package manageBeans.crud;

import entity.Departamento;
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
import javax.inject.Inject;
import sessionBeans.DepartamentoFacadeLocal;

@Named("departamentoController")
@SessionScoped
public class DepartamentoController implements Serializable {
    @Inject FacultadController facultadcontroller;
    private Facultad facultadAnterior;
    private String nombreAnterior;
    private Departamento current;
    private DataModel items = null;
    @EJB
    private DepartamentoFacadeLocal ejbFacade;
    private PaginationHelper pagination;
    private int selectedItemIndex;

    public DepartamentoController() {
    }

    public Departamento getSelected() {
        if (current == null) {
            current = new Departamento();
            selectedItemIndex = -1;
        }
        return current;
    }

    private DepartamentoFacadeLocal getFacade() {
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

    public String prepareView() {
        current = (Departamento) getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return "View";
    }

    public String prepareCreate() {
        current = new Departamento();
        selectedItemIndex = -1;
        return "Create";
    }

    public String create() {
        try {
            FacesContext facesContext = FacesContext.getCurrentInstance();
            for (Departamento departamento : getFacade().findAll()) {
                if(departamento.getNombre().equals(current.getNombre()) && departamento.getFacultadId().equals(current.getFacultadId())){
                facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "ERROR: Departamento no creado","El departamento ya existe en facultad seleccionada"));
                    return null;
                }
            }
            getFacade().create(current);
            facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Departamento creado", "Se ha creado un departamento correctamente"));
            return prepareList();
        } catch (Exception e) {
            FacesContext facesContext = FacesContext.getCurrentInstance();
            facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "ERROR: Departamento no creado","Lo sentimos, inténtelo más tarde"));
            return null;
        }
    }

    public String prepareEdit() {
        current = (Departamento) getItems().getRowData();
        nombreAnterior = current.getNombre();
        facultadAnterior = current.getFacultadId();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return "Edit";
    }

    public String update() {
        try {
            FacesContext facesContext = FacesContext.getCurrentInstance();
            if(!(nombreAnterior.equals(current.getNombre())) || !(facultadAnterior.equals(current.getFacultadId()))){
                for (Departamento departamento : getFacade().findAll()) {
                    if(departamento.getNombre().equals(current.getNombre()) && departamento.getFacultadId().equals(current.getFacultadId())){
                         facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "ERROR: Departamento no creado","El departamento ya existe en facultad seleccionada"));
                         return null;
                    }
                }
            }
            getFacade().edit(current);
            facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Departamento actualizado", "Se ha actualizado correctamente"));
            return "List";
        } catch (Exception e) {
            FacesContext facesContext = FacesContext.getCurrentInstance();
            facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error: Departamento no actualizado", "Lo sentimos, inténtelo más tarde"));

            return null;
        }
    }

    public String destroy() {
        current = (Departamento) getItems().getRowData();
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
            facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Departamento eliminado", "Se ha eliminado una departamento"));
        } catch (Exception e) {
            FacesContext facesContext = FacesContext.getCurrentInstance();
            facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "ERROR: Departamento no eliminado", "Lo sentimos, inténtelo más tarde"));
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

    public Departamento getDepartamento(java.lang.Integer id) {
        return ejbFacade.find(id);
    }

    @FacesConverter(forClass = Departamento.class)
    public static class DepartamentoControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            DepartamentoController controller = (DepartamentoController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "departamentoController");
            return controller.getDepartamento(getKey(value));
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
            if (object instanceof Departamento) {
                Departamento o = (Departamento) object;
                return getStringKey(o.getIdDepartamento());
            } else {
                throw new IllegalArgumentException("object " + object + " is of type " + object.getClass().getName() + "; expected type: " + Departamento.class.getName());
            }
        }
    }
}
