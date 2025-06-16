// Variables globales
let map;
let routingControl = null;
let stationsData = [];
let markersLayer;
let restaurantsData = [];
let restaurantsLayer;
let incidentsData = [];
let incidentsLayer;

// Initialisation de la carte
async function initMap() {
    try {
        const apiUrl = `${CONFIG.get('BAN_API_URL')}?q=${CONFIG.get('DEFAULT_CITY')}&limit=${CONFIG.getInt('BAN_API_LIMIT')}`;
        const response = await fetch(apiUrl);
        const data = await response.json();

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

    loadStationsData();
    loadRestaurantsData();
    loadIncidentsData();
}

// Géolocalisation
function geolocateUser() {
    const btn = document.querySelector('.btn-success-custom');
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
        alert('Géolocalisation impossible : ' + e.message);
        btn.innerHTML = originalText;
        btn.disabled = false;
    });
}

// Rechargement de la carte
function reloadMap() {
    const btn = document.querySelector('.btn-primary-custom');
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

// Afficher toutes les stations
function showAllStations() {
    if (stationsData.length > 0) {
        const group = new L.featureGroup(Object.values(markersLayer._layers));
        map.fitBounds(group.getBounds().pad(0.1));
    }
}

// Création d'un marqueur pour un restaurant
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

// Création du contenu du popup restaurant
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
                                background: var(--gradient-primary);
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
                                background: var(--gradient-secondary);
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
                    
                    <button onclick="afficherReservationForm('${restaurant.nom}', ${restaurant.lat}, ${restaurant.lon})"
                            style="
                                background: var(--gradient-accent);
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

// Traitement des données des restaurants
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
                ...restaurant,
                lat: restaurant.lat,
                lon: restaurant.lon,
                address: `${restaurant.numero_rue}, ${restaurant.rue} - ${restaurant.ville}`
            };

            restaurantsData.push(restaurantData);

            setTimeout(() => {
                createRestaurantMarker(restaurantData);
            }, index * CONFIG.getInt('MARKER_ANIMATION_DELAY'));

        } catch (error) {
            console.error(`Erreur pour le restaurant ${restaurant.nom}:`, error);
        }
    });
}

// Chargement des données des restaurants
async function loadRestaurantsData() {
    try {
        const response = await fetch(CONFIG.get('RESTAURANTS_DATA_FILE'));

        if (!response.ok) {
            throw new Error(`Erreur HTTP: ${response.status}`);
        }

        const result = await response.json();

        if (result.status === 200 && result.data) {
            processRestaurantsData(result.data);
        } else {
            throw new Error('Format de données invalide');
        }
    } catch (error) {
        console.error("Erreur lors du chargement des restaurants:", error);
    }
}

