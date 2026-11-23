package com.microserviciobeerhouse.api.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "categoria")
public class categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_categoria")
    private int id_categoria;

    @Column(name = "nombre_categoria", nullable = false, length = 100)
    private String nombre_categoria;

    @Column(name = "descripcion", length = 500)
    private String descripcion;

    @Column(name = "imagen", length = 255)
    private String imagen;

    @Column(name = "imagen_public_id", length = 255)
    private String imagen_public_id;

    public categoria() {
    }

    public categoria(String imagen_public_id, String imagen, String descripcion, String nombre_categoria, int id_categoria) {
        this.imagen_public_id = imagen_public_id;
        this.imagen = imagen;
        this.descripcion = descripcion;
        this.nombre_categoria = nombre_categoria;
        this.id_categoria = id_categoria;
    }

    public int getId_categoria() {
        return id_categoria;
    }

    public void setId_categoria(int id_categoria) {
        this.id_categoria = id_categoria;
    }

    public String getNombre_categoria() {
        return nombre_categoria;
    }

    public void setNombre_categoria(String nombre_categoria) {
        this.nombre_categoria = nombre_categoria;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getImagen() {
        return imagen;
    }

    public void setImagen(String imagen) {
        this.imagen = imagen;
    }

    public String getImagen_public_id() {
        return imagen_public_id;
    }

    public void setImagen_public_id(String imagen_public_id) {
        this.imagen_public_id = imagen_public_id;
    }
}
