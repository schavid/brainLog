# BrainLog Watchlist App

## Kurzbeschreibung

BrainLog ist eine persönliche Watchlist-Anwendung für Android. Sie ermöglicht es Benutzern, Filme, TV-Serien und Videospiele zu entdecken, zu einer persönlichen Liste hinzuzufügen und den Status (z.B. "gesehen") zu verwalten.

## Wichtigste Features

* **Benutzer-Authentifizierung:** Sicherer Login über Google Sign-In, um die Watchlist mit einem Firebase-Account zu verknüpfen.

* **Medien entdecken:** Suche nach Filmen und Serien (über die TMDB-API) sowie nach Videospielen (über die RAWG-API).

* **Persönliche Watchlist:** Füge gefundene Medien zu deiner persönlichen Liste hinzu oder entferne sie.

* **Status-Tracking:** Markiere Medien als "gesehen/gespielt" oder mache dies wieder rückgängig.

* **Filter-Funktion:** Filtere deine Watchlist nach den Zuständen "Gesehen", "Ungesehen" oder zeige alle Einträge an.

* **Auswahlmodus:** Durch langes Drücken auf ein Element wird ein Auswahlmodus aktiviert, um mehrere Medien gleichzeitig zu löschen oder deren Status zu ändern.

## Setup

Um das Projekt lokal zu kompilieren und auszuführen, sind folgende Schritte notwendig:


1. **API-Schlüssel:**

    * Du benötigst einen API-Schlüssel von [The Movie Database (TMDB)](https://www.themoviedb.org/documentation/api) und [RAWG Video Games Database](https://rawg.io/apidocs).

    * **Wichtiger Sicherheitshinweis:** Um die Schlüssel nicht im Code preiszugeben, müssen diese in der `local.properties`-Datei im Projekt-Root hinterlegt werden. Fehlen diese Einträge, wird die App abstürzen, da keine API-Anfragen möglich sind.

      *Beispiel für `local.properties`:*
      ```
      TMDB_API_KEY="DEIN_TMDB_SCHLÜSSEL_HIER"
      RAWG_GAME_API="DEIN_RAWG_SCHLÜSSEL_HIER"
      ```

2. **SHA-1 Fingerabdruck:**

    * Damit Google Sign-In funktioniert, musst du die **SHA-1-Fingerabdrücke** deiner Entwicklungs- (`debug.keystore`).

## Verwendete Technologien

* **Sprache:** Kotlin

* **UI-Toolkit:** Jetpack Compose

* **Architektur:** MVVM (Model-View-ViewModel)

* **Backend:** Firebase (Authentication & Firestore)

* **Netzwerk:** Retrofit für API-Anfragen

* **Asynchronität:** Kotlin Coroutines & StateFlow
