// Fonction pour récupérer l'ID du client
function getCustomerId() {
    return sessionStorage.getItem('customerId');
}

// Fonction pour afficher/cacher les sections
function showSection(sectionName) {
    // Cacher toutes les sections
    document.querySelectorAll('.section').forEach(section => {
        section.style.display = 'none';
    });
    
    // Enlever la classe active de tous les liens
    document.querySelectorAll('.nav-link').forEach(link => {
        link.classList.remove('active');
    });
    
    // Afficher la section demandée
    const section = document.getElementById(sectionName + '-section');
    if (section) {
        section.style.display = 'block';
    }
    
    // Ajouter la classe active au lien correspondant
    if (event && event.target) {
        event.target.classList.add('active');
    }
    
    // Charger les données selon la section
    if (sectionName === 'orders') {
        loadOrders();
    } else if (sectionName === 'pay') {
        loadUnpaidOrders();
    } else if (sectionName === 'new-order') {
        resetOrderForm();
    }
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

// Fonction de déconnexion
function logout() {
    sessionStorage.clear();
    window.location.href = 'login.html';
}