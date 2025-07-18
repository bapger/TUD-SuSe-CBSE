const BASE_URL = "http://localhost:8080/st.cbse.LogisticsCenter.CRM.server/rest-api";

document.addEventListener("DOMContentLoaded", () => {
  loadManagerOrders();
});

function loadManagerOrders() {
  fetch(`${BASE_URL}/manager/orders`)
    .then(res => res.json())
    .then(data => {
      console.log("Orders loaded:", data.orders);
      const orders = data.orders || [];
      renderOrders(document.getElementById("orders-list"), orders);
    })
    .catch(err => {
      console.error("Error loading orders:", err);
      document.getElementById("orders-list").innerHTML =
        "<div class='alert alert-danger'>Failed to load orders</div>";
    });
}

function renderOrders(container, orders) {
  if (!orders.length) {
    container.innerHTML = `<div class="alert alert-info">No orders found</div>`;
    return;
  }

  container.innerHTML = orders.map((o, i) => {
    const canShip = o.status === "FINISHED";
    const canSendToProd = o.status === "COMPLETED";

    return `
      <div class="card mb-4">
        <div class="card-header d-flex justify-content-between align-items-center">
          <div>
            <strong>Order [${i + 1}]</strong> - <span class="text-muted">${o.id.substring(0, 8)}</span>
          </div>
          <span class="badge ${getStatusClass(o.status)}">${o.status}</span>
        </div>
        <div class="card-body">
          <p><strong>Customer:</strong> ${o.customerName}</p>
          <p><strong>Total:</strong> €${o.total}</p>
          <p><strong>Date:</strong> ${formatDate(o.creationDate)}</p>

          ${o.printingRequests && o.printingRequests.length > 0 ? `
            <h6 class="mt-3">Printing Requests:</h6>
            <ul class="list-group mb-3">
              ${o.printingRequests.map(pr => `
                <li class="list-group-item">
                  <strong>ID:</strong> ${pr.ID.substring(0, 8)}<br>
                  <strong>STL:</strong> ${pr.stlPath || 'N/A'}<br>
                  <strong>Note:</strong> ${pr.note || '—'}<br>
                  <strong>Price:</strong> €${pr.price}<br>
                  ${pr.options && pr.options.length > 0 ? `
                    <strong>Options:</strong> ${pr.options.map(opt => opt.type).join(', ')}<br>
                  ` : ''}
                  <button class="btn btn-sm btn-outline-primary mt-2" onclick="addNoteToRequest('${pr.ID}')">
                    Add Note
                  </button>
                </li>
              `).join('')}
            </ul>
          ` : '<p>No printing requests</p>'}

          <div class="mt-3 d-flex gap-2">
            ${canSendToProd ? `
              <button class="btn btn-sm btn-warning" onclick="sendToProduction('${o.id}')">
                Send to Production
              </button>` : ''}
            ${canShip ? `
              <button class="btn btn-sm btn-success" onclick="shipOrder('${o.id}', '${o.total}')">
                Ship Order
              </button>` : ''}
          </div>
        </div>
      </div>
    `;
  }).join('');
}

async function addNoteToRequest(requestId) {
  const note = prompt("Enter a note:");
  if (!note) return;

  try {
    await fetch(`${BASE_URL}/manager/addNote/${requestId}`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      body: `note=${encodeURIComponent(note)}`
    });
    alert("Note added.");
    loadManagerOrders();
  } catch (e) {
    alert("Error adding note: " + e.message);
  }
}

async function sendToProduction(orderId) {
  try {
    await fetch(`${BASE_URL}/manager/sendToProduction/${orderId}`, {
      method: 'POST'
    });
    alert("Order sent to production.");
    loadManagerOrders();
  } catch (e) {
    alert("Failed to send to production: " + e.message);
  }
}

async function shipOrder(orderId, total) {
  try {
    await fetch(`${BASE_URL}/manager/ship/${orderId}`, { method: 'POST' });
    alert(`Order shipped and invoice created for €${total}`);
    loadManagerOrders();
  } catch (e) {
    alert("Error shipping order: " + e.message);
  }
}

function loadStorage() {
  const container = document.getElementById("storage-list");
  container.innerHTML = `<div class="text-muted">Loading...</div>`;

  fetch(`${BASE_URL}/manager/storage`)
    .then(res => res.json())
    .then(data => {
      const storedItems = (data.items || []).filter(item => !item.shipped); // Exclude shipped

      if (storedItems.length === 0) {
        container.innerHTML = `<div class="alert alert-info">Storage is empty</div>`;
        return;
      }

      container.innerHTML = `
        <ul class="list-group">
          ${storedItems.map(item => `
            <li class="list-group-item">
              <strong>Request ID:</strong> ${item.printRequestId}<br>
              <strong>Status:</strong> Stored<br>
              <strong>Completed At:</strong> ${formatDate(item.completedAt)}<br>
              ${item.itemInfo ? `
                <strong>Location:</strong> ${item.itemInfo.currentLocation}<br>
                <strong>Item Status:</strong> ${item.itemInfo.status}<br>
              ` : ''}
            </li>
          `).join('')}
        </ul>
      `;
    })
    .catch(err => {
      container.innerHTML = `<div class="alert alert-danger">Failed to load storage: ${err.message}</div>`;
    });
}

function formatDate(isoDate) {
  const date = new Date(isoDate);
  return date.toLocaleDateString() + ' ' + date.toLocaleTimeString();
}

function getStatusClass(status) {
  switch (status) {
    case 'PENDING': return 'bg-warning';
    case 'COMPLETED': return 'bg-primary';
    case 'IN_PROD': return 'bg-info';
    case 'FINISHED': return 'bg-success';
    case 'SHIPPED': return 'bg-secondary';
    default: return 'bg-dark';
  }
}
