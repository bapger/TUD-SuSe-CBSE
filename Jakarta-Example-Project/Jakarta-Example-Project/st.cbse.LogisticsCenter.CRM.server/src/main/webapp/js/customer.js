// Variables globales
let currentOrders = [];
let unpaidOrders = [];

// Charger automatiquement les commandes au chargement de la page
document.addEventListener('DOMContentLoaded', function() {
    loadOrders();
    
    // Setup des formulaires
    document.getElementById('createOrderForm').addEventListener('submit', handleCreateOrder);
    document.getElementById('payOrderForm').addEventListener('submit', handlePayOrder);
});

// Fonction pour charger les commandes
async function loadOrders() {
    const ordersList = document.getElementById('orders-list');
    const customerId = getCustomerId();
    
    try {
        const response = await fetch(`rest-api/orders/getOrdersByCustomer/${customerId}`);
        const data = await response.json();
        
        if (data.error) {
            ordersList.innerHTML = `<div class="alert alert-danger">${data.error}</div>`;
            return;
        }
        
        currentOrders = data.orders || [];
        displayOrders(currentOrders);
        
    } catch (error) {
        console.error('Error loading orders:', error);
        ordersList.innerHTML = '<div class="alert alert-danger">Failed to load orders</div>';
    }
}

// Fonction pour afficher les commandes
function displayOrders(orders) {
    const ordersList = document.getElementById('orders-list');
    
    if (!orders || orders.length === 0) {
        ordersList.innerHTML = '<div class="alert alert-info">No orders found</div>';
        return;
    }
    
    let html = '<div class="row">';
    
    orders.forEach(order => {
        const statusClass = getStatusClass(order.status);
        html += `
            <div class="col-md-6 mb-3">
                <div class="card">
                    <div class="card-header">
                        <div class="d-flex justify-content-between align-items-center">
                            <h5 class="mb-0">Order #${order.id.substring(0, 8)}</h5>
                            <span class="badge ${statusClass}">${order.status}</span>
                        </div>
                    </div>
                    <div class="card-body">
                        <p class="mb-2"><strong>Date:</strong> ${formatDate(order.creationDate)}</p>
                        <p class="mb-2"><strong>Total:</strong> €${order.total}</p>
                        <p class="mb-2"><strong>Customer:</strong> ${order.customerName}</p>
                        
                        ${order.printingRequests && order.printingRequests.length > 0 ? `
                            <hr>
                            <h6>Printing Requests:</h6>
                            <ul class="list-unstyled">
                                ${order.printingRequests.map(pr => `
                                    <li class="mb-2">
                                        <small>ID: ${pr.ID.substring(0, 8)}</small><br>
                                        <small>Price: €${pr.price}</small>
                                        ${pr.options && pr.options.options && pr.options.options.length > 0 ? `
                                            <br><small>Options: ${pr.options.options.map(opt => opt.type).join(', ')}</small>
                                        ` : ''}
                                    </li>
                                `).join('')}
                            </ul>
                        ` : ''}
                    </div>
                </div>
            </div>
        `;
    });
    
    html += '</div>';
    ordersList.innerHTML = html;
}

// Fonction pour obtenir la classe CSS selon le statut
function getStatusClass(status) {
    const statusClasses = {
        'PENDING': 'bg-warning text-dark',
        'PAID': 'bg-info text-white',
        'IN_PRODUCTION': 'bg-primary',
        'FINISHED': 'bg-success',
        'SHIPPED': 'bg-secondary'
    };
    return statusClasses[status] || 'bg-secondary';
}

// Fonction pour formater la date
function formatDate(dateString) {
    try {
        const date = new Date(dateString);
        return date.toLocaleDateString() + ' ' + date.toLocaleTimeString();
    } catch (e) {
        return dateString;
    }
}

