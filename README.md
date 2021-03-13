# ChaosConnect
The modern, distributed and scalable implementation of a game inspired by Connect Four.

# Gameplay
- There are two main fractions: Yellow vs Red
- There's only one big playing field for all players
- Each player can make a move after a certain timeout
- Other players cannot place a chip close to another chip for a certain timeout
- If more people join, the timeouts are altered and the playing field scales automatically
  - More people => make playing field wider
  - More people => make playing field slightly higher
  - Less people => Mark (now superfluous) rows and columns as as soon to be deleted, delete after a certain time passed
- If a disk you placed is part of a 4 disk long line you get points and the associated columns are cleared
- You get more points if you're on the losing fraction for balance purposes
- You can exchange your points for perks and skins
- To ensure fairness, some bots might be added to balance the teams

# Running for Production
In order to run all services within docker you can run an alternation of one of the following commands:

```sh
# Run services hosted under http://localhost:5001, built locally, run 2 joestar instances
docker-compose -f docker-compose.dev.yml up --build --scale joestar=2

# Generate self-signed certs, files ./certs/cc.key and ./certs/cc.cert are required to run HTTPS
docker-compose -f docker-compose.gen.yml up gen_self_signed_cert

# Run services under https://localhost/, using certificates from the certs directory, using images published to GitHub Packages
# Valid certificates are expected to be placed under ./certs/cc.key and ./certs/cc.cert
docker-compose -f docker-compose.prod.yml up --scale joestar=5 -d
```

# Development
In order to run components in development mode, the following commands are good to get started:

```sh
# Generate self-signed certs, puts them in the ./certs directory
# Ensure the permissions are set such that the envoy docker user can read the certificate
docker-compose -f docker-compose.gen.yml up gen_self_signed_cert

# Required once at the beginning and afterwards once the protocol buffer contract changes
docker-compose -f docker-compose.gen.yml up gen_grpc_joestar_client

# Run proxy (proxies http://localhost:5001 => http://localhost:5000 and http://localhost:5001/api => http://localhost:8080/)

# (Docker for Windows / Docker for Mac)
docker-compose -f docker-compose.proxy.yml up -d
# (Docker on Linux)
docker-compose -f docker-compose.proxy-linux.yml up -d

# Svelte Frontend
cd frontend
npm run dev

# backend
cd backend
# run joestar
./gradlew joestar:run
# run rohan
./gradlew rohan:run
```

# Technical Implementation

![Architecture Overview](https://www.plantuml.com/plantuml/png/VP91RzGm48Nl_XN3YkikMdfTHQLmwg6WqYCNuymw9ex7jSVRLgZ_dOcpZKeWlLWQlyylpy-vpAmJby6hTouONrg4WoTB-KDBfiUqYw8r_uYMOhSg_ieKLgIUsBirCL9ccp3V-nKWdz0pRfsP_HKxzWXtQBf00Zt1rnEcayC7fNBlGjH93p1G8DCb6X0u5Nobj7ZKnVCTFl8dxsmOC30OMJ0f5RNfjKNO7DvFPJGR-Aq04XhMmVgglChK_0XVA4P76z0PZed49hHouBvWgV1Kct3V8sBxe2s5oiP4Zqy2pb-y9XmV9bSr6osbsIiHnQz6M8IOQXNVQmgQEpsv1cfn_oQSCNOp-l5Db7MY6RqGz5cjmRVqac1iCcb_JYu7ZcvYnr-agKZxKxO3iMnVkTQDZew2zc1e64fmHeypS9Ues0xixRVFzTpDNZshbuvXsvmxhAkCYz9KxG8EMr4MeUhLLvMB_of_Zo20N4FTx65NTlF3d-Tbad4txPQEbxAKmVy1)

## Service Names
The service names are all a reference to the popular anime `JoJo's Bizarre Adventure`.

| Service | Name |
| ------- | ---- |
| Frontend | Doppio |
| Loadbalancer | Speedwagon |
| Scaling Backend | Joestar |
| Central Backend | Rohan |

## Frontend
The frontend is written in [Svelte](https://svelte.dev/). As it's main purpose is to display the current state of the board, we decided that frameworks such as Angular are overkill.

## Reverse Proxy
We use [Envoy](https://www.envoyproxy.io/) as a reverse proxy to handle load balancing. We had to use Envoy as it's the only reverse proxy which currently supports grpc-web.

## Backend
The backend is split into two parts:
* Scaling: Communicates directly with our Frontend, issues and validates JWT, caches game state and sends game updates to all clients
* Central: Manages the actual game state, synchronizes requests and handels game logic, stores persistent information, such as user credentials and scores, in a json document

Both backends use [Micronaut](https://micronaut.io/) with [Kotlin](https://kotlinlang.org/).

## Database
We decided not to use any database but instead store the (very minimalistic) data in a json document.

## Communication
Bidirectional communication is enabled through [gRPC](https://grpc.io/). For example, this allows `Rohan` to send a game update event to all `Joestar` instances, which then forward them realtime to all `Doppio` clients.
