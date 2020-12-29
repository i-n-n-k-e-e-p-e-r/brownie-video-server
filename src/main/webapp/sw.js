var staticCacheName = 'static';
var version = 1;


function updateCache() {
    return caches.open(staticCacheName + version)
        .then(function (cache) {
            return cache.addAll([
                ''
            ]);
        });
}

self.addEventListener('install', function (event) {
    event.waitUntil(updateCache());
});