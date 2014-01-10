/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package manageBeans;

import entity.Universidad;
import java.io.IOException;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Init;
import javax.inject.Named;
import javax.enterprise.context.RequestScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import sessionBeans.UniversidadFacadeLocal;

/**
 *
 * @author chevo
 */
@Named(value = "postLoginMB")
@RequestScoped
public class postLoginMB {
    @EJB
    private UniversidadFacadeLocal universidadFacade;
    private List<Universidad> listUniversidad;
    @Inject
    private LoginSessionMB login;
    private String nombreUniversidad;
    public postLoginMB() {
    }
    @PostConstruct
    public void init(){
        Integer nueva = login.getIdUniversidad();
        if(nueva!=null){
            nombreUniversidad = universidadFacade.find(nueva).getNombre();
        } 
        listUniversidad = universidadFacade.findAll();
    }
    
    public void redireccionar(String pagina){
        ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
       try {
           externalContext.redirect(externalContext.getRequestContextPath() + pagina);
       }
       catch (IOException e) {
           System.out.println(e.getMessage());
       }
    }
    
    public void redirec(Integer universidad){
        login.setIdUniversidad(universidad);
        redireccionar("/faces/administrador/index3.xhtml");        
    }
    public List<Universidad> getListUniversidad() {
        return listUniversidad;
    }

    public void setListUniversidad(List<Universidad> listUniversidad) {
        this.listUniversidad = listUniversidad;
    }

    public String getNombreUniversidad() {
        return nombreUniversidad;
    }

    public void setNombreUniversidad(String nombreUniversidad) {
        this.nombreUniversidad = nombreUniversidad;
    }
    
    
}