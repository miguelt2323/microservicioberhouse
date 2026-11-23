/**
 * Controlador de Frontend para API Cervecería Beerhouse
 * Gestiona peticiones a /api/categorias con paginación, búsqueda por ID, y eliminación.
 */

// Estado global de la aplicación
const state = {
  page: 0,
  size: 10,
  sortBy: 'id_categoria',
  sortDir: 'asc',
  totalPages: 0,
  totalElements: 0,
  currentCategories: [],
  deleteTargetId: null
};

// URL Base del Endpoint
const API_BASE_URL = '/api/categorias';

// Referencias del DOM
const elements = {
  categoriesTableBody: document.getElementById('categoriesTableBody'),
  paginationContainer: document.getElementById('paginationContainer'),
  paginationInfo: document.getElementById('paginationInfo'),
  paginationButtons: document.getElementById('paginationButtons'),
  totalCountBadge: document.getElementById('totalCountBadge'),
  searchInput: document.getElementById('searchIdInput'),
  btnSearch: document.getElementById('btnSearch'),
  btnClearSearch: document.getElementById('btnClearSearch'),
  sortBySelect: document.getElementById('sortBySelect'),
  sortDirSelect: document.getElementById('sortDirSelect'),
  pageSizeSelect: document.getElementById('pageSizeSelect'),
  btnRefresh: document.getElementById('btnRefresh'),
  toastContainer: document.getElementById('toastContainer'),
  
  // Modal de Eliminación
  deleteModal: document.getElementById('deleteModal'),
  deleteCategoryName: document.getElementById('deleteCategoryName'),
  deleteCategoryId: document.getElementById('deleteCategoryId'),
  btnConfirmDelete: document.getElementById('btnConfirmDelete'),
  btnCancelDelete: document.getElementById('btnCancelDelete'),
  btnCloseDeleteModal: document.getElementById('btnCloseDeleteModal'),

  // Modal de Detalle
  detailModal: document.getElementById('detailModal'),
  detailImage: document.getElementById('detailImage'),
  detailId: document.getElementById('detailId'),
  detailName: document.getElementById('detailName'),
  detailDesc: document.getElementById('detailDesc'),
  detailPublicId: document.getElementById('detailPublicId'),
  btnCloseDetailModal: document.getElementById('btnCloseDetailModal'),
  btnCloseDetailBtn: document.getElementById('btnCloseDetailBtn')
};

// ================= INICIALIZACIÓN =================
document.addEventListener('DOMContentLoaded', () => {
  setupEventListeners();
  cargarCategorias();
});

function setupEventListeners() {
  // Filtros y cambio de página
  elements.pageSizeSelect.addEventListener('change', (e) => {
    state.size = parseInt(e.target.value, 10);
    state.page = 0;
    cargarCategorias();
  });

  elements.sortBySelect.addEventListener('change', (e) => {
    state.sortBy = e.target.value;
    state.page = 0;
    cargarCategorias();
  });

  elements.sortDirSelect.addEventListener('change', (e) => {
    state.sortDir = e.target.value;
    state.page = 0;
    cargarCategorias();
  });

  elements.btnRefresh.addEventListener('click', () => {
    elements.searchInput.value = '';
    state.page = 0;
    cargarCategorias();
  });

  // Búsqueda por ID
  elements.btnSearch.addEventListener('click', handleSearchById);
  elements.searchInput.addEventListener('keypress', (e) => {
    if (e.key === 'Enter') handleSearchById();
  });
  elements.btnClearSearch.addEventListener('click', () => {
    elements.searchInput.value = '';
    state.page = 0;
    cargarCategorias();
  });

  // Eventos de Modales
  elements.btnCancelDelete.addEventListener('click', closeDeleteModal);
  elements.btnCloseDeleteModal.addEventListener('click', closeDeleteModal);
  elements.btnConfirmDelete.addEventListener('click', executeDeleteCategory);

  elements.btnCloseDetailModal.addEventListener('click', closeDetailModal);
  elements.btnCloseDetailBtn.addEventListener('click', closeDetailModal);

  // Cerrar al hacer clic fuera del modal
  window.addEventListener('click', (e) => {
    if (e.target === elements.deleteModal) closeDeleteModal();
    if (e.target === elements.detailModal) closeDetailModal();
  });
}

// ================= LLAMADAS A LA API =================

