# 📍 WakeMeUp - Réveil Géographique

<div align="center">

![Android](https://img.shields.io/badge/Android-21%2B-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-1.9.10-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![Google Maps](https://img.shields.io/badge/Google%20Maps-API-4285F4?style=for-the-badge&logo=googlemaps&logoColor=white)
![GPS](https://img.shields.io/badge/GPS-Location-FF6B35?style=for-the-badge&logo=location&logoColor=white)

*Ne ratez plus jamais votre arrêt ! Réveillez-vous automatiquement quand vous approchez de votre destination.*

</div>

## 🎯 À propos

**WakeMeUp** est une application Android innovante qui vous permet de créer des alarmes basées sur votre position géographique plutôt que sur l'heure. Parfait pour :

- 🚊 **Transports en commun** : Dormez tranquillement, l'app vous réveille à votre arrêt
- 🚗 **Voyages en voiture** : Idéal pour les passagers lors de longs trajets
- ✈️ **Voyages** : Ne manquez plus vos correspondances ou destinations
- 🏃 **Sport** : Réveil pour vos points de passage lors de courses longues

## ✨ Fonctionnalités

### 🎛️ Gestion des Alarmes
- **Création intuitive** : Interface simple avec carte interactive
- **Personnalisation complète** : Nom, position, rayon d'activation (10m - 1km)
- **Options flexibles** : Son, vibration, activation/désactivation
- **Recherche d'adresse** : Trouvez facilement vos destinations

### 📍 Technologie de Géolocalisation
- **Surveillance continue** : Service en arrière-plan optimisé
- **Précision GPS** : Utilise la géolocalisation fine
- **Économie d'énergie** : Gestion intelligente de la batterie
- **Fonctionnement offline** : Pas besoin d'internet une fois configuré

### 🔔 Système d'Alerte
- **Notifications push** : Alertes immédiates et visibles
- **Son d'alarme** : Sonnerie forte et personnalisable
- **Vibration** : Réveil tactile même en mode silencieux
- **Auto-désactivation** : L'alarme se désactive après déclenchement

## 📱 Captures d'écran

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   📋 Liste      │    │   🗺️ Carte      │    │   ⚙️ Config     │
│   des Alarmes   │    │   Interactive   │    │   Alarme        │
│                 │    │                 │    │                 │
│ • Maison        │    │    📍 Marker    │    │ Nom: Bureau     │
│ • Bureau   ✅   │    │    ⭕ Zone      │    │ Rayon: 200m     │
│ • Gare     ❌   │    │                 │    │ 🔊 Son: ON      │
│ • Aéroport ✅   │    │                 │    │ 📳 Vibr: ON     │
│                 │    │                 │    │                 │
│     [+]         │    │   [Sauver]      │    │   [Sauvegarder] │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

## 🚀 Installation

### Prérequis
- **Android 5.0** (API 21) ou supérieur
- **Services Google Play** installés
- **GPS** activé sur l'appareil

### Étapes d'installation

1. **Cloner le projet**
   ```bash
   git clone https://github.com/votre-username/wakemeup.git
   cd wakemeup
   ```

2. **Ouvrir dans Android Studio**
   - Ouvrir Android Studio
   - File → Open → Sélectionner le dossier du projet

3. **Configuration Google Maps**
   - Obtenir une clé API Google Maps depuis [Google Cloud Console](https://console.cloud.google.com/)
   - Ajouter la clé dans `local.properties` :
     ```properties
     MAPS_API_KEY=votre_cle_api_ici
     ```

4. **Compiler et installer**
   ```bash
   ./gradlew assembleDebug
   ./gradlew installDebug
   ```

## 🔧 Configuration

### Permissions requises

L'application demande automatiquement les permissions suivantes :

```xml
<!-- Géolocalisation -->
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_BACKGROUND_LOCATION" />

<!-- Notifications et réveil -->
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
<uses-permission android:name="android.permission.WAKE_LOCK" />
<uses-permission android:name="android.permission.VIBRATE" />
```

### Optimisation de la batterie

Pour un fonctionnement optimal, désactivez l'optimisation de batterie pour WakeMeUp :

1. **Paramètres** → **Batterie** → **Optimisation de la batterie**
2. Rechercher **WakeMeUp**
3. Sélectionner **"Ne pas optimiser"**

## 📖 Guide d'utilisation

### 1. Créer votre première alarme

1. **Ouvrir l'application** et appuyer sur le bouton **[+]**
2. **Nommer votre alarme** (ex: "Arrêt Châtelet")
3. **Définir la position** :
   - 📍 Utiliser votre position actuelle
   - 🔍 Rechercher une adresse
   - 🗺️ Cliquer directement sur la carte
4. **Configurer le rayon** d'activation (recommandé: 100-200m)
5. **Activer le son et/ou la vibration**
6. **Sauvegarder**

### 2. Utiliser l'alarme

1. **Activer l'alarme** dans la liste principale
2. **Lancer votre trajet** - l'app surveille votre position en arrière-plan
3. **Être alerté** automatiquement quand vous approchez de la destination
4. **Se réveiller** grâce au son et à la vibration

### 3. Gestion avancée

- **✏️ Modifier** : Cliquer sur "Modifier" pour changer les paramètres
- **🔄 Activer/Désactiver** : Utiliser le switch pour activer/désactiver temporairement
- **🗑️ Supprimer** : Cliquer sur "Supprimer" pour effacer définitivement

## 🏗️ Architecture

### Structure du projet

```
app/
├── 📱 MainActivity.kt          # Écran principal
├── ✏️ AlarmEditorActivity.kt   # Création/modification d'alarmes
├── 📍 LocationService.kt       # Service de géolocalisation
├── 💾 AlarmRepository.kt       # Gestion des données
├── 📋 AlarmAdapter.kt          # Affichage de la liste
├── 🏠 LocationAlarm.kt         # Modèle de données
└── 🎛️ MainViewModel.kt        # Logique métier
```

### Technologies utilisées

- **Language** : Kotlin
- **Architecture** : MVVM (Model-View-ViewModel)
- **Géolocalisation** : Google Play Services Location
- **Cartes** : Google Maps Android API
- **Stockage** : SharedPreferences + Gson
- **Interface** : Material Design Components
- **Services** : Foreground Service pour le background

### Composants clés

| Composant | Rôle | Technologie |
|-----------|------|-------------|
| `LocationService` | Surveillance GPS en arrière-plan | FusedLocationProviderClient |
| `AlarmRepository` | Persistance des données | SharedPreferences + Gson |
| `MainActivity` | Interface principale | RecyclerView + LiveData |
| `AlarmEditorActivity` | Configuration des alarmes | Google Maps Fragment |

## 🔒 Confidentialité

- **Données locales** : Toutes les données sont stockées localement sur votre appareil
- **Pas de serveur** : Aucune donnée n'est envoyée vers des serveurs externes
- **GPS uniquement** : Seule la position GPS est utilisée, pas d'autres données personnelles
- **Transparent** : Code source ouvert et auditable

## 🐛 Résolution de problèmes

### L'alarme ne se déclenche pas

- ✅ Vérifier que l'alarme est **activée** (switch vert)
- ✅ Confirmer que les **permissions de géolocalisation** sont accordées
- ✅ Désactiver l'**optimisation de batterie** pour l'app
- ✅ Vérifier que le **GPS est activé** sur l'appareil
- ✅ S'assurer d'être dans la **zone définie** (rayon configuré)

### L'app se ferme en arrière-plan

- ✅ Désactiver l'**optimisation de batterie** pour WakeMeUp
- ✅ Ajouter l'app aux **applications protégées** (selon le fabricant)
- ✅ Vérifier dans **Paramètres** → **Applications** → **Autorisation spéciale**

### GPS imprécis

- ✅ Activer la **géolocalisation haute précision**
- ✅ Être dans un **environnement ouvert** (éviter les tunnels/bâtiments)
- ✅ Augmenter le **rayon d'activation** si nécessaire

## 🤝 Contribution

Les contributions sont les bienvenues ! Pour contribuer :

1. **Fork** le projet
2. Créer une **branche feature** (`git checkout -b feature/nouvelle-fonctionnalite`)
3. **Commit** vos changements (`git commit -m 'Ajout nouvelle fonctionnalité'`)
4. **Push** vers la branche (`git push origin feature/nouvelle-fonctionnalite`)
5. Ouvrir une **Pull Request**

### Idées d'améliorations

- 🌙 **Mode nuit** pour l'interface
- 📊 **Statistiques** d'utilisation des alarmes
- 🎵 **Sons personnalisés** pour les alarmes
- 🌐 **Support multi-langues**
- ⏰ **Alarmes combinées** (heure + position)
- 📱 **Widget** pour l'écran d'accueil

## 📄 Licence

Ce projet est sous licence MIT. Voir le fichier [LICENSE](LICENSE) pour plus de détails.

## 👨‍💻 Développeur

Développé avec ❤️ pour rendre les voyages plus sereins.

---

<div align="center">

**⭐ N'hésitez pas à donner une étoile si ce projet vous aide ! ⭐**

*WakeMeUp - Parce que votre destination compte plus que l'heure*

</div>