// Fonction pour gérer la création de commande
async function handleCreateOrder(event) {
    event.preventDefault();
    
    const messageDiv = document.getElementById('create-message');
    const customerId = urlParams.get('id');
    
    // Récupérer les valeurs du formulaire
    const formData = new FormData();
    formData.append('customerId', customerId);
    formData.append('price', document.getElementById('basePrice').value);
    
    try {
        const response = await fetch('rest-api/orders/createOrderForm', {
            method: 'POST',
            body: formData
        });
        
        const result = await response.text();
        
        messageDiv.className = 'alert alert-success mt-3';
        messageDiv.textContent = result;
        messageDiv.classList.remove('d-none');
        
        // Réinitialiser le formulaire
        document.getElementById('createOrderForm').reset();
        
        // Recharger les commandes après 2 secondes
        setTimeout(() => {
            showSection('orders');
            messageDiv.classList.add('d-none');
        }, 2000);
        
    } catch (error) {
        console.error('Error creating order:', error);
        messageDiv.className = 'alert alert-danger mt-3';
        messageDiv.textContent = 'Failed to create order';
        messageDiv.classList.remove('d-none');
    }
}

// Fonction pour charger les commandes non payées
function loadUnpaidOrders() {
    const selectElement = document.getElementById('unpaidOrders');
    
    // Filtrer les commandes avec statut PENDING
    unpaidOrders = currentOrders.filter(order => order.status === 'PENDING');
    
    // Réinitialiser le select
    selectElement.innerHTML = '<option value="">Choose an order...</option>';
    
    // Ajouter les options
    unpaidOrders.forEach(order => {
        const option = document.createElement('option');
        option.value = order.id;
        option.textContent = `Order #${order.id.substring(0, 8)} - €${order.total}`;
        selectElement.appendChild(option);
    });
    
    if (unpaidOrders.length === 0) {
        selectElement.innerHTML = '<option value="">No unpaid orders</option>';
    }
}

// Fonction pour afficher les détails de la commande sélectionnée
function showOrderDetails() {
    const selectElement = document.getElementById('unpaidOrders');
    const orderId = selectElement.value;
    const detailsDiv = document.getElementById('order-details');
    const orderInfoDiv = document.getElementById('order-info');
    const orderTotalDiv = document.getElementById('order-total');
    
    if (!orderId) {
        detailsDiv.classList.add('d-none');
        return;
    }
    
    const order = unpaidOrders.find(o => o.id === orderId);
    if (order) {
        orderInfoDiv.innerHTML = `
            <p class="mb-1">Order ID: ${order.id.substring(0, 8)}</p>
            <p class="mb-1">Date: ${formatDate(order.creationDate)}</p>
            <p class="mb-1">Status: ${order.status}</p>
        `;
        orderTotalDiv.textContent = `Total: €${order.total}`;
        detailsDiv.classList.remove('d-none');
    }
}

// Fonction pour gérer le paiement
async function handlePayOrder(event) {
    event.preventDefault();
    
    const messageDiv = document.getElementById('pay-message');
    const orderId = document.getElementById('unpaidOrders').value;
    const transactionRef = document.getElementById('transactionRef').value;
    
    if (!orderId) {
        messageDiv.className = 'alert alert-danger mt-3';
        messageDiv.textContent = 'Please select an order';
        messageDiv.classList.remove('d-none');
        return;
    }
    
    try {
        // Ici, vous devriez appeler votre API de paiement
        // Pour l'instant, on simule le succès
        
        messageDiv.className = 'alert alert-success mt-3';
        messageDiv.textContent = 'Payment processed successfully!';
        messageDiv.classList.remove('d-none');
        
        // Réinitialiser le formulaire
        document.getElementById('payOrderForm').reset();
        document.getElementById('order-details').classList.add('d-none');
        
        // Recharger les commandes après 2 secondes
        setTimeout(() => {
            showSection('orders');
            messageDiv.classList.add('d-none');
        }, 2000);
        
    } catch (error) {
        console.error('Error processing payment:', error);
        messageDiv.className = 'alert alert-danger mt-3';
        messageDiv.textContent = 'Failed to process payment';
        messageDiv.classList.remove('d-none');
    }
}