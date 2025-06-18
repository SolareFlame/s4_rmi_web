let map;
let routingControl = null;
let stationsData = [];
let markersLayer;
let restaurantsData = [];
let restaurantsLayer;
let incidentsData = [];
let incidentsLayer;

async function initMap() {
    try {
        const apiUrl = `${CONFIG.get('BAN_API_URL')}?q=${CONFIG.get('DEFAULT_CITY')}&limit=1`;
        const response = await fetch(apiUrl);

        if (!response.ok) {
            throw new Error(`Erreur API BAN: ${response.status}`);
        }

        const data = await response.json();

        if (!data.features || data.features.length === 0) {
            throw new Error('Aucune donnée de géolocalisation trouvée');
        }

        const coordinates = data.features[0].geometry.coordinates;
        const lat = coordinates[1];
        const lon = coordinates[0];

        map = L.map('map').setView([lat, lon], CONFIG.getInt('DEFAULT_ZOOM'));
    } catch (error) {
        console.error('Erreur lors de l\'initialisation:', error);
        map = L.map('map').setView([CONFIG.get('DEFAULT_LAT'), CONFIG.get('DEFAULT_LON')], CONFIG.getInt('DEFAULT_ZOOM'));
    }

    L.tileLayer(CONFIG.get('TILE_URL'), {
        maxZoom: CONFIG.getInt('MAX_ZOOM'),
        attribution: CONFIG.get('TILE_ATTRIBUTION')
    }).addTo(map);

    markersLayer = L.layerGroup().addTo(map);

    await Promise.allSettled([
        loadStationsData(),
        loadRestaurantsData(),
        loadIncidentsData()
    ]);
}

function geolocateUser() {
    const btn = document.querySelector('.btn-success');
    const originalText = btn.innerHTML;
    btn.innerHTML = '<span class="loading-spinner me-2"></span>Localisation...';
    btn.disabled = true;

    map.locate({setView: true, maxZoom: CONFIG.getInt('LOCATION_MAX_ZOOM')});

    map.on('locationfound', function (e) {
        L.marker(e.latlng, {
            icon: L.divIcon({
                className: 'user-location-marker',
                html: '<div style="background: #4468ff; width: 20px; height: 20px; border-radius: 50%; border: 3px solid white; box-shadow: 0 2px 5px rgba(0,0,0,0.3);"></div>',
                iconSize: [20, 20],
                iconAnchor: [10, 10]
            })
        }).addTo(map).bindPopup('<strong><i class="fas fa-user-circle me-1"></i>Vous êtes ici !</strong>').openPopup();

        btn.innerHTML = originalText;
        btn.disabled = false;
    });

    map.on('locationerror', function (e) {
        showErrorPopup('Géolocalisation impossible', e.message);
        btn.innerHTML = originalText;
        btn.disabled = false;
    });
}

function reloadMap() {
    const btn = document.querySelector('.btn-primary');
    const reloadText = document.getElementById('reload-text');
    const originalText = reloadText.textContent;

    btn.innerHTML = '<span class="loading-spinner me-2"></span>Rechargement...';
    btn.disabled = true;

    markersLayer.clearLayers();
    loadStationsData();

    setTimeout(() => {
        btn.innerHTML = `<i class="fas fa-sync-alt me-2"></i>${originalText}`;
        btn.disabled = false;
    }, CONFIG.getInt('RELOAD_DELAY'));
}

function showAllStations() {
    if (stationsData.length > 0) {
        const group = new L.featureGroup(Object.values(markersLayer._layers));
        map.fitBounds(group.getBounds().pad(0.1));
    }
}

function createRestaurantMarker(restaurant) {
    const marker = L.marker([restaurant.lat, restaurant.lon], {
        icon: L.divIcon({
            className: 'custom-restaurant-marker',
            html: `
                <div style="
                    background: linear-gradient(135deg, #ff6b6b 0%, #ee5a24 100%);
                    width: 35px;
                    height: 35px;
                    border-radius: 50%;
                    border: 3px solid white;
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    box-shadow: 0 4px 12px rgba(255, 107, 107, 0.4);
                    font-size: 16px;
                    color: white;
                    transition: all 0.3s ease;
                ">
                    🍽️
                </div>
            `,
            iconSize: [35, 35],
            iconAnchor: [17.5, 17.5]
        })
    });

    const popupContent = createRestaurantPopupContent(restaurant);
    marker.bindPopup(popupContent);
    restaurantsLayer.addLayer(marker);
}

