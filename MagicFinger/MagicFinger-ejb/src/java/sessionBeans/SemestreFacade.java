/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sessionBeans;

import entity.Semestre;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 *
 * @author chevo
 */
@Stateless
public class SemestreFacade extends AbstractFacade<Semestre> implements SemestreFacadeLocal {
    @PersistenceContext(unitName = "MagicFinger-ejbPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public SemestreFacade() {
        super(Semestre.class);
    }
    
}