/**
 * 1. Consultar categorías con Paginación
 * GET /api/categorias?page=X&size=Y&sortBy=Z&sortDir=W
 */
async function cargarCategorias() {
  mostrarCargando();
  const url = `${API_BASE_URL}?page=${state.page}&size=${state.size}&sortBy=${state.sortBy}&sortDir=${state.sortDir}`;

  try {
    const response = await fetch(url, {
      method: 'GET',
      headers: { 'Accept': 'application/json' }
    });

    const data = await response.json();

    if (response.ok && data.status === 200) {
      const paginacion = data.data;
      state.currentCategories = paginacion.content || [];
      state.totalPages = paginacion.totalPages || 0;
      state.totalElements = paginacion.totalElements || 0;

      renderizarTabla(state.currentCategories);
      renderizarPaginacion(paginacion);
      elements.totalCountBadge.textContent = `${state.totalElements} registros`;
    } else {
      // Manejo de error 400 / 500 estructurado
      manejarRespuestaError(data, response.status);
      renderizarErrorEnTabla(data.message || 'Error al obtener categorías');
    }
  } catch (error) {
    mostrarToast('error', 'Error de Conexión (500)', 'No fue posible comunicarse con el servidor o la base de datos MySQL.');
    renderizarErrorEnTabla('Error de red o servidor no disponible.');
  }
}

/**
 * 2. Consultar categoría por ID
 * GET /api/categorias/{id}
 */
async function handleSearchById() {
  const query = elements.searchInput.value.trim();
  if (!query) {
    mostrarToast('warning', 'Validación 400', 'Ingrese un ID numérico de categoría válido.');
    return;
  }

  const id = parseInt(query, 10);
  if (isNaN(id) || id <= 0) {
    mostrarToast('warning', 'Validación 400', 'El ID debe ser un número entero mayor a cero.');
    return;
  }

  mostrarCargando();

  try {
    const response = await fetch(`${API_BASE_URL}/${id}`, {
      method: 'GET',
      headers: { 'Accept': 'application/json' }
    });

    const data = await response.json();

    if (response.ok && data.status === 200) {
      const cat = data.data;
      state.currentCategories = [cat];
      state.totalPages = 1;
      state.totalElements = 1;
      renderizarTabla([cat]);
      renderizarPaginacionBusquedaIndividual();
      elements.totalCountBadge.textContent = `1 resultado`;
      mostrarToast('success', 'Consulta Exitosa (200 OK)', `Categoría '${cat.nombre_categoria}' encontrada.`);
    } else {
      // 400 / 404 / 500
      manejarRespuestaError(data, response.status);
      renderizarErrorEnTabla(data.message || 'Categoría no encontrada');
      elements.totalCountBadge.textContent = `0 resultados`;
    }
  } catch (error) {
    mostrarToast('error', 'Fallo Crítico (500)', 'Error al consultar la categoría en el servidor.');
    renderizarErrorEnTabla('Error de red o fallo en el servicio.');
  }
}

/**
 * 3. Eliminar Categoría por ID
 * DELETE /api/categorias/{id}
 */
async function executeDeleteCategory() {
  if (!state.deleteTargetId) return;

  const id = state.deleteTargetId;
  elements.btnConfirmDelete.disabled = true;
  elements.btnConfirmDelete.innerHTML = `<span class="loading-spinner" style="width:16px;height:16px;"></span> Eliminando...`;

  try {
    const response = await fetch(`${API_BASE_URL}/${id}`, {
      method: 'DELETE',
      headers: { 'Accept': 'application/json' }
    });

    const data = await response.json();

    if (response.ok && data.status === 200) {
      mostrarToast('success', 'Eliminación Exitosa (200 OK)', data.message || `La categoría ${id} ha sido eliminada.`);
      closeDeleteModal();
      // Recargar página actual
      cargarCategorias();
    } else {
      manejarRespuestaError(data, response.status);
    }
  } catch (error) {
    mostrarToast('error', 'Error 500', 'Fallo crítico al intentar eliminar la categoría.');
  } finally {
    elements.btnConfirmDelete.disabled = false;
    elements.btnConfirmDelete.innerHTML = `Sí, Eliminar`;
  }
}

// ================= RENDERIZADO UI =================

