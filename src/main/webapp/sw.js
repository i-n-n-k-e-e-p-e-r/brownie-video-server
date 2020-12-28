importScripts('./VAADIN/static/server/workbox/workbox-sw.js');

workbox.setConfig({
  modulePathPrefix: './VAADIN/static/server/workbox/'
});
workbox.precaching.precacheAndRoute([
    { url: 'icons/brownie-144x144.png', revision: '-1619375005' },
    { url: 'icons/brownie-192x192.png', revision: '429260614' },
    { url: 'icons/brownie-512x512.png', revision: '-381362175' },
    { url: 'icons/brownie-16x16.png', revision: '1640292953' },
    { url: 'offline.html', revision: '-2067995194' },
    { url: 'manifest.webmanifest', revision: '-37768073' },
    { url: 'images/offline-login-banner.jpg', revision: '1610653180' }
]);
self.addEventListener('fetch', function(event) {
  var request = event.request;
  if (request.mode === 'navigate') {
    event.respondWith(
      fetch(request)
        .catch(function() {
            return caches.match('offline.html');
        })
    );
  }
 });