function createRestaurantPopupContent(restaurant) {
    return `
        <div style="min-width: 280px; font-family: var(--font-primary);">
            <div style="
                background: linear-gradient(135deg, #ff6b6b 0%, #ee5a24 100%);
                color: white;
                padding: 15px;
                margin: -10px -10px 15px -10px;
                border-radius: 8px 8px 0 0;
                text-align: center;
            ">
                <h4 style="margin: 0; font-size: 1.2em; font-weight: 700;">
                    🍽️ ${restaurant.nom}
                </h4>
            </div>
            
            <div style="padding: 0 5px;">
                <div style="
                    display: flex;
                    align-items: center;
                    gap: 10px;
                    margin-bottom: 12px;
                    padding: 10px;
                    background: #f8f9fa;
                    border-radius: 6px;
                ">
                    <span style="font-size: 1.2em;">📍</span>
                    <div>
                        <div style="font-weight: 600; color: #2c3e50;">
                            ${restaurant.address}
                        </div>
                        <small style="color: #7f8c8d;">
                            ${restaurant.coordonee}
                        </small>
                    </div>
                </div>
                
                <div style="
                    display: grid;
                    grid-template-columns: 1fr 1fr;
                    gap: 10px;
                    margin-top: 15px;
                ">
                    <button onclick="routeToRestaurant('${restaurant.nom}', ${restaurant.lat}, ${restaurant.lon})" 
                            style="
                                background: #0d6efd;
                                color: white;
                                border: none;
                                padding: 8px 12px;
                                border-radius: 6px;
                                cursor: pointer;
                                font-size: 0.85em;
                                font-weight: 600;
                                transition: all 0.3s ease;
                            ">
                        🧭 Itinéraire
                    </button>
                    
                    <button onclick="centerOnRestaurant(${restaurant.lat}, ${restaurant.lon})" 
                            style="
                                background: #0d6efd;
                                color: white;
                                border: none;
                                padding: 8px 12px;
                                border-radius: 6px;
                                cursor: pointer;
                                font-size: 0.85em;
                                font-weight: 600;
                                transition: all 0.3s ease;
                            ">
                        🎯 Centrer
                    </button>
                    
                    <button onclick="afficherReservationForm(${JSON.stringify(restaurant).replace(/"/g, '&quot;')})"
                            style="
                                background: #0d6efd;
                                color: white;
                                border: none;
                                padding: 8px 12px;
                                border-radius: 6px;
                                cursor: pointer;
                                font-size: 0.85em;
                                font-weight: 600;
                                transition: all 0.3s ease;
                            ">
                        📅 Réserver
                    </button>
                </div>
            </div>
        </div>
    `;
}

function processRestaurantsData(restaurants) {
    if (restaurantsLayer) {
        restaurantsLayer.clearLayers();
    } else {
        restaurantsLayer = L.layerGroup().addTo(map);
    }

    restaurantsData = [];

    restaurants.forEach((restaurant, index) => {
        try {
            const restaurantData = {
                id_restau: restaurant.id_restau,
                nom: restaurant.nom || 'Restaurant sans nom',
                address: `${restaurant.numero_rue || ''} ${restaurant.rue || ''}, ${restaurant.ville || ''}`.trim(),
                coordonee: restaurant.lat && restaurant.lon ? `${restaurant.lat}, ${restaurant.lon}` : 'Coordonnées non disponibles',
                lat: parseFloat(restaurant.lat) || CONFIG.getNumber('DEFAULT_LAT'),
                lon: parseFloat(restaurant.lon) || CONFIG.getNumber('DEFAULT_LON')
            };

            restaurantsData.push(restaurantData);

            createRestaurantMarker(restaurantData);

        } catch (error) {
            console.error(`Erreur pour le restaurant ${restaurant.nom || 'inconnu'}:`, error);
        }
    });
}

