package com.microserviciobeerhouse.api.controller;

import com.microserviciobeerhouse.api.dto.ApiResponseDto;
import com.microserviciobeerhouse.api.dto.PaginatedResponseDto;
import com.microserviciobeerhouse.api.model.categoria;
import com.microserviciobeerhouse.api.service.ICategoriaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para la gestión de Categorías
 */
@RestController
@RequestMapping("/api/categorias")
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.DELETE, RequestMethod.OPTIONS})
public class CategoriaController {

    private static final Logger log = LoggerFactory.getLogger(CategoriaController.class);

    private final ICategoriaService categoriaService;

    @Autowired
    public CategoriaController(ICategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    /**
     * Endpoint para consultar categorías con paginación
     * Ejemplos:
     * GET /api/categorias?page=0&size=10
     * GET /api/categorias?page=1&size=5&sortBy=nombre_categoria&sortDir=asc
     */
    @GetMapping
    public ResponseEntity<ApiResponseDto<PaginatedResponseDto<categoria>>> consultarCategorias(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id_categoria") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir
    ) {
        log.info("REST: Solicitud recibida GET /api/categorias - Pagina: {}, Tamaño: {}, Orden: {} {}",
                page, size, sortBy, sortDir);
        try {
            Page<categoria> resultado = categoriaService.consultarCategorias(page, size, sortBy, sortDir);
            PaginatedResponseDto<categoria> paginatedResponse = PaginatedResponseDto.fromPage(resultado);
            ApiResponseDto<PaginatedResponseDto<categoria>> response = new ApiResponseDto<>(
                    HttpStatus.OK.value(),
                    "Categorías consultadas exitosamente con paginación",
                    paginatedResponse
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("REST Error al consultar categorías: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Endpoint para consultar una categoría por su ID
     * Ejemplo:
     * GET /api/categorias/1
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDto<categoria>> consultarPorId(@PathVariable int id) {
        log.info("REST: Solicitud recibida GET /api/categorias/{}", id);
        try {
            categoria cat = categoriaService.consultarPorId(id);
            ApiResponseDto<categoria> response = new ApiResponseDto<>(
                    HttpStatus.OK.value(),
                    "Categoría encontrada con éxito",
                    cat
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("REST Error al consultar categoría con ID {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Endpoint para eliminar una categoría por su ID
     * Ejemplo:
     * DELETE /api/categorias/1
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDto<String>> eliminarCategoria(@PathVariable int id) {
        log.info("REST: Solicitud recibida DELETE /api/categorias/{}", id);
        try {
            categoriaService.eliminarCategoria(id);
            ApiResponseDto<String> response = new ApiResponseDto<>(
                    HttpStatus.OK.value(),
                    "Categoría eliminada exitosamente",
                    "La categoría con ID " + id + " ha sido eliminada del sistema."
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("REST Error al eliminar categoría con ID {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }
}
