# Scénarios de Test du Système Pub/Sub

Ce document décrit les différents scénarios de test disponibles pour le système de publication/souscription (Pub/Sub). Chaque scénario est conçu pour tester des fonctionnalités spécifiques, des interactions complexes ou des contraintes de sécurité.

## Sommaire

1. [SimpleScenario](#simplescenario) - Test de base : une éolienne et une station.
2. [FullOperationScenario](#fulloperationscenario) - Opérations complètes : enregistrement, création de canal, abonnement avec filtres et publications.
3. [ComplexInteractionScenario](#complexinteractionscenario) - Interactions riches : multiples composants, filtres complexes et changements dynamiques.
4. [SecurityTestScenario](#securitytestscenario) - Tests de sécurité : vérification des accès non autorisés et des composants non enregistrés.
5. [LargeScaleScenario](#largescalescenario) - Test de montée en charge : grand nombre de composants (5 stations, 5 éoliennes, 2 bureaux).
6. [ChannelManagementScenario](#channelmanagementscenario) - Gestion des canaux : création, modification de liste blanche et destruction.
7. [FilterModificationScenario](#filtermodificationscenario) - Filtres dynamiques : modification d'un filtre d'abonnement en cours d'exécution.
8. [MultipleSubscriptionScenario](#multiplesubscriptionscenario) - Abonnements multiples : un composant abonné à plusieurs canaux simultanément.
9. [UnregisterRedeployScenario](#unregisterredeployscenario) - Cycle de vie : désinscription puis réinscription d'un composant.
10. [ServiceClassUpgradeScenario](#serviceclassupgradescenario) - Montée en gamme : passage de la classe FREE à PREMIUM pour accéder aux fonctions privilégiées.

---

## Détails des Scénarios

### SimpleScenario
Test minimal pour vérifier la connectivité de base.
- **Composants** : 1 WindTurbine (`windTurbine1`), 1 Station (`station1`).
- **Trace prévue** :
    - `windTurbine1` : "WindTurbine : Registered", "Subscribed to wind_channel", "Message received on 'wind_channel' | payload=WindData[...]".
    - `station1` : "Station1 : Registered", "Station1 : Publish a message on wind_channel - WindData[...]".

### FullOperationScenario
Teste l'interaction entre un Bureau (PREMIUM) et des éoliennes/stations (FREE).
- **Composants** : 1 Bureau (`bureau1`), 1 WindTurbine (`windTurbine1`), 2 Stations (`station1`, `station2`).
- **Trace prévue** :
    - `bureau1` : "Registered as PREMIUM", "Created weather_alert_channel", "Subscribed to wind_channel", "Message received on 'wind_channel' | payload=WindData[...]".
    - `windTurbine1` : "Registered", "Subscribed to wind_channel", "Subscribed to weather_alerts_channel with filter [...]", "Message received on 'wind_channel' | payload=WindData[...]".
    - `station1` : "Registered", "Publish a message on wind_channel - WindData[...]".
    - `station2` : "Registered".

### ComplexInteractionScenario
Scénario dynamique avec des alertes météo et des changements d'abonnements.
- **Composants** : 1 Bureau (`bureau1`), 2 WindTurbines (`turbine1`, `turbine2`), 3 Stations (`station1`, `station2`, `station3`).
- **Trace prévue** :
    - `bureau1` : Création de `weather_alerts_channel`, réception des données vent, publication d'alertes STORM/ICY_STORM.
    - `turbine1` : Abonnement aux alertes RED/SCARLET, puis désabonnement, puis réabonnement à tout.
    - `turbine2` : Abonnement constant aux données de vent.
    - `stationX` : Publications successives de données météo.

### SecurityTestScenario
Vérifie que les contraintes du système sont respectées.
- **Trace prévue** :
    - `security-bureau` (FREE) : Tente de créer un canal -> "Successfully caught expected UnauthorisedClientException".
    - `security-station` (Non enregistré) : Tente de publier -> "Successfully caught expected UnknownClientException".
    - `security-turbine` (Non enregistré) : Tente de s'abonner -> "Successfully caught expected UnknownClientException".

### LargeScaleScenario
Vérifie le comportement du Broker face à de nombreux clients.
- **Composants** : 5 Stations, 5 WindTurbines, 2 Bureaux. (nombre modulable)
- **Trace prévue** : Enregistrements séquentiels de tous les composants, créations de canaux par les bureaux, publications en rafale par les stations et réception par les turbines abonnées.

### ChannelManagementScenario
Cycle de vie des canaux privés.
- **Trace prévue** :
    - `mgmt-bureau` : "Registered as PREMIUM", "Created channel 'private_channel' with whitelist 'allowed_station'", "Modified authorized users [...] to '.*'", "Destroyed channel 'private_channel'".

### FilterModificationScenario
Vérifie que le changement de filtre est immédiat.
- **Trace prévue** :
    - `filter-turbine` : S'abonne avec force > 10.0. Reçoit un message à 15.0. Modifie le filtre à force > 20.0. Ne reçoit plus le message à 15.0, mais reçoit celui à 25.0.

### MultipleSubscriptionScenario
Réception entrelacée de plusieurs canaux avec filtrage complexe.
- **Composants** : 2 Bureaux (`multi-bureau-1`, `multi-bureau-2`), 3 Stations (`multi-station-1`, `multi-station-2`, `multi-station-3`), 1 WindTurbine (`multi-turbine`).
- **Trace prévue** :
    - `multi-turbine` : S'abonne à `WIND_CHANNEL` (tout), `alert_channel` (Level >= RED), et `private_channel` (force > 50.0).
    - Reçoit :
        1. Données vent de S1 sur `WIND_CHANNEL`.
        2. Alerte STORM (Level.RED) de B1 sur `alert_channel`.
        3. Données vent (force 80.0) de S3 sur `private_channel`.
        4. Données vent de S2 sur `WIND_CHANNEL`.
        5. Alerte EVACUATE (Level.SCARLET) de B1 sur `alert_channel`.
    - Ne reçoit PAS : l'alerte GREEN de B1, ni les données de force 20.0 ou 10.0 sur `private_channel`.

### UnregisterRedeployScenario
Teste la persistance (ou non) après désinscription.
- **Trace prévue** :
    - `unreg-turbine` : Reçoit "FirstMsg". Se désinscrit. Ne reçoit pas "SecondMsg". Se réinscrit. Reçoit "ThirdMsg".

### ServiceClassUpgradeScenario
Evolution des privilèges d'un composant.
- **Trace prévue** :
    - `upgrade-bureau` : S'enregistre en FREE. Échec de création de canal ("Caught expected exception"). Passe en PREMIUM. Succès de création de canal ("Successfully created channel 'premium_channel'").