async function loadRestaurantsData() {
    try {
        const response = await fetch(CONFIG.get('RESTAURANTS_API'));

        if (!response.ok) {
            if (response.status === 500 || response.status === 503) {
                throw new Error(`Service temporairement indisponible (${response.status})`);
            }
            throw new Error(`Erreur serveur: ${response.status}`);
        }

        const result = await response.json();

        if (result.status === 200 && result.data) {
            processRestaurantsData(result.data);
        } else {
            throw new Error(result.error || 'Format de données invalide');
        }
    } catch (error) {
        console.error("Erreur lors du chargement des restaurants:", error);

        if (error.name === 'TypeError' && error.message.includes('fetch')) {
            showErrorPopup('Connexion impossible', 'Impossible de se connecter au serveur des restaurants');
        } else {
            showErrorPopup('Erreur restaurants', error.message);
        }
    }
}

function afficherReservationForm(restaurant) {
    if (typeof restaurant === 'string') {
        restaurant = JSON.parse(restaurant.replace(/&quot;/g, '"'));
    }

    const formHtml = `
        <div class="reservation-form">
            <h5>Réserver une table au ${restaurant.nom}</h5>
            <form id="reservationForm">
                <div class="mb-3">
                    <label for="nom" class="form-label">Nom</label>
                    <input type="text" class="form-control" id="nom" required>
                </div>
                <div class="mb-3">
                    <label for="prenom" class="form-label">Prénom</label>
                    <input type="text" class="form-control" id="prenom" required>
                </div>
                <div class="mb-3">
                    <label for="telephone" class="form-label">Téléphone</label>
                    <input type="tel" class="form-control" id="telephone" required>
                </div>
                <div class="mb-3">
                    <label for="nbPers" class="form-label">Nombre de personnes</label>
                    <input type="number" min="1" class="form-control" id="nbPers" required>
                </div>
                <div class="mb-3">
                    <label for="date" class="form-label">Date et heure</label>
                    <input type="datetime-local" class="form-control" id="date" required>
                </div>
                <button type="submit" class="btn btn-primary">Réserver</button>
            </form>
        </div>
    `;

    const popup = L.popup()
        .setLatLng([restaurant.lat, restaurant.lon])
        .setContent(formHtml)
        .openOn(map);

    document.getElementById('reservationForm').addEventListener('submit', function (e) {
        e.preventDefault();
        const formData = {
            nom: document.getElementById('nom').value,
            prenom: document.getElementById('prenom').value,
            telephone: document.getElementById('telephone').value,
            nbPers: document.getElementById('nbPers').value,
            date: document.getElementById('date').value
        };

        reserverRestaurant(restaurant.id_restau, formData);
        popup.remove();
    });
}

function reserverCreneauAlternatif(id, formData, creneauChoisi) {
    if (window.currentCreneauxModal) {
        window.currentCreneauxModal.hide();
    }

    const dateOriginale = creneauChoisi.split(' ')[0];
    const dateFormatee = `${dateOriginale}T${creneauChoisi.split(' ')[1]}`;

    reserverRestaurant(id, { ...formData, date: dateFormatee });
}

function afficherCreneauxAlternatifs(creneaux, id, formData, dateOriginale) {
    if (!creneaux || creneaux.length === 0) {
        showErrorPopup('Aucune disponibilité', 'Aucun créneau alternatif n\'est disponible pour ce restaurant.');
        return;
    }

    const modalHtml = `
        <div class="modal fade" id="creneauxModal" tabindex="-1">
            <div class="modal-dialog modal-lg">
                <div class="modal-content">
                    <div class="modal-header bg-warning text-white">
                        <h5 class="modal-title">⚠️ Aucune table disponible</h5>
                        <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                    </div>
                    <div class="modal-body">
                        <div class="alert alert-info">
                            <strong>Date demandée :</strong> ${new Date(dateOriginale).toLocaleString('fr-FR')} 
                            pour ${formData.nbPers} personne(s)
                        </div>
                        <h6 class="mb-3">Créneaux alternatifs disponibles :</h6>
                        <div class="row g-2">
                            ${creneaux.map(creneau =>
        `<div class="col-md-6">
                                    <button class="btn btn-outline-primary w-100" 
                                            onclick="reserverCreneauAlternatif('${id}', ${JSON.stringify(formData).replace(/"/g, '&quot;')}, '${creneau}')">
                                        📅 ${new Date(creneau.replace(' ', 'T')).toLocaleString('fr-FR')}
                                    </button>
                                </div>`
    ).join('')}
                        </div>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Annuler</button>
                    </div>
                </div>
            </div>
        </div>
    `;

    document.body.insertAdjacentHTML('beforeend', modalHtml);

    const modal = new bootstrap.Modal(document.getElementById('creneauxModal'));
    window.currentCreneauxModal = modal;
    modal.show();

    document.getElementById('creneauxModal').addEventListener('hidden.bs.modal', function() {
        this.remove();
        window.currentCreneauxModal = null;
    });
}

