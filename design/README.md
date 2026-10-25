# Diseño de Air Play JD

- `brand/`: logos, icono de app (capas adaptativas) y banners de Android TV. Único color de marca: `#fc5858`.
- `mocks/`: las 11 pantallas de TV a 960×540 (fuente `.dc.html`), con los tokens (`ds/airplayjd/tokens.json`, `tokens.css`) y los estilos de componentes (`components/bundle.css`) del design system.

Versión interactiva de los mocks: https://claude.ai/artifact/7VoeriGH98DemQXVziTgSG
Design system: https://claude.ai/artifact/6nancAmD3HzieqHsU26a91

| # | Pantalla | Fichero |
|---|---|---|
| 1 | Espera | `mocks/Main.dc.html` |
| 2 | Solicitud de conexión | `mocks/Request.dc.html` |
| 3 | Código de emparejamiento | `mocks/Pin.dc.html` |
| 4 | Duplicado de pantalla | `mocks/Mirroring.dc.html` |
| 5 | Vídeo con controles | `mocks/Video.dc.html` |
| 6 | Audio con metadatos | `mocks/Audio.dc.html` |
| 7 | Ajustes | `mocks/Settings.dc.html` |
| 8 | Error: sin red | `mocks/ErrorNetwork.dc.html` |
| 9 | Error: conexión perdida | `mocks/ErrorLost.dc.html` |
| 10 | Salvapantallas (tras 2 min en espera) | `mocks/Screensaver.dc.html` |
| 11 | Dispositivos permitidos | `mocks/Devices.dc.html` |

Las imágenes `/_blob/...` de los mocks son el logo y el monograma de `brand/Logos/`.
