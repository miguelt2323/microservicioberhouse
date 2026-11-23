package com.microserviciobeerhouse.api.service.impl;

import com.microserviciobeerhouse.api.exception.BadRequestException;
import com.microserviciobeerhouse.api.exception.ResourceNotFoundException;
import com.microserviciobeerhouse.api.model.categoria;
import com.microserviciobeerhouse.api.repository.CategoriaRepository;
import com.microserviciobeerhouse.api.service.ICategoriaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CategoriaServiceImpl implements ICategoriaService {

    private static final Logger log = LoggerFactory.getLogger(CategoriaServiceImpl.class);

    private final CategoriaRepository categoriaRepository;

    @Autowired
    public CategoriaServiceImpl(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<categoria> consultarCategorias(int page, int size, String sortBy, String sortDir) {
        log.info("Ejecutando consultarCategorias - pagina: {}, tamano: {}, orden: {} {}", page, size, sortBy, sortDir);
        try {
            // Validaciones de negocio para códigos de estado 400
            if (page < 0) {
                log.warn("Validación fallida: el número de página no puede ser menor a cero (recibido: {})", page);
                throw new BadRequestException("El número de página no puede ser menor a cero");
            }
            if (size <= 0 || size > 100) {
                log.warn("Validación fallida: el tamaño de página debe estar entre 1 y 100 (recibido: {})", size);
                throw new BadRequestException("El tamaño de página debe estar entre 1 y 100");
            }

            Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);

            Page<categoria> categorias = categoriaRepository.findAll(pageable);
            log.info("Consulta exitosa. Se recuperaron {} registros de un total de {} elementos en la base de datos.",
                    categorias.getNumberOfElements(), categorias.getTotalElements());
            return categorias;
        } catch (BadRequestException e) {
            log.warn("Error de validación 400 en consultarCategorias: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error al consultar categorías en la base de datos: {}", e.getMessage(), e);
            throw new RuntimeException("Error en la base de datos al consultar categorías: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public categoria consultarPorId(int id) {
        log.info("Ejecutando consultarPorId con id: {}", id);
        try {
            if (id <= 0) {
                log.warn("Validación fallida: El ID debe ser mayor a cero (recibido: {})", id);
                throw new BadRequestException("El ID de la categoría debe ser un número entero mayor a cero");
            }

            return categoriaRepository.findById(id)
                    .orElseThrow(() -> {
                        log.warn("Categoría con ID {} no encontrada", id);
                        return new ResourceNotFoundException("No se encontró la categoría con ID: " + id);
                    });
        } catch (BadRequestException | ResourceNotFoundException e) {
            log.warn("Excepción de negocio en consultarPorId: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado en consultarPorId para id {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Error al consultar la categoría con ID " + id + ": " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void eliminarCategoria(int id) {
        log.info("Ejecutando eliminarCategoria con id: {}", id);
        try {
            if (id <= 0) {
                log.warn("Validación fallida al eliminar: ID inválido (recibido: {})", id);
                throw new BadRequestException("El ID de la categoría a eliminar debe ser mayor a cero");
            }

            if (!categoriaRepository.existsById(id)) {
                log.warn("Intento de eliminar categoría inexistente con ID: {}", id);
                throw new ResourceNotFoundException("No se puede eliminar: la categoría con ID " + id + " no existe");
            }

            categoriaRepository.deleteById(id);
            log.info("Categoría con ID {} eliminada satisfactoriamente de la base de datos", id);
        } catch (BadRequestException | ResourceNotFoundException e) {
            log.warn("Excepción de validación en eliminarCategoria: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado al eliminar la categoría con ID {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Error al eliminar la categoría en la base de datos: " + e.getMessage(), e);
        }
    }
}
