const CONFIG = {
    // API Configuration
    BAN_API_URL: 'https://api-adresse.data.gouv.fr/search/',
    CYCLOCITY_BASE_URL: 'https://api.cyclocity.fr/contracts/nancy/gbfs/',
    CYCLOCITY_GBFS_ENDPOINT: 'gbfs.json',
    CYCLOCITY_STATUS_ENDPOINT: 'v2/station_status.json',
    // INCIDENTS_API_URL: 'https://www.datagrandest.fr/data4citizen/sites/default/files/dataset/2025/03/04/9a7472ee-a0f9-47f9-b888-769085cb52dd/cifs_waze_v2.json',
    INCIDENTS_API_URL: 'https://localhost:8080/data',

    // Map Configuration
    DEFAULT_CITY: 'Nancy',
    DEFAULT_ZOOM: 14,
    MAX_ZOOM: 19,
    LOCATION_MAX_ZOOM: 16,
    RESTAURANT_ZOOM: 16,

    DEFAULT_LAT: 48.6937223,
    DEFAULT_LON: 6.1834097,

    // Tile Configuration
    TILE_URL: 'https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png',
    TILE_ATTRIBUTION: '&copy; <a href="http://www.openstreetmap.org/copyright">OpenStreetMap</a>',

    // Data Files
    RESTAURANTS_API: 'https://localhost:8080/database/restaurants/',

    // Timing Configuration
    RELOAD_DELAY: 2000,

    // Helper methods
    get(key) {
        return this[key];
    },

    getInt(key) {
        return parseInt(this[key]);
    },

    getNumber(key) {
        return parseFloat(this[key]);
    }
};

// Make CONFIG available globally
window.CONFIG = CONFIG;
