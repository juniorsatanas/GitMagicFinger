package manageBeans.crud;

import entity.Curso;
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
import sessionBeans.CursoFacadeLocal;

@Named("cursoController")
@SessionScoped
public class CursoController implements Serializable {

    private Curso current;
    private DataModel items = null;
    @EJB
    private CursoFacadeLocal ejbFacade;
    private PaginationHelper pagination;
    private int selectedItemIndex;

    public CursoController() {
    }

    public Curso getSelected() {
        if (current == null) {
            current = new Curso();
            current.setCursoPK(new entity.CursoPK());
            selectedItemIndex = -1;
        }
        return current;
    }

    private CursoFacadeLocal getFacade() {
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

    public String prepareView(Curso vari) {
        current = vari;
        //selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return "View";
    }

    public String prepareCreate() {
        current = new Curso();
        current.setCursoPK(new entity.CursoPK());
        selectedItemIndex = -1;
        return "Create";
    }

    public String create() {
        try {
            current.getCursoPK().setNombreDepartamento(current.getAsignatura().getAsignaturaPK().getNombreDepartamento());
            current.getCursoPK().setNombreCarrera(current.getAsignatura().getAsignaturaPK().getNombreCarrera());
            current.getCursoPK().setIdFecha(current.getSemestre().getIdFecha());
            current.getCursoPK().setNombreMalla(current.getAsignatura().getAsignaturaPK().getNombreMalla());
            current.getCursoPK().setNombreAsignatura(current.getAsignatura().getAsignaturaPK().getNombreAsignatura());
            current.getCursoPK().setNombreFacultad(current.getAsignatura().getAsignaturaPK().getNombreFacultad());
            current.getCursoPK().setIdTipoAsignatura(current.getTipoAsignatura().getIdTipoAsignatura());
            current.getCursoPK().setIdUniversidad(current.getAsignatura().getAsignaturaPK().getIdUniversidad());
            getFacade().create(current);
            FacesContext facesContext = FacesContext.getCurrentInstance();
            facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Curso creado", "Se ha creado una Curso correctamente"));
            return prepareList();
        } catch (Exception e) {
            FacesContext facesContext = FacesContext.getCurrentInstance();
            facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "ERROR: Curso no creado", "Lo sentimos, intentelo mas tarde"));
            return null;
        }
    }

    public String prepareEdit(Curso var) {
        current = var;
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return "Edit";
    }

    public String update() {
        try {
            current.getCursoPK().setNombreDepartamento(current.getAsignatura().getAsignaturaPK().getNombreDepartamento());
            current.getCursoPK().setNombreCarrera(current.getAsignatura().getAsignaturaPK().getNombreCarrera());
            current.getCursoPK().setIdFecha(current.getSemestre().getIdFecha());
            current.getCursoPK().setNombreMalla(current.getAsignatura().getAsignaturaPK().getNombreMalla());
            current.getCursoPK().setNombreAsignatura(current.getAsignatura().getAsignaturaPK().getNombreAsignatura());
            current.getCursoPK().setNombreFacultad(current.getAsignatura().getAsignaturaPK().getNombreFacultad());
            current.getCursoPK().setIdTipoAsignatura(current.getTipoAsignatura().getIdTipoAsignatura());
            current.getCursoPK().setIdUniversidad(current.getAsignatura().getAsignaturaPK().getIdUniversidad());
            getFacade().edit(current);
            FacesContext facesContext = FacesContext.getCurrentInstance();
            facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Curso actualizado", "Se ha actualizado correctamente"));
            return "View";
        } catch (Exception e) {
            FacesContext facesContext = FacesContext.getCurrentInstance();
            facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error: Curso no actualizado", "Lo sentimos, intentelo mas tarde"));

            return null;
        }
    }

    public String destroy(Curso valor) {
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
            facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Curso eliminado", "Se ha eliminado una Curso"));
        } catch (Exception e) {
            FacesContext facesContext = FacesContext.getCurrentInstance();
            facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "ERROR: Curso no eliminado", "Lo sentimos, intentelo mas tarde"));
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

    public Curso getCurso(entity.CursoPK id) {
        return ejbFacade.find(id);
    }

    @FacesConverter(forClass = Curso.class)
    public static class CursoControllerConverter implements Converter {

        private static final String SEPARATOR = "#";
        private static final String SEPARATOR_ESCAPED = "\\#";

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            CursoController controller = (CursoController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "cursoController");
            return controller.getCurso(getKey(value));
        }

        entity.CursoPK getKey(String value) {
            entity.CursoPK key;
            String values[] = value.split(SEPARATOR_ESCAPED);
            key = new entity.CursoPK();
            key.setIdUniversidad(Integer.parseInt(values[0]));
            key.setNombreFacultad(values[1]);
            key.setNombreDepartamento(values[2]);
            key.setNombreCarrera(values[3]);
            key.setNombreMalla(values[4]);
            key.setNombreAsignatura(values[5]);
            key.setIdTipoAsignatura(Integer.parseInt(values[6]));
            key.setIdFecha(Integer.parseInt(values[7]));
            return key;
        }

        String getStringKey(entity.CursoPK value) {
            StringBuilder sb = new StringBuilder();
            sb.append(value.getIdUniversidad());
            sb.append(SEPARATOR);
            sb.append(value.getNombreFacultad());
            sb.append(SEPARATOR);
            sb.append(value.getNombreDepartamento());
            sb.append(SEPARATOR);
            sb.append(value.getNombreCarrera());
            sb.append(SEPARATOR);
            sb.append(value.getNombreMalla());
            sb.append(SEPARATOR);
            sb.append(value.getNombreAsignatura());
            sb.append(SEPARATOR);
            sb.append(value.getIdTipoAsignatura());
            sb.append(SEPARATOR);
            sb.append(value.getIdFecha());
            return sb.toString();
        }

        @Override
        public String getAsString(FacesContext facesContext, UIComponent component, Object object) {
            if (object == null) {
                return null;
            }
            if (object instanceof Curso) {
                Curso o = (Curso) object;
                return getStringKey(o.getCursoPK());
            } else {
                throw new IllegalArgumentException("object " + object + " is of type " + object.getClass().getName() + "; expected type: " + Curso.class.getName());
            }
        }
    }
}
