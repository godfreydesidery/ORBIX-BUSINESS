# Orbix Manager

The Android app for managers on the Orbix Business API. It shows the branch dashboard, LPO, GRN and discount approvals, and reports.

The app uses **only the existing API**. Nothing in `Orbix-Business-API` or the web app changes for it.

## Run it

```
flutter pub get
flutter run --flavor sighman --dart-define-from-file=brands/sighman/generated/dart_defines.json --dart-define=ORBIX_HOST=http://13.51.26.114:8080
```

`flutter run` on its own starts the generic Orbix build, which asks for a server address on first launch.

## Brands

There is one codebase with one Android flavor per client. Each `brands/<id>/brand.json` holds the client's app name, colour, icon letter and default server.

```
python tool/brand.py list
python tool/brand.py new acme --name "Acme Manager" --company "Acme Ltd" --mark A --color "#7C2D12" --server http://1.2.3.4:8080
```

`tool/brand.py` generates the palette, launcher icons and Android resources into `brands/<id>/generated/`. Commit that folder. You need Pillow to generate icons.

## Build an APK

Double-click `dist\build.cmd`, or run:

```
.\dist\build.ps1 -Brand sighman
```

The script runs the tests, builds a release APK, and writes it to `dist\apk\` with a `.json` record beside it (brand, server, commit, SHA-256).

Release builds are still signed with the debug key. Set up a real keystore before distributing.

## Accounts and sessions

- **One account per branch.** The API takes the branch from the signed-in user, so a manager of several branches signs in once per branch. The app keeps each login as an account, with a switcher on the dashboard and the Account tab.
- **The server address is hidden.** Each brand has its server built in. Support can change it by tapping the sign-in footer seven times. Each saved account remembers its own server.
- **No session refresh.** The server's `/token/refresh` issues a token its own authorization filter rejects. When the 8-hour access token runs out, the app asks for that account's password again.
- **Tokens are stored in the Android Keystore** (`flutter_secure_storage`). Passwords are never stored.
