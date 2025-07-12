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
    event.target.classList.add('active');
    
    // Charger les données selon la section
    if (sectionName === 'orders') {
        loadOrders();
    } else if (sectionName === 'pay') {
        loadUnpaidOrders();
    }
}

// Fonction pour afficher/cacher les options
function toggleOption(optionType) {
    const optionsDiv = document.getElementById(optionType + '-options');
    if (optionsDiv) {
        optionsDiv.style.display = optionsDiv.style.display === 'none' ? 'block' : 'none';
    }
}

// Fonction de déconnexion
function logout() {
    // Supprimer les données de session si nécessaire
    sessionStorage.clear();
    // Rediriger vers la page de login
    window.location.href = 'login.html';
}