function showSuccessReservation(reservationData) {
    const modalHtml = `
        <div class="modal fade" id="successModal" tabindex="-1">
            <div class="modal-dialog">
                <div class="modal-content">
                    <div class="modal-header bg-success text-white">
                        <h5 class="modal-title">✅ Réservation confirmée</h5>
                        <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                    </div>
                    <div class="modal-body text-center">
                        <div class="mb-4">
                            <i class="fas fa-check-circle text-success" style="font-size: 3rem;"></i>
                        </div>
                        <h6 class="mb-3">Votre table est réservée !</h6>
                        <div class="card">
                            <div class="card-body">
                                <p class="mb-2"><strong>Table n°:</strong> ${reservationData.numtab}</p>
                                <p class="mb-2"><strong>Date:</strong> ${new Date(reservationData.date + 'T' + reservationData.heure).toLocaleString('fr-FR')}</p>
                                <p class="mb-0"><strong>Référence:</strong> #${reservationData.id || 'N/A'}</p>
                            </div>
                        </div>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-success" data-bs-dismiss="modal">Parfait !</button>
                    </div>
                </div>
            </div>
        </div>
    `;

    document.body.insertAdjacentHTML('beforeend', modalHtml);

    const modal = new bootstrap.Modal(document.getElementById('successModal'));
    modal.show();

    document.getElementById('successModal').addEventListener('hidden.bs.modal', function() {
        this.remove();
    });
}

function showErrorPopup(title, message) {
    const modalHtml = `
        <div class="modal fade" id="errorModal" tabindex="-1">
            <div class="modal-dialog">
                <div class="modal-content">
                    <div class="modal-header bg-danger text-white">
                        <h5 class="modal-title">❌ ${title}</h5>
                        <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                    </div>
                    <div class="modal-body">
                        <div class="alert alert-danger mb-0">
                            ${message}
                        </div>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Fermer</button>
                    </div>
                </div>
            </div>
        </div>
    `;

    document.body.insertAdjacentHTML('beforeend', modalHtml);

    const modal = new bootstrap.Modal(document.getElementById('errorModal'));
    modal.show();

    document.getElementById('errorModal').addEventListener('hidden.bs.modal', function() {
        this.remove();
    });
}

async function reserverRestaurant(id, formData) {
    try {
        const response = await fetch(CONFIG.get('RESTAURANTS_API'), {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                id_restau: id,
                nom: formData.nom,
                prenom: formData.prenom,
                telephone: formData.telephone,
                nb_pers: formData.nbPers,
                date: formData.date
            })
        });

        let result;
        try {
            result = await response.json();
        } catch (jsonError) {
            if (response.status === 500 || response.status === 503) {
                throw new Error('Service de réservation temporairement indisponible');
            }
            throw new Error('Erreur de communication avec le serveur');
        }

        if (response.status === 201) {
            showSuccessReservation(result.data);
        } else if (response.status === 404) {
            afficherCreneauxAlternatifs(result.data, id, formData, formData.date);
        } else if (response.status === 409) {
            showErrorPopup('Table non disponible', 'La table sélectionnée n\'est pas disponible pour cette date et heure.');
        } else if (response.status === 400) {
            showErrorPopup('Capacité insuffisante', 'Aucune table assez grande n\'est disponible pour ce nombre de personnes.');
        } else if (response.status === 500 || response.status === 503) {
            const errorMessage = result.error || 'Service temporairement indisponible';
            showErrorPopup('Erreur serveur', errorMessage);
        } else {
            const errorMessage = result.error || result.message || 'Erreur lors de la réservation';
            showErrorPopup('Erreur de réservation', errorMessage);
        }

    } catch (error) {
        console.error('Erreur lors de la réservation:', error);

        if (error.name === 'TypeError' && error.message.includes('fetch')) {
            showErrorPopup('Connexion impossible', 'Impossible de se connecter au serveur de réservation');
        } else {
            showErrorPopup('Erreur de réservation', error.message);
        }
    }
}

