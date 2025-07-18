let currentOrders = [];
let unpaidOrders = [];
let currentOrderId = null;
let currentRequestId = null;
let requestCounter = 0;
const BASE_URL = "http://localhost:8080/st.cbse.LogisticsCenter.CRM.server/rest-api";



document.addEventListener('DOMContentLoaded', function() {

    const urlParams = new URLSearchParams(window.location.search);
    const customerId = urlParams.get('id');
    if (customerId) {
        sessionStorage.setItem('customerId', customerId);
    }
    

    if (!getCustomerId()) {
        window.location.href = 'login.html';
        return;
    }
    

    loadOrders();
    

    resetOrderForm();
});


async function loadOrders() {
    const ordersList = document.getElementById('orders-list');
    const customerId = getCustomerId();
    
    try {
        const response = await fetch(BASE_URL + `/orders/getOrdersByCustomer/${customerId}`);
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
										<small>Note: ${pr.note}</small><br>
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


async function startOrder() {
    const basePrice = document.getElementById('basePrice').value;
    if (!basePrice) {
        alert('Please enter a base price');
        return;
    }

    try {
		const payload = new URLSearchParams();
		payload.append('customerId', getCustomerId());
		payload.append('price', basePrice);

		const response = await fetch(BASE_URL + '/orders/create', {
		    method: 'POST',
		    headers: {
		        'Content-Type': 'application/x-www-form-urlencoded'
		    },
		    body: payload
		});


        const result = await response.text();

        const uuidMatch = result.match(/[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}/i);
        if (uuidMatch) {
            currentOrderId = uuidMatch[0];
        }
        
        document.getElementById('step-base-price').style.display = 'none';
        document.getElementById('step-print-requests').style.display = 'block';
        requestCounter = 1;
        document.getElementById('request-number').textContent = requestCounter;
        
    } catch (error) {
        alert('Error creating order: ' + error.message);
    }
}

async function addPrintRequest() {
    const stlPath = document.getElementById('stlPath').value;
    const note = document.getElementById('note').value;

    if (!stlPath) {
        alert('Please enter STL file path');
        return;
    }

    try {
        const payload = new URLSearchParams();
        payload.append('orderId', currentOrderId);
        payload.append('stlPath', stlPath);
        payload.append('note', note || '');

        const response = await fetch(BASE_URL + '/orders/addPrintRequest', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            body: payload
        });

        const result = await response.text();
        if (result.startsWith('ERROR:')) {
            throw new Error(result);
        }

        currentRequestId = result;

        document.getElementById('step-print-requests').style.display = 'none';
        document.getElementById('step-options').style.display = 'block';
        document.getElementById('options-request-number').textContent = requestCounter;

    } catch (error) {
        alert('Error adding print request: ' + error.message);
    }
}



function showOptionForm(optionType) {

    document.getElementById('paint-form').style.display = 'none';
    document.getElementById('smoothing-form').style.display = 'none';
    document.getElementById('engraving-form').style.display = 'none';
    

    document.getElementById(optionType + '-form').style.display = 'block';
}

function hideOptionForm(optionType) {
    document.getElementById(optionType + '-form').style.display = 'none';
}

async function addPaintOption() {
    const color = document.getElementById('paintColor').value;
    const layers = document.getElementById('paintLayers').value;

    if (!color || !layers) {
        alert('Please fill all paint details');
        return;
    }

    try {
        const payload = new URLSearchParams();
        payload.append('requestId', currentRequestId);
        payload.append('color', color);
        payload.append('layers', layers);

        const response = await fetch(BASE_URL + '/orders/addPaintOption', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            body: payload
        });

        if (response.ok) {
            alert('Paint option added!');
            document.getElementById('paintColor').value = '';
            document.getElementById('paintLayers').value = '';
            hideOptionForm('paint');
        } else {
            throw new Error(await response.text());
        }

    } catch (error) {
        alert('Error adding paint option: ' + error.message);
    }
}


async function addSmoothingOption() {
    const granularity = document.getElementById('granularity').value;
    
    if (!granularity) {
        alert('Please enter granularity');
        return;
    }
    
    try {
        const payload = new URLSearchParams();
        payload.append('requestId', currentRequestId);
        payload.append('granularity', granularity);

        const response = await fetch('rest-api/orders/addSmoothingOption', {
			    method: 'POST',
			    headers: {
			        'Content-Type': 'application/x-www-form-urlencoded'
			    },
			    body: payload
			});

        if (response.ok) {
            alert('Smoothing option added!');
            document.getElementById('granularity').value = '';
            hideOptionForm('smoothing');
        } else {
            throw new Error(await response.text());
        }
        
    } catch (error) {
        alert('Error adding smoothing option: ' + error.message);
    }
}

async function addEngravingOption() {
    const text = document.getElementById('engravingText').value;
    const font = document.getElementById('engravingFont').value;
    const image = document.getElementById('engravingImage').value;
    
    if (!text || !font) {
        alert('Please enter text and font');
        return;
    }
    
    try {
        const payload = new URLSearchParams();
        payload.append('requestId', currentRequestId);
        payload.append('text', text);
        payload.append('font', font);
        payload.append('image', image || '');

        const response = await fetch('rest-api/orders/addEngravingOption', {
			    method: 'POST',
			    headers: {
			        'Content-Type': 'application/x-www-form-urlencoded'
			    },
			    body: payload
			});

        if (response.ok) {
            alert('Engraving option added!');
            document.getElementById('engravingText').value = '';
            document.getElementById('engravingFont').value = '';
            document.getElementById('engravingImage').value = '';
            hideOptionForm('engraving');
        } else {
            throw new Error(await response.text());
        }
        
    } catch (error) {
        alert('Error adding engraving option: ' + error.message);
    }
}

