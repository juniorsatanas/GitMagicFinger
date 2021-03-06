package manageBeans.crud;

import com.sun.jersey.core.util.Base64;
import entity.Alumno;
import entity.Universidad;
import entity.User;
import entity.Userrol;
import manageBeans.crud.util.JsfUtil;
import manageBeans.crud.util.PaginationHelper;

import javax.faces.application.FacesMessage;
import java.io.Serializable;
import java.util.List;
import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.inject.Named;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import manageBeans.LoginSessionMB;
import sessionBeans.AlumnoFacadeLocal;

@Named("alumnoController")
@RequestScoped
public class AlumnoController implements Serializable {
    @Inject 
    UserController usercontroller;
    
    private Alumno current;
    private DataModel items = null;
    @EJB
    private AlumnoFacadeLocal ejbFacade;
    private PaginationHelper pagination;
    private int selectedItemIndex;
    @Inject
    LoginSessionMB session;
    private String huellaEnString1;
    private String huellaEnString2;
    public AlumnoController() {
    }

    public Alumno getSelected() {
        if (current == null) {
            current = new Alumno();
            selectedItemIndex = -1;
        }
        return current;
    }

    private AlumnoFacadeLocal getFacade() {
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

    public String prepareView(Alumno vari) {
        current = vari;
        //selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return "View";
    }

    public String prepareCreate() {
        current = new Alumno();
        selectedItemIndex = -1;
        return "Create";
    }

    public String create() {
        try {
            User user = new User();
            Userrol userrol = new Userrol("Alumno");
            //user.setId();
            FacesContext facesContext = FacesContext.getCurrentInstance();
            user.setUsuario(current.getRut());
            user.setPassword(current.getApellidop());
            user.setUserrolName(userrol);
            usercontroller.setCurrent(user);
            if(usercontroller.create()!=null){
            current.setUniIdUniversidad(new Universidad(session.getIdUniversidad()));
            byte[] templeByte1 = Base64.decode(huellaEnString1);
            byte[] templeByte2 = Base64.decode(huellaEnString2);
            current.setHuella1(templeByte1);
            current.setHuella2(templeByte2);
            getFacade().create(current);
            //EDITANDO
            Alumno alumno = getFacade().findAll().get(getFacade().count()-1);
            User user2 = usercontroller.getFacade().findAll().get(usercontroller.getFacade().count()-1);
            alumno.setUseId(user2);
            user2.setAluIdAlumno(alumno);
            getFacade().edit(alumno);
            usercontroller.getFacade().edit(user2);
            /*****/
            facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Alumno creado", "Se ha creado una Alumno correctamente"));
            return prepareList();
            }else{
                facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "ERROR: El alumno ya existe en los registros",null ));
                return null;
            }
        } catch (Exception e) {
            FacesContext facesContext = FacesContext.getCurrentInstance();
            facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "ERROR: Alumno no creado", "Lo sentimos, intentelo mas tarde"));
            return null;
        }
        
    }

    public String prepareEdit(Alumno var) {
        current = var;
       // selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return "Edit";
    }

    public String update() {
        try {
            getFacade().edit(current);
            FacesContext facesContext = FacesContext.getCurrentInstance();
            facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Alumno actualizado", "Se ha actualizado correctamente"));
            return "View";
        } catch (Exception e) {
            FacesContext facesContext = FacesContext.getCurrentInstance();
            facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error: Alumno no actualizado", "Lo sentimos, intentelo mas tarde"));

            return null;
        }
    }

    public String destroy(Alumno valor) {
        current = valor;
      //  selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
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
            facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Alumno eliminado", "Se ha eliminado una Alumno"));
        } catch (Exception e) {
            FacesContext facesContext = FacesContext.getCurrentInstance();
            facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "ERROR: Alumno no eliminado", "Lo sentimos, intentelo mas tarde"));
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
            List lista = ejbFacade.BuscarPorIdUniversidad(session.getIdUniversidad());
            items = new ListDataModel(lista);
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

    public Alumno getAlumno(java.lang.Integer id) {
        return ejbFacade.find(id);
    }

    @FacesConverter(forClass = Alumno.class)
    public static class AlumnoControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            AlumnoController controller = (AlumnoController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "alumnoController");
            return controller.getAlumno(getKey(value));
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
            if (object instanceof Alumno) {
                Alumno o = (Alumno) object;
                return getStringKey(o.getIdAlumno());
            } else {
                throw new IllegalArgumentException("object " + object + " is of type " + object.getClass().getName() + "; expected type: " + Alumno.class.getName());
            }
        }
    }

    public String getHuellaEnString1() {
        return huellaEnString1;
    }

    public void setHuellaEnString1(String huellaEnString1) {
        this.huellaEnString1 = huellaEnString1;
    }

    public String getHuellaEnString2() {
        return huellaEnString2;
    }

    public void setHuellaEnString2(String huellaEnString2) {
        this.huellaEnString2 = huellaEnString2;
    }
    
}
