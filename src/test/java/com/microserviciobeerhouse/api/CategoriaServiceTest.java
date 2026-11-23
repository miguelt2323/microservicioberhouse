package com.microserviciobeerhouse.api;

import com.microserviciobeerhouse.api.exception.BadRequestException;
import com.microserviciobeerhouse.api.exception.ResourceNotFoundException;
import com.microserviciobeerhouse.api.model.categoria;
import com.microserviciobeerhouse.api.repository.CategoriaRepository;
import com.microserviciobeerhouse.api.service.impl.CategoriaServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CategoriaServiceTest {

    @Mock
    private CategoriaRepository categoriaRepository;

    @InjectMocks
    private CategoriaServiceImpl categoriaService;

    private categoria testCategoria;

    @BeforeEach
    void setUp() {
        testCategoria = new categoria("pub_123", "http://image.url", "Cerveza artesanal rubia", "Rubia", 1);
    }

    @Test
    void testConsultarCategoriasExitoso() {
        Page<categoria> pageMock = new PageImpl<>(List.of(testCategoria));
        when(categoriaRepository.findAll(any(Pageable.class))).thenReturn(pageMock);

        Page<categoria> resultado = categoriaService.consultarCategorias(0, 10, "id_categoria", "asc");

        assertNotNull(resultado);
        assertEquals(1, resultado.getContent().size());
        assertEquals("Rubia", resultado.getContent().get(0).getNombre_categoria());
        verify(categoriaRepository, times(1)).findAll(any(Pageable.class));
    }

    @Test
    void testConsultarCategoriasPaginaInvalida() {
        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                categoriaService.consultarCategorias(-1, 10, "id_categoria", "asc"));
        assertEquals("El número de página no puede ser menor a cero", ex.getMessage());
    }

    @Test
    void testConsultarCategoriasTamanoInvalido() {
        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                categoriaService.consultarCategorias(0, 0, "id_categoria", "asc"));
        assertEquals("El tamaño de página debe estar entre 1 y 100", ex.getMessage());
    }

    @Test
    void testConsultarPorIdExitoso() {
        when(categoriaRepository.findById(1)).thenReturn(Optional.of(testCategoria));

        categoria resultado = categoriaService.consultarPorId(1);

        assertNotNull(resultado);
        assertEquals(1, resultado.getId_categoria());
        assertEquals("Rubia", resultado.getNombre_categoria());
        verify(categoriaRepository, times(1)).findById(1);
    }

    @Test
    void testConsultarPorIdNoEncontrado() {
        when(categoriaRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> categoriaService.consultarPorId(99));
        verify(categoriaRepository, times(1)).findById(99);
    }

    @Test
    void testEliminarCategoriaExitoso() {
        when(categoriaRepository.existsById(1)).thenReturn(true);
        doNothing().when(categoriaRepository).deleteById(1);

        assertDoesNotThrow(() -> categoriaService.eliminarCategoria(1));
        verify(categoriaRepository, times(1)).existsById(1);
        verify(categoriaRepository, times(1)).deleteById(1);
    }

    @Test
    void testEliminarCategoriaInexistente() {
        when(categoriaRepository.existsById(88)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> categoriaService.eliminarCategoria(88));
        verify(categoriaRepository, times(1)).existsById(88);
        verify(categoriaRepository, never()).deleteById(anyInt());
    }

    @Test
    void testEliminarCategoriaIdInvalido() {
        assertThrows(BadRequestException.class, () -> categoriaService.eliminarCategoria(0));
        verify(categoriaRepository, never()).existsById(anyInt());
    }
}
