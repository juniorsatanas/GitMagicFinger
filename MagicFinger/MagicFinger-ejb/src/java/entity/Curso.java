/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package entity;

import java.io.Serializable;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 *
 * @author chevo
 */
@Entity
@Table(name = "curso")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Curso.findAll", query = "SELECT c FROM Curso c"),
    @NamedQuery(name = "Curso.findByAsignaturaId", query = "SELECT c FROM Curso c WHERE c.cursoPK.asignaturaId = :asignaturaId"),
    @NamedQuery(name = "Curso.findByTipoAsignaturaId", query = "SELECT c FROM Curso c WHERE c.cursoPK.tipoAsignaturaId = :tipoAsignaturaId"),
    @NamedQuery(name = "Curso.findByPorcenteajeAprobacion", query = "SELECT c FROM Curso c WHERE c.porcenteajeAprobacion = :porcenteajeAprobacion"),
    @NamedQuery(name = "Curso.findByTermino", query = "SELECT c FROM Curso c WHERE c.termino = :termino")})
public class Curso implements Serializable {
    private static final long serialVersionUID = 1L;
    @EmbeddedId
    protected CursoPK cursoPK;
    @Column(name = "PORCENTEAJE_APROBACION")
    private Integer porcenteajeAprobacion;
    @Column(name = "TERMINO")
    private Integer termino;
    @ManyToMany(mappedBy = "cursoList")
    private List<Profesor> profesorList;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "curso")
    private List<AlumnosDelCurso> alumnosDelCursoList;
    @JoinColumn(name = "ASIGNATURA_ID", referencedColumnName = "ID_ASIGNATURA", insertable = false, updatable = false)
    @ManyToOne(optional = false)
    private Asignatura asignatura;
    @JoinColumn(name = "TIPO_ASIGNATURA_ID", referencedColumnName = "ID_TIPO_ASIGNATURA", insertable = false, updatable = false)
    @ManyToOne(optional = false)
    private TipoAsignatura tipoAsignatura;
    @OneToMany(mappedBy = "curso")
    private List<BloqueClase> bloqueClaseList;

    public Curso() {
    }

    public Curso(CursoPK cursoPK) {
        this.cursoPK = cursoPK;
    }

    public Curso(int asignaturaId, int tipoAsignaturaId) {
        this.cursoPK = new CursoPK(asignaturaId, tipoAsignaturaId);
    }

    public CursoPK getCursoPK() {
        return cursoPK;
    }

    public void setCursoPK(CursoPK cursoPK) {
        this.cursoPK = cursoPK;
    }

    public Integer getPorcenteajeAprobacion() {
        return porcenteajeAprobacion;
    }

    public void setPorcenteajeAprobacion(Integer porcenteajeAprobacion) {
        this.porcenteajeAprobacion = porcenteajeAprobacion;
    }

    public Integer getTermino() {
        return termino;
    }

    public void setTermino(Integer termino) {
        this.termino = termino;
    }

    @XmlTransient
    public List<Profesor> getProfesorList() {
        return profesorList;
    }

    public void setProfesorList(List<Profesor> profesorList) {
        this.profesorList = profesorList;
    }

    @XmlTransient
    public List<AlumnosDelCurso> getAlumnosDelCursoList() {
        return alumnosDelCursoList;
    }

    public void setAlumnosDelCursoList(List<AlumnosDelCurso> alumnosDelCursoList) {
        this.alumnosDelCursoList = alumnosDelCursoList;
    }

    public Asignatura getAsignatura() {
        return asignatura;
    }

    public void setAsignatura(Asignatura asignatura) {
        this.asignatura = asignatura;
    }

    public TipoAsignatura getTipoAsignatura() {
        return tipoAsignatura;
    }

    public void setTipoAsignatura(TipoAsignatura tipoAsignatura) {
        this.tipoAsignatura = tipoAsignatura;
    }

    @XmlTransient
    public List<BloqueClase> getBloqueClaseList() {
        return bloqueClaseList;
    }

    public void setBloqueClaseList(List<BloqueClase> bloqueClaseList) {
        this.bloqueClaseList = bloqueClaseList;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (cursoPK != null ? cursoPK.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Curso)) {
            return false;
        }
        Curso other = (Curso) object;
        if ((this.cursoPK == null && other.cursoPK != null) || (this.cursoPK != null && !this.cursoPK.equals(other.cursoPK))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "entity.Curso[ cursoPK=" + cursoPK + " ]";
    }
    
}