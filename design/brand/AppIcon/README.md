# AppIcon
Recursos que piden Android TV y Google Play: la pantalla de vidrio con JD sobre el fondo ambiental en el rojo de marca.

- `tv-banner-320x180.png` / `.svg`: banner obligatorio de Android TV (`android:banner` en el manifest, `res/drawable-xhdpi`). Aparece en la fila de apps de Google TV.
- `tv-banner-1280x720.png` / `.svg`: banner de TV para la ficha de Google Play.
- `app-icon-512.png` / `.svg`: icono de la ficha de Google Play (512×512, cuadrado completo; Play redondea las esquinas).
- `adaptive-background.svg`, `adaptive-foreground.svg`, `adaptive-monochrome.svg`: capas del icono adaptativo (108dp, el símbolo dentro de la zona segura de 66dp). El fondo usa desenfoque SVG: expórtalo a PNG (432×432) en vez de a Vector Drawable, que no admite filtros.
