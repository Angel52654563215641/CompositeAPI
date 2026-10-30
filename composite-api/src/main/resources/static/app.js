const searchForm = document.querySelector('#product-search');
const productInput = document.querySelector('#product-id');
const errorMessage = document.querySelector('#error-message');
const searchButton = searchForm.querySelector('button');
const quickPicks = document.querySelectorAll('.quick-pick');

const formatCurrency = (amount, currency) => new Intl.NumberFormat('es-MX', {
    style: 'currency',
    currency,
    minimumFractionDigits: 2
}).format(amount);

const formatDate = (value) => {
    if (!value) return '—';
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) return value;
    return new Intl.DateTimeFormat('es-MX', {
        dateStyle: 'medium',
        timeStyle: 'short'
    }).format(date);
};

const setText = (selector, value) => {
    document.querySelector(selector).textContent = value ?? '—';
};

const describeError = async (response) => {
    try {
        const body = await response.json();
        return body.detail || body.message || body.error || `La consulta falló (HTTP ${response.status}).`;
    } catch (error) {
        if (!(error instanceof SyntaxError)) throw error;
        return `La consulta falló (HTTP ${response.status}).`;
    }
};

const setLoading = (loading) => {
    searchButton.disabled = loading;
    searchButton.textContent = loading ? 'Buscando…' : 'Buscar ↗';
    if (loading) setText('#query-status', 'Consultando');
};

const showProduct = (product) => {
    setText('#price-mxn', formatCurrency(product.price_mxn, 'MXN'));
    setText('#exchange-rate', `Tipo de cambio: ${Number(product.exchange_rate_mxn).toFixed(4)} MXN / USD`);
    setText('#available-units', String(product.available_units));
    setText('#stock-status', product.stock_status || 'Estado de inventario');
    setText('#product-category', product.category);
    setText('#estimated-delivery', product.estimated_delivery);
    setText('#detail-product-id', `PRODUCTO ${product.product_id}`);
    setText('#product-title', product.title);
    setText('#product-description', product.description);
    setText('#price-usd', formatCurrency(product.price_usd, 'USD'));
    setText('#warehouse-location', product.warehouse_location);
    setText('#conversion-source', product.currency_conversion_source);
    setText('#query-status', product.status);
    setText('#updated-at', `Actualizado ${formatDate(product.generated_at)}`);
    setText('#generated-at', `Generado ${formatDate(product.generated_at)}`);

    const stockBadge = document.querySelector('#stock-badge');
    const stockLabel = document.querySelector('#stock-badge-label');
    const stockStatus = String(product.stock_status || '').toUpperCase();
    stockLabel.textContent = stockStatus.replaceAll('_', ' ') || 'Sin datos';
    stockBadge.classList.toggle('out-of-stock', stockStatus.includes('OUT') || stockStatus.includes('NO_STOCK'));
    stockBadge.classList.toggle('low-stock', stockStatus.includes('LOW'));

    const productIndex = [...quickPicks].findIndex((button) => button.dataset.productId === product.product_id);
    const indexLabel = productIndex < 0 ? '—' : String(productIndex + 1).padStart(2, '0');
    setText('.visual-index', `${indexLabel} / ${String(quickPicks.length).padStart(2, '0')}`);
    errorMessage.hidden = true;
};

const loadProduct = async (productId) => {
    const id = productId.trim();
    if (!id) {
        errorMessage.textContent = 'Escribe el ID de un producto para realizar la búsqueda.';
        errorMessage.hidden = false;
        productInput.focus();
        return;
    }

    productInput.value = id;
    quickPicks.forEach((button) => {
        button.classList.toggle('selected', button.dataset.productId === id);
    });
    setLoading(true);
    errorMessage.hidden = true;

    try {
        const response = await fetch(`/api/v1/products/${encodeURIComponent(id)}/summary`, {
            headers: { Accept: 'application/json' }
        });
        if (!response.ok) {
            throw new Error(await describeError(response));
        }

        const product = await response.json();
        if (!product || product.status !== 'SUCCESS') {
            throw new Error('El servicio devolvió una respuesta incompleta o inesperada.');
        }
        showProduct(product);
    } catch (error) {
        errorMessage.textContent = error instanceof TypeError
            ? 'No fue posible conectar con la API. Verifica que los servicios estén activos e inténtalo de nuevo.'
            : error.message;
        errorMessage.hidden = false;
        setText('#query-status', 'Error');
    } finally {
        setLoading(false);
    }
};

searchForm.addEventListener('submit', (event) => {
    event.preventDefault();
    loadProduct(productInput.value);
});

quickPicks.forEach((button) => {
    button.addEventListener('click', () => loadProduct(button.dataset.productId));
});

document.querySelector('#today-label').textContent = new Intl.DateTimeFormat('es-MX', {
    weekday: 'short',
    day: 'numeric',
    month: 'short',
    year: 'numeric'
}).format(new Date());

loadProduct(productInput.value);
