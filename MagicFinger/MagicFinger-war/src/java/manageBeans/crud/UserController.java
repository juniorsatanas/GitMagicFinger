package manageBeans.crud;

import entity.User;
import manageBeans.crud.util.JsfUtil;
import manageBeans.crud.util.PaginationHelper;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
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
import sessionBeans.UserFacadeLocal;

@Named("userController")
@RequestScoped
public class UserController implements Serializable {

    private User current;
    private DataModel items = null;
    @EJB
    private UserFacadeLocal ejbFacade;
    private PaginationHelper pagination;
    private int selectedItemIndex;
    
    public UserController() {
    }

    public User getSelected() {
        if (current == null) {
            current = new User();
            selectedItemIndex = -1;
        }
        return current;
    }

    public UserFacadeLocal getFacade() {
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

    public String prepareView(User vari) {
        current = vari;
        //selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return "View";
    }

    public String prepareCreate() {
        current = new User();
        selectedItemIndex = -1;
        return "Create";
    }
    private static String convertToMd5(final String md5) throws UnsupportedEncodingException {
    StringBuffer sb = null;   
    try {
            final java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
            final byte[] array = md.digest(md5.getBytes("UTF-8"));
            sb = new StringBuffer();
            for (int i = 0; i < array.length; ++i) {
                sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1, 3));
            }
            return sb.toString();
        } catch (final java.security.NoSuchAlgorithmException e) {
        }
        return sb.toString();
    }
    public String create() {
        try {
            FacesContext facesContext = FacesContext.getCurrentInstance();
            for (User user : getFacade().findAll()) {
                if(user.getUsuario().equals(current.getUsuario())){
                    return null;
                }
            }
            current.setPassword(convertToMd5(current.getPassword()));
            getFacade().create(current);
           // facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "User creado", "Se ha creado una User correctamente"));
            return prepareList();
        } catch (Exception e) {
            FacesContext facesContext = FacesContext.getCurrentInstance();
            
            facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Usuario no creado", "Lo sentimos, los datos que ingresó ya existen"));
            return null;
        }
    }

    public String prepareEdit(User var) {
        current = var;
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return "Edit";
    }

    public String update() {
        try {
            FacesContext facesContext = FacesContext.getCurrentInstance();
            for (User user : getFacade().findAll()) {
                if(user.getUsuario().equals(current.getUsuario())){
                    facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "ERROR: El usuario ya existe","Debe escoger otro nombre" ));
                    return null;
                }
            }
            current.setPassword(convertToMd5(current.getPassword()));
            getFacade().edit(current);
            facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "User actualizado", "Se ha actualizado correctamente"));
            return "View";
        } catch (Exception e) {
            FacesContext facesContext = FacesContext.getCurrentInstance();
            
            facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "user no actualizado", "Lo sentimos, los datos que ingresó ya existen"));
            return null;
        }
    }

    public String destroy(User valor) {
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
            facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "User eliminado", "Se ha eliminado una User"));
        } catch (Exception e) {
            FacesContext facesContext = FacesContext.getCurrentInstance();
            facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "ERROR: User no eliminado", "Lo sentimos, intentelo mas tarde"));
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

    public User getUser(java.lang.Integer id) {
        return ejbFacade.find(id);
    }

    @FacesConverter(forClass = User.class)
    public static class UserControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            UserController controller = (UserController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "userController");
            return controller.getUser(getKey(value));
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
            if (object instanceof User) {
                User o = (User) object;
                return getStringKey(o.getId());
            } else {
                throw new IllegalArgumentException("object " + object + " is of type " + object.getClass().getName() + "; expected type: " + User.class.getName());
            }
        }
    }

    public User getCurrent() {
        return current;
    }

    public void setCurrent(User current) {
        this.current = current;
    }
    
}