function finishOptions() {
    document.getElementById('step-options').style.display = 'none';
    document.getElementById('step-confirm').style.display = 'block';
}

function addAnotherRequest() {
    requestCounter++;
    document.getElementById('request-number').textContent = requestCounter;
    document.getElementById('stlPath').value = '';
    document.getElementById('note').value = '';
    
    document.getElementById('step-confirm').style.display = 'none';
    document.getElementById('step-print-requests').style.display = 'block';
}

async function finalizeOrder() {
    try {
        const payload = new URLSearchParams();
        payload.append('orderId', currentOrderId);

        const response = await fetch(BASE_URL + '/orders/finalize', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            body: payload
        });

        if (response.ok) {
            const messageDiv = document.getElementById('create-message');
            messageDiv.className = 'alert alert-success mt-3';
            messageDiv.textContent = '✓ Order submitted successfully!';
            messageDiv.classList.remove('d-none');

            resetOrderForm();
            setTimeout(() => showSection('orders'), 2000);
        } else {
            throw new Error(await response.text());
        }

    } catch (error) {
        alert('Error finalizing order: ' + error.message);
    }
}


function resetOrderForm() {
    currentOrderId = null;
    currentRequestId = null;
    requestCounter = 0;
    
    document.getElementById('basePrice').value = '';
    document.getElementById('step-base-price').style.display = 'block';
    document.getElementById('step-print-requests').style.display = 'none';
    document.getElementById('step-options').style.display = 'none';
    document.getElementById('step-confirm').style.display = 'none';
    document.getElementById('create-message').classList.add('d-none');
}


function loadUnpaidOrders() {
    const selectElement = document.getElementById('unpaidOrders');
    

    unpaidOrders = currentOrders.filter(order => 
        order.status === 'SHIPPED' && order.hasUnpaidInvoice === true
    );
	console.log(currentOrders);


    selectElement.innerHTML = '<option value="">Choose an unpaid invoice...</option>';


    unpaidOrders.forEach(order => {
        const option = document.createElement('option');
        option.value = order.id;
        option.textContent = `Order #${order.id.substring(0, 8)} - €${order.total}`;
        selectElement.appendChild(option);
    });


    if (unpaidOrders.length === 0) {
        selectElement.innerHTML = '<option value="">No unpaid invoices</option>';
    }
}



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
	console.log(order);
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


			async function handlePayOrder(event) {
			    event.preventDefault();
			    
			    const messageDiv = document.getElementById('pay-message');
			    const orderId = document.getElementById('unpaidOrders').value;
			    const transactionRef = document.getElementById('transactionRef').value;
				const formData = new URLSearchParams();
				formData.append('transactionRef', transactionRef);
				formData.append('orderId', orderId); 

			    
			    if (!orderId) {
			        messageDiv.className = 'alert alert-danger mt-3';
			        messageDiv.textContent = 'Please select an order';
			        messageDiv.classList.remove('d-none');
			        return;
			    }
			    
			    try {
					const response = await fetch(`rest-api/customer/pay/${orderId}`, {
					  method: 'POST',
					  headers: {
					    'Content-Type': 'application/x-www-form-urlencoded'
					  },
					  body: formData
					});

			        if (response.ok) {
			            messageDiv.className = 'alert alert-success mt-3';
			            messageDiv.textContent = 'Payment processed successfully!';
			            messageDiv.classList.remove('d-none');
			            

			            document.getElementById('payOrderForm').reset();
			            document.getElementById('order-details').classList.add('d-none');
			            

			            setTimeout(() => {
			                showSection('orders');
			                messageDiv.classList.add('d-none');
			            }, 2000);
			        } else {
			            throw new Error(await response.text());
			        }
			        
			    } catch (error) {
			        console.error('Error processing payment:', error);
			        messageDiv.className = 'alert alert-danger mt-3';
			        messageDiv.textContent = 'Failed to process payment: ' + error.message;
			        messageDiv.classList.remove('d-none');
			    }
			}


			window.startOrder = startOrder;
			window.addPrintRequest = addPrintRequest;
			window.showOptionForm = showOptionForm;
			window.hideOptionForm = hideOptionForm;
			window.addPaintOption = addPaintOption;
			window.addSmoothingOption = addSmoothingOption;
			window.addEngravingOption = addEngravingOption;
			window.finishOptions = finishOptions;
			window.addAnotherRequest = addAnotherRequest;
			window.finalizeOrder = finalizeOrder;
			window.showSection = showSection;
			window.loadOrders = loadOrders;
			window.showOrderDetails = showOrderDetails;
			window.logout = logout;


			document.addEventListener('DOMContentLoaded', function() {
			    const payOrderForm = document.getElementById('payOrderForm');
			    if (payOrderForm) {
			        payOrderForm.addEventListener('submit', handlePayOrder);
			    }
			});