function renderizarTabla(categorias) {
  if (!categorias || categorias.length === 0) {
    elements.categoriesTableBody.innerHTML = `
      <tr>
        <td colspan="6">
          <div class="empty-state">
            <svg fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M20 13V6a2 2 0 00-2-2H6a2 2 0 00-2 2v7m16 0v5a2 2 0 01-2 2H6a2 2 0 01-2-2v-5m16 0h-2.586a1 1 0 00-.707.293l-2.414 2.414a1 1 0 01-.707.293h-3.172a1 1 0 01-.707-.293l-2.414-2.414A1 1 0 006.586 13H4" />
            </svg>
            <p>No se encontraron categorías registradas</p>
          </div>
        </td>
      </tr>
    `;
    return;
  }

  const rowsHtml = categorias.map(cat => {
    const imagenSrc = cat.imagen && cat.imagen.startsWith('http') ? cat.imagen : null;
    const imagenHtml = imagenSrc 
      ? `<img src="${imagenSrc}" alt="${cat.nombre_categoria}" class="cat-thumb" onerror="this.onerror=null; this.src=''; this.parentElement.innerHTML='<div class=\'cat-thumb\'><svg fill=\'none\' viewBox=\'0 0 24 24\' stroke=\'currentColor\'><path stroke-linecap=\'round\' stroke-linejoin=\'round\' stroke-width=\'2\' d=\'M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z\' /></svg></div>';">`
      : `<div class="cat-thumb"><svg fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" /></svg></div>`;

    return `
      <tr>
        <td><span class="cat-id-badge">#${cat.id_categoria}</span></td>
        <td>${imagenHtml}</td>
        <td class="cat-name">${escapeHtml(cat.nombre_categoria || 'Sin nombre')}</td>
        <td class="cat-desc" title="${escapeHtml(cat.descripcion || '')}">${escapeHtml(cat.descripcion || 'Sin descripción')}</td>
        <td><span class="cat-public-id">${escapeHtml(cat.imagen_public_id || 'N/A')}</span></td>
        <td>
          <div class="actions-cell">
            <button class="btn btn-secondary btn-icon" title="Ver Detalle" onclick="openDetailModal(${cat.id_categoria})">
              <svg fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
              </svg>
            </button>
            <button class="btn btn-danger btn-icon" title="Eliminar Categoría" onclick="openDeleteModal(${cat.id_categoria}, '${escapeHtml(cat.nombre_categoria)}')">
              <svg fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
              </svg>
            </button>
          </div>
        </td>
      </tr>
    `;
  }).join('');

  elements.categoriesTableBody.innerHTML = rowsHtml;
}

function renderizarPaginacion(paginacion) {
  const { pageNumber, totalPages, totalElements, pageSize, isFirst, isLast } = paginacion;

  elements.paginationInfo.innerHTML = `
    Mostrando página <strong>${totalPages === 0 ? 0 : pageNumber + 1}</strong> de <strong>${totalPages}</strong> | Total: <strong>${totalElements}</strong>
  `;

  if (totalPages <= 1) {
    elements.paginationButtons.innerHTML = '';
    return;
  }

  let buttonsHtml = '';

  // Botón Primero y Anterior
  buttonsHtml += `
    <button class="page-btn" ${isFirst ? 'disabled' : ''} onclick="cambiarPagina(0)" title="Primera Página">&laquo;</button>
    <button class="page-btn" ${isFirst ? 'disabled' : ''} onclick="cambiarPagina(${pageNumber - 1})" title="Página Anterior">&lsaquo;</button>
  `;

  // Ventana de páginas numéricas (máximo 5)
  let startPage = Math.max(0, pageNumber - 2);
  let endPage = Math.min(totalPages - 1, startPage + 4);
  if (endPage - startPage < 4) {
    startPage = Math.max(0, endPage - 4);
  }

  for (let i = startPage; i <= endPage; i++) {
    buttonsHtml += `
      <button class="page-btn ${i === pageNumber ? 'active' : ''}" onclick="cambiarPagina(${i})">
        ${i + 1}
      </button>
    `;
  }

  // Botón Siguiente y Último
  buttonsHtml += `
    <button class="page-btn" ${isLast ? 'disabled' : ''} onclick="cambiarPagina(${pageNumber + 1})" title="Página Siguiente">&rsaquo;</button>
    <button class="page-btn" ${isLast ? 'disabled' : ''} onclick="cambiarPagina(${totalPages - 1})" title="Última Página">&raquo;</button>
  `;

  elements.paginationButtons.innerHTML = buttonsHtml;
}