function centerOnRestaurant(lat, lon) {
    map.setView([lat, lon], CONFIG.getInt('RESTAURANT_ZOOM'), {
        animate: true,
        duration: 1
    });
}

function routeToRestaurant(restaurantName, lat, lon) {
    map.locate();

    map.once('locationfound', function (e) {
        if (routingControl) {
            map.removeControl(routingControl);
        }

        routingControl = L.Routing.control({
            waypoints: [
                L.latLng(e.latlng.lat, e.latlng.lng),
                L.latLng(lat, lon)
            ],
            routeWhileDragging: true,
            geocoder: L.Control.Geocoder.nominatim(),
            createMarker: function () { return null; }
        }).addTo(map);
    });

    map.once('locationerror', function (e) {
        showErrorPopup('Géolocalisation impossible', e.message);
    });
}

function showAllRestaurants() {
    if (restaurantsData.length > 0) {
        const group = new L.featureGroup(Object.values(restaurantsLayer._layers));
        map.fitBounds(group.getBounds().pad(0.1));
        if (typeof showToast === 'function') {
            showToast(`${restaurantsData.length} restaurants affichés`, 'info', '🍽️');
        }
    } else {
        if (typeof showToast === 'function') {
            showToast('Aucun restaurant à afficher', 'warning', '⚠️');
        }
    }
}

function toggleRestaurants() {
    if (map.hasLayer(restaurantsLayer)) {
        map.removeLayer(restaurantsLayer);
        if (typeof showToast === 'function') {
            showToast('Restaurants masqués', 'info', '👁️‍🗨️');
        }
    } else {
        map.addLayer(restaurantsLayer);
        if (typeof showToast === 'function') {
            showToast('Restaurants affichés', 'info', '👁️');
        }
    }
}

async function loadStationsData() {
    try {
        const gbfsUrl = `${CONFIG.get('CYCLOCITY_BASE_URL')}${CONFIG.get('CYCLOCITY_GBFS_ENDPOINT')}`;
        const gbfsResponse = await fetch(gbfsUrl);

        if (!gbfsResponse.ok) {
            throw new Error(`Erreur API GBFS: ${gbfsResponse.status}`);
        }

        const gbfsData = await gbfsResponse.json();
        const stationsUrl = gbfsData.data.fr.feeds.find(feed => feed.name === "station_information").url;

        const [stationsResponse, statusResponse] = await Promise.all([
            fetch(stationsUrl),
            fetch(`${CONFIG.get('CYCLOCITY_BASE_URL')}${CONFIG.get('CYCLOCITY_STATUS_ENDPOINT')}`)
        ]);

        if (!stationsResponse.ok || !statusResponse.ok) {
            throw new Error('Erreur lors du chargement des données des stations');
        }

        const [stationsInfo, statusData] = await Promise.all([
            stationsResponse.json(),
            statusResponse.json()
        ]);

        processStationsData(stationsInfo.data.stations, statusData.data.stations);

    } catch (error) {
        console.error("Erreur lors du chargement des données:", error);
        showErrorPopup('Erreur stations', 'Impossible de charger les données des stations de vélos');
    }
}

function processStationsData(stations, statuses) {
    const statusMap = new Map(statuses.map(s => [s.station_id, s]));
    let totalBikes = 0;
    let totalDocks = 0;

    markersLayer.clearLayers();
    stationsData = [];

    stations.forEach(station => {
        const status = statusMap.get(station.station_id);
        if (status) {
            const stationData = {
                ...station,
                bikes_available: status.num_bikes_available,
                docks_available: status.num_docks_available,
                is_renting: status.is_renting,
                is_returning: status.is_returning
            };

            stationsData.push(stationData);
            totalBikes += status.num_bikes_available;
            totalDocks += status.num_docks_available;

            createStationMarker(stationData);
        }
    });

    updateStats(totalBikes, totalDocks);
}

