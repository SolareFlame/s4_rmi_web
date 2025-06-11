const key = 'vhRbPodpE7gzeB4RwXuF';
let map;
let stationsData = [];
let markersLayer;

// Initialisation de la carte
function initMap() {
    map = L.map('map').setView([48.6937223, 6.1834097], 14);

    L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
        maxZoom: 19,
        attribution: '&copy; <a href="http://www.openstreetmap.org/copyright">OpenStreetMap</a>'
    }).addTo(map);

    markersLayer = L.layerGroup().addTo(map);

    loadStationsData();
}

// Géolocalisation
function geolocateUser() {
    const btn = document.querySelector('.btn-success-custom');
    const originalText = btn.innerHTML;
    btn.innerHTML = '<span class="loading-spinner me-2"></span>Localisation...';
    btn.disabled = true;

    map.locate({setView: true, maxZoom: 16});

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
    }, 2000);
}

// Afficher toutes les stations
function showAllStations() {
    if (stationsData.length > 0) {
        const group = new L.featureGroup(Object.values(markersLayer._layers));
        map.fitBounds(group.getBounds().pad(0.1));
    }
}

// Chargement des données des stations
async function loadStationsData() {
    try {
        // Récupération de l'URL des stations
        const gbfsResponse = await fetch("https://api.cyclocity.fr/contracts/nancy/gbfs/gbfs.json");
        const gbfsData = await gbfsResponse.json();
        const stationsUrl = gbfsData.data.fr.feeds.find(feed => feed.name === "station_information").url;

        // Récupération des informations des stations
        const stationsResponse = await fetch(stationsUrl);
        const stationsInfo = await stationsResponse.json();

        // Récupération du statut des stations
        const statusResponse = await fetch("https://api.cyclocity.fr/contracts/nancy/gbfs/v2/station_status.json");
        const statusData = await statusResponse.json();

        // Traitement des données
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

    // Mise à jour des statistiques
    updateStats(totalBikes, totalDocks);
}

// Création d'un marqueur pour une station
function createStationMarker(station) {
    const cbText = station.rental_methods && station.rental_methods.includes('creditcard') ? ' (CB)' : '';
    const isOperational = station.is_renting && station.is_returning;

    // Couleur du marqueur selon la disponibilité
    let markerColor = '#6c757d'; // Gris par défaut
    if (isOperational) {
        if (station.bikes_available === 0) {
            markerColor = '#dc3545'; // Rouge - vide
        } else if (station.bikes_available <= 3) {
            markerColor = '#ffc107'; // Jaune - peu de vélos
        } else {
            markerColor = '#198754'; // Vert - disponible
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

    const popupContent = `
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

    marker.bindPopup(popupContent);
    markersLayer.addLayer(marker);
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