function renderizarPaginacionBusquedaIndividual() {
  elements.paginationInfo.innerHTML = `Mostrando resultado de búsqueda individual (1 registro)`;
  elements.paginationButtons.innerHTML = `
    <button class="btn btn-secondary" onclick="restablecerListado()">
      Ver todo con paginación
    </button>
  `;
}

function restablecerListado() {
  elements.searchInput.value = '';
  state.page = 0;
  cargarCategorias();
}

function cambiarPagina(nuevaPagina) {
  if (nuevaPagina < 0 || nuevaPagina >= state.totalPages) return;
  state.page = nuevaPagina;
  cargarCategorias();
}

function mostrarCargando() {
  elements.categoriesTableBody.innerHTML = `
    <tr>
      <td colspan="6" style="text-align: center; padding: 2.5rem;">
        <div class="loading-spinner"></div>
        <p style="margin-top: 0.75rem; color: var(--text-muted); font-size: 0.9rem;">Cargando categorías desde MySQL...</p>
      </td>
    </tr>
  `;
}

function renderizarErrorEnTabla(mensaje) {
  elements.categoriesTableBody.innerHTML = `
    <tr>
      <td colspan="6">
        <div class="empty-state" style="color: var(--danger);">
          <svg fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
          </svg>
          <p style="font-weight: 600;">${escapeHtml(mensaje)}</p>
        </div>
      </td>
    </tr>
  `;
}

// ================= MODALES =================

function openDeleteModal(id, name) {
  state.deleteTargetId = id;
  elements.deleteCategoryId.textContent = id;
  elements.deleteCategoryName.textContent = name || 'Sin nombre';
  elements.deleteModal.classList.add('active');
}

function closeDeleteModal() {
  state.deleteTargetId = null;
  elements.deleteModal.classList.remove('active');
}

function openDetailModal(id) {
  const cat = state.currentCategories.find(c => c.id_categoria === id);
  if (!cat) return;

  elements.detailId.textContent = `#${cat.id_categoria}`;
  elements.detailName.textContent = cat.nombre_categoria || 'Sin nombre';
  elements.detailDesc.textContent = cat.descripcion || 'Sin descripción disponible';
  elements.detailPublicId.textContent = cat.imagen_public_id || 'N/A';

  if (cat.imagen && cat.imagen.startsWith('http')) {
    elements.detailImage.src = cat.imagen;
    elements.detailImage.style.display = 'block';
  } else {
    elements.detailImage.style.display = 'none';
  }

  elements.detailModal.classList.add('active');
}

function closeDetailModal() {
  elements.detailModal.classList.remove('active');
}

// ================= MANEJO DE ERRORES Y TOASTS =================

function manejarRespuestaError(data, statusCode) {
  const errorTitulo = statusCode === 400 
    ? 'Error 400 - Solicitud Incorrecta' 
    : (statusCode === 500 ? 'Error 500 - Error Interno Servidor' : `Error ${statusCode}`);
  
  const mensaje = data.message || 'Se produjo un error al procesar la solicitud.';
  mostrarToast(statusCode >= 500 ? 'error' : 'warning', errorTitulo, mensaje, statusCode);
}

function mostrarToast(type, title, message, status = null) {
  const toast = document.createElement('div');
  toast.className = `toast toast-${type}`;

  const statusBadge = status ? `<span class="status-tag">${status}</span>` : '';

  let iconSvg = '';
  if (type === 'success') {
    iconSvg = `<svg fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" /></svg>`;
  } else if (type === 'error') {
    iconSvg = `<svg fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" /></svg>`;
  } else {
    iconSvg = `<svg fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" /></svg>`;
  }

  toast.innerHTML = `
    <div class="toast-icon">${iconSvg}</div>
    <div class="toast-body">
      <div class="toast-title">
        <span>${escapeHtml(title)}</span>
        ${statusBadge}
      </div>
      <div class="toast-message">${escapeHtml(message)}</div>
    </div>
    <button class="toast-close" onclick="this.parentElement.remove()">&times;</button>
  `;

  elements.toastContainer.appendChild(toast);

  // Auto-eliminar a los 5 segundos
  setTimeout(() => {
    toast.style.opacity = '0';
    setTimeout(() => toast.remove(), 300);
  }, 5000);
}

function escapeHtml(text) {
  if (!text) return '';
  return text
    .toString()
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;")
    .replace(/'/g, "&#039;");
}