function createStationMarker(station) {
    const cbText = station.rental_methods && station.rental_methods.includes('creditcard') ? ' (CB)' : '';
    const isOperational = station.is_renting && station.is_returning;

    let markerColor = '#6c757d';
    if (isOperational) {
        if (station.bikes_available === 0) {
            markerColor = '#dc3545';
        } else if (station.bikes_available <= 3) {
            markerColor = '#ffc107';
        } else {
            markerColor = '#198754';
        }
    }

    const marker = L.marker([station.lat, station.lon], {
        icon: L.divIcon({
            className: 'custom-station-marker',
            html: `
                <div style="
                    background: ${markerColor};
                    width: 30px;
                    height: 30px;
                    border-radius: 50%;
                    border: 3px solid white;
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    box-shadow: 0 2px 8px rgba(0,0,0,0.3);
                    font-size: 12px;
                    font-weight: bold;
                    color: white;
                ">
                    ${station.bikes_available}
                </div>
            `,
            iconSize: [30, 30],
            iconAnchor: [15, 15]
        })
    });

    const popupContent = createStationPopupContent(station, cbText, isOperational);
    marker.bindPopup(popupContent);
    markersLayer.addLayer(marker);
}

function createStationPopupContent(station, cbText, isOperational) {
    return `
        <div style="min-width: 250px;">
            <h5 style="margin-bottom: 10px; color: #212529;">
                <i class="fas fa-bicycle me-2"></i>
                ${station.name}${cbText}
            </h5>
            <p style="margin-bottom: 10px; color: #6c757d;">
                <i class="fas fa-map-marker-alt me-2"></i>
                ${station.address || 'Adresse non renseignée'}
            </p>
            <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 10px; margin: 15px 0;">
                <div style="text-align: center; padding: 10px; background: #e8f5e8; border-radius: 8px;">
                    <div style="font-size: 1.5em; font-weight: bold; color: #198754;">
                        ${station.bikes_available}
                    </div>
                    <small style="color: #198754;">Vélos disponibles</small>
                </div>
                <div style="text-align: center; padding: 10px; background: #e3f2fd; border-radius: 8px;">
                    <div style="font-size: 1.5em; font-weight: bold; color: #1976d2;">
                        ${station.docks_available}
                    </div>
                    <small style="color: #1976d2;">Places libres</small>
                </div>
            </div>
            <div style="text-align: center; margin-top: 10px;">
                <span style="
                    padding: 5px 10px;
                    border-radius: 15px;
                    font-size: 0.8em;
                    font-weight: bold;
                    background: ${isOperational ? '#d4edda' : '#f8d7da'};
                    color: ${isOperational ? '#155724' : '#721c24'};
                ">
                    ${isOperational ? 'Station opérationnelle' : 'Station hors service'}
                </span>
            </div>
            <small style="display: block; text-align: center; margin-top: 8px; color: #6c757d;">
                Capacité totale: ${station.capacity} places
            </small>
        </div>
    `;
}

async function loadIncidentsData() {
    try {
        const response = await fetch(CONFIG.get('INCIDENTS_API_URL'));

        if (!response.ok) {
            if (response.status === 500 || response.status === 503) {
                throw new Error(`Service d'incidents temporairement indisponible (${response.status})`);
            }
            throw new Error(`Erreur serveur incidents: ${response.status}`);
        }

        const data = await response.json();

        if (Array.isArray(data.data)) {
            processIncidentsData(data.data);
        } else if (data.data.incidents && Array.isArray(data.data.incidents)) {
            processIncidentsData(data.data.incidents);
        } else {
            console.error('Format inattendu des données incidents:', data.data);
        }
    } catch (error) {
        console.error("Erreur lors du chargement des incidents:", error);

        if (error.name === 'TypeError' && error.message.includes('fetch')) {
            console.warn('Service d\'incidents non disponible');
        } else {
            console.warn('Erreur incidents:', error.message);
        }
    }
}

