package com.microserviciobeerhouse.api.service;

import com.microserviciobeerhouse.api.model.categoria;
import org.springframework.data.domain.Page;

public interface ICategoriaService {

    /**
     * Consulta categorías con paginación y ordenamiento
     */
    Page<categoria> consultarCategorias(int page, int size, String sortBy, String sortDir);

    /**
     * Consulta una categoría por su identificador primario
     */
    categoria consultarPorId(int id);

    /**
     * Elimina una categoría por su identificador primario
     */
    void eliminarCategoria(int id);
}
