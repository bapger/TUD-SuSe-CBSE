const BASE_URL = "http://localhost:8080/st.cbse.LogisticsCenter.CRM.server/rest-api";

function loadAllProcesses() {
  const list = document.getElementById("process-list");
  list.innerHTML = "Loading...";

  fetch(`${BASE_URL}/production/processes`)
    .then(res => res.json())
    .then(data => {
      const processes = data.processes || [];
      if (!processes.length) {
        list.innerHTML = "<div class='alert alert-info'>No processes found.</div>";
        return;
      }

      list.innerHTML = processes.map(p => `
		
        <div class="card mb-3">
          <div class="card-body">
            <p><strong>ID:</strong> ${p.id}</p>
            <p><strong>Status:</strong> <span class="badge bg-secondary">${p.status}</span></p>
            <p><strong>Progress:</strong> ${p.progressPercentage}%</p>
            <div class="d-flex gap-2">
              ${p.status === "IN_PROGRESS" ? `<button class="btn btn-sm btn-warning" onclick="pauseProcess('${p.id}')">Pause</button>` : ''}
              ${p.status === "PAUSED" ? `<button class="btn btn-sm btn-success" onclick="resumeProcess('${p.id}')">Resume</button>` : ''}
              ${["PENDING", "RUNNING", "PAUSED"].includes(p.status) ? `<button class="btn btn-sm btn-danger" onclick="cancelProcess('${p.id}')">Cancel</button>` : ''}
            </div>
          </div>
        </div>
      `).join('');
    })
    .catch(err => {
      list.innerHTML = `<div class="alert alert-danger">Error loading processes: ${err.message}</div>`;
    });
}

function pauseProcess(id) {
  fetch(`${BASE_URL}/production/processes/${id}/pause`, { method: "POST" })
    .then(() => {
      alert("Paused.");
      loadAllProcesses();
    })
    .catch(err => alert("Error: " + err.message));
}

function resumeProcess(id) {
  fetch(`${BASE_URL}/production/processes/${id}/resume`, { method: "POST" })
    .then(() => {
      alert("Resumed.");
      loadAllProcesses();
    })
    .catch(err => alert("Error: " + err.message));
}

function cancelProcess(id) {
  if (!confirm("Are you sure to cancel this process?")) return;
  fetch(`${BASE_URL}/production/processes/${id}/cancel`, { method: "POST" })
    .then(() => {
      alert("Cancelled.");
      loadAllProcesses();
    })
    .catch(err => alert("Error: " + err.message));
}
