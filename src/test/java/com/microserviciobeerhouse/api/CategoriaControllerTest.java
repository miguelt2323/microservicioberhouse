package com.microserviciobeerhouse.api;

import com.microserviciobeerhouse.api.controller.CategoriaController;
import com.microserviciobeerhouse.api.exception.BadRequestException;
import com.microserviciobeerhouse.api.exception.GlobalExceptionHandler;
import com.microserviciobeerhouse.api.exception.ResourceNotFoundException;
import com.microserviciobeerhouse.api.model.categoria;
import com.microserviciobeerhouse.api.service.ICategoriaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class CategoriaControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ICategoriaService categoriaService;

    @InjectMocks
    private CategoriaController categoriaController;

    private categoria testCategoria;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(categoriaController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        testCategoria = new categoria("pub_123", "http://image.url", "Cerveza negra", "Negra", 2);
    }

    @Test
    void testConsultarCategorias200Ok() throws Exception {
        when(categoriaService.consultarCategorias(0, 10, "id_categoria", "asc"))
                .thenReturn(new PageImpl<>(List.of(testCategoria)));

        mockMvc.perform(get("/api/categorias")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.content[0].nombre_categoria").value("Negra"));
    }

    @Test
    void testConsultarCategorias400BadRequest() throws Exception {
        when(categoriaService.consultarCategorias(-1, 10, "id_categoria", "asc"))
                .thenThrow(new BadRequestException("El número de página no puede ser menor a cero"));

        mockMvc.perform(get("/api/categorias")
                        .param("page", "-1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("El número de página no puede ser menor a cero"));
    }

    @Test
    void testConsultarPorId200Ok() throws Exception {
        when(categoriaService.consultarPorId(2)).thenReturn(testCategoria);

        mockMvc.perform(get("/api/categorias/2")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.id_categoria").value(2));
    }

    @Test
    void testEliminarCategoria200Ok() throws Exception {
        doNothing().when(categoriaService).eliminarCategoria(2);

        mockMvc.perform(delete("/api/categorias/2")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Categoría eliminada exitosamente"));
    }

    @Test
    void testEliminarCategoria400NotFound() throws Exception {
        doThrow(new ResourceNotFoundException("No se puede eliminar: la categoría con ID 99 no existe"))
                .when(categoriaService).eliminarCategoria(99);

        mockMvc.perform(delete("/api/categorias/99")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Recurso no encontrado"));
    }

    @Test
    void testError500InternalServerError() throws Exception {
        when(categoriaService.consultarCategorias(anyInt(), anyInt(), anyString(), anyString()))
                .thenThrow(new RuntimeException("Fallo crítico en conexión a la base de datos"));

        mockMvc.perform(get("/api/categorias")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Internal Server Error"));
    }
}