// Afficher le formulaire de réservation
function afficherReservationForm(nomRestaurant, lat, lon) {
    const formHtml = `
        <div class="reservation-form">
            <h5>Réserver une table au ${nomRestaurant}</h5>
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
        .setLatLng([lat, lon])
        .setContent(formHtml)
        .openOn(map);

    document.getElementById('reservationForm').addEventListener('submit', function (e) {
        e.preventDefault();
        const nom = document.getElementById('nom').value;
        const prenom = document.getElementById('prenom').value;
        const telephone = document.getElementById('telephone').value;
        const date = document.getElementById('date').value;

        reserverRestaurant(nomRestaurant, nom, prenom, telephone, date);
        popup.remove();
    });
}

// Réservation restaurant
async function reserverRestaurant(restaurant, nom, prenom, telephone, date) {
    try {
        const response = await fetch(CONFIG.get('RESERVATIONS_ENDPOINT'), {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                restaurant: restaurant,
                nom: nom,
                prenom: prenom,
                telephone: telephone,
                date: date
            })
        });

        const result = await response.json();
        console.log('Réservation effectuée:', result);
    } catch (error) {
        console.error('Erreur lors de la réservation:', error);
    }
}

// Centrer la carte sur un restaurant
function centerOnRestaurant(lat, lon) {
    map.setView([lat, lon], CONFIG.getInt('RESTAURANT_ZOOM'), {
        animate: true,
        duration: 1
    });
}

// Calculer l'itinéraire vers un restaurant
function routeToRestaurant(restaurantName, lat, lon) {
    map.locate({setView: false});

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

        if (typeof showToast === 'function') {
            showToast(`Itinéraire vers ${restaurantName} calculé !`, 'success', '🧭');
        }
    });

    map.once('locationerror', function (e) {
        if (typeof showToast === 'function') {
            showToast('Géolocalisation impossible : ' + e.message, 'error', '❌');
        }
    });
}

// Afficher tous les restaurants
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

// Basculer l'affichage des restaurants
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

// Chargement des données des stations
async function loadStationsData() {
    try {
        const gbfsUrl = `${CONFIG.get('CYCLOCITY_BASE_URL')}${CONFIG.get('CYCLOCITY_GBFS_ENDPOINT')}`;
        const gbfsResponse = await fetch(gbfsUrl);
        const gbfsData = await gbfsResponse.json();
        const stationsUrl = gbfsData.data.fr.feeds.find(feed => feed.name === "station_information").url;

        const stationsResponse = await fetch(stationsUrl);
        const stationsInfo = await stationsResponse.json();

        const statusUrl = `${CONFIG.get('CYCLOCITY_BASE_URL')}${CONFIG.get('CYCLOCITY_STATUS_ENDPOINT')}`;
        const statusResponse = await fetch(statusUrl);
        const statusData = await statusResponse.json();

        processStationsData(stationsInfo.data.stations, statusData.data.stations);

    } catch (error) {
        console.error("Erreur lors du chargement des données:", error);
        alert("Erreur lors du chargement des données des stations");
    }
}

// Traitement et affichage des stations
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

// Création d'un marqueur pour une station
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

// Création du contenu du popup station
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
            throw new Error(`Erreur HTTP: ${response.status}`);
        }
        const data = await response.json();
        console.log('Données incidents reçues:', data);

        // Vérifier le format des données reçues
        if (Array.isArray(data.data)) {
            // Si data est directement un tableau
            processIncidentsData(data.data);
        } else if (data.data.incidents && Array.isArray(data.data.incidents)) {
            // Si data contient une propriété 'incidents' qui est un tableau
            processIncidentsData(data.data.incidents);
        } else {
            console.error('Format inattendu des données incidents:', data.data);
            console.log('Structure reçue:', Object.keys(data.data));
        }
    } catch (error) {
        console.error("Erreur lors du chargement des incidents:", error);
    }
}

function createIncidentMarker(incident) {
    const lat = incident.location.polyline.split(' ')[0];
    const lon = incident.location.polyline.split(' ')[1];
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
                <strong>Date :</strong> ${new Date(incident.date).toLocaleString() || 'Inconnue'}
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

            setTimeout(() => {
                createIncidentMarker(incidentData);
            }, index * CONFIG.getInt('MARKER_ANIMATION_DELAY'));

        } catch (error) {
            console.error(`Erreur pour l'incident ${index}:`, error);
        }
    });
}


// Fonction pour trouver la station la plus proche
function findNearestStation(latlon) {
    if (stationsData.length === 0) {
        return null;
    }

    let nearestStation = null;
    let minDistance = Infinity;

    stationsData.forEach(station => {
        const stationLatLon = L.latLng(station.lat, station.lon);
        const distance = latlon.distanceTo(stationLatLon);

        if (distance < minDistance) {
            minDistance = distance;
            nearestStation = station;
        }
    });

    return nearestStation;
}

// Fonction pour tracer un itinéraire vers la station la plus proche
function routeToNearestStation() {
    map.locate({setView: false, maxZoom: CONFIG.getInt('LOCATION_MAX_ZOOM')});

    map.on('locationfound', function (e) {
        if (routingControl) {
            map.removeControl(routingControl);
        }

        const nearestStation = findNearestStation(e.latlng);
        if (!nearestStation) {
            alert('Aucune station trouvée à proximité.');
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

        console.log('Itinéraire vers la station la plus proche tracé avec succès.');

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
        alert('Géolocalisation impossible : ' + e.message);
    });
}

// Mise à jour des statistiques
function updateStats(totalBikes, totalDocks) {
    document.getElementById('total-bikes').textContent = totalBikes;
    document.getElementById('total-docks').textContent = totalDocks;
}

// Initialisation au chargement de la page
document.addEventListener('DOMContentLoaded', function () {
    initMap();
});