function createIncidentMarker(incident) {
    let lat, lon;

    if (incident.location && incident.location.polyline) {
        if (typeof incident.location.polyline === 'string') {
            const coords = incident.location.polyline.split(' ');
            lat = parseFloat(coords[0]);
            lon = parseFloat(coords[1]);
        } else if (Array.isArray(incident.location.polyline)) {
            lat = parseFloat(incident.location.polyline[0]);
            lon = parseFloat(incident.location.polyline[1]);
        }
    }

    if (isNaN(lat) || isNaN(lon)) {
        console.warn('Coordonnées invalides pour l\'incident:', incident);
        return;
    }

    const marker = L.marker([lat, lon], {
        icon: L.divIcon({
            className: 'custom-incident-marker',
            html: `
                <div style="
                    background: rgba(255, 0, 0, 0.8);
                    width: 30px;
                    height: 30px;
                    border-radius: 50%;
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    box-shadow: 0 2px 8px rgba(0,0,0,0.3);
                    color: white;
                    font-size: 16px;
                ">
                    <i class="fas fa-exclamation-triangle"></i>
                </div>
            `,
            iconSize: [30, 30],
            iconAnchor: [15, 15]
        })
    });

    const popupContent = createIncidentPopupContent(incident);
    marker.bindPopup(popupContent);
    incidentsLayer.addLayer(marker);
}

function createIncidentPopupContent(incident) {
    return `
        <div style="min-width: 250px;">
            <h5 style="margin-bottom: 10px; color: #212529;">
                <i class="fas fa-exclamation-triangle me-2"></i>
                Incident signalé
            </h5>
            <p style="margin-bottom: 10px; color: #6c757d;">
                <strong>Type :</strong> ${incident.type || 'Inconnu'}
            </p>
            <p style="margin-bottom: 10px; color: #6c757d;">
                <strong>Description :</strong> ${incident.description || 'Aucune description fournie'}
            </p>
            <p style="margin-bottom: 10px; color: #6c757d;">
                <strong>Date :</strong> ${incident.date ? new Date(incident.date).toLocaleString() : 'Inconnue'}
            </p>
        </div>
    `;
}

function processIncidentsData(incidents) {
    if (incidentsLayer) {
        incidentsLayer.clearLayers();
    } else {
        incidentsLayer = L.layerGroup().addTo(map);
    }

    incidentsData = [];

    incidents.forEach((incident, index) => {
        try {
            const incidentData = {
                ...incident,
                location: incident.location || { polyline: [CONFIG.get('DEFAULT_LAT'), CONFIG.get('DEFAULT_LON')] }
            };

            incidentsData.push(incidentData);

            createIncidentMarker(incidentData);

        } catch (error) {
            console.error(`Erreur pour l'incident ${index}:`, error);
        }
    });
}

function findNearestStation(latlng) {
    if (stationsData.length === 0) {
        return null;
    }

    let nearestStation = null;
    let minDistance = Infinity;

    stationsData.forEach(station => {
        const stationLatLng = L.latLng(station.lat, station.lon);
        const distance = latlng.distanceTo(stationLatLng);

        if (distance < minDistance) {
            minDistance = distance;
            nearestStation = station;
        }
    });

    return nearestStation;
}

function routeToNearestStation() {
    map.locate({setView: false, maxZoom: CONFIG.getInt('LOCATION_MAX_ZOOM')});

    map.on('locationfound', function (e) {
        if (routingControl) {
            map.removeControl(routingControl);
        }

        const nearestStation = findNearestStation(e.latlng);
        if (!nearestStation) {
            showErrorPopup('Station introuvable', 'Aucune station trouvée à proximité.');
            return;
        }

        routingControl = L.Routing.control({
            waypoints: [
                L.latLng(e.latlng.lat, e.latlng.lng),
                L.latLng(nearestStation.lat, nearestStation.lon)
            ],
            routeWhileDragging: true,
            geocoder: L.Control.Geocoder.nominatim(),
            createMarker: function () { return null; }
        }).addTo(map);

        const popupContent = `
            <strong><i class="fas fa-bicycle me-1"></i>Station la plus proche :</strong><br>
            ${nearestStation.name} (${nearestStation.bikes_available} vélos disponibles)
        `;
        L.popup()
            .setLatLng([nearestStation.lat, nearestStation.lon])
            .setContent(popupContent)
            .openOn(map);
    });

    map.on('locationerror', function (e) {
        showErrorPopup('Géolocalisation impossible', e.message);
    });
}

function updateStats(totalBikes, totalDocks) {
    const bikesElement = document.getElementById('total-bikes');
    const docksElement = document.getElementById('total-docks');

    if (bikesElement) bikesElement.textContent = totalBikes;
    if (docksElement) docksElement.textContent = totalDocks;
}

document.addEventListener('DOMContentLoaded', function () {
    initMap();
});
