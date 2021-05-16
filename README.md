# ChaosConnect [![ChaosConnect Status](https://github.com/PascalHonegger/ChaosConnect/actions/workflows/chaos-connect.yml/badge.svg)](https://github.com/PascalHonegger/ChaosConnect/actions/workflows/chaos-connect.yml)
The modern, distributed and scalable implementation of a game inspired by Connect Four.

# Gameplay

## Rules
- There are two main fractions: Yellow vs Red
- There's only one big playing field for all players
- Each player can make a move after a certain timeout
- Other players cannot place a chip close to another chip for a certain timeout
- If more people join, the timeouts are altered, and the playing field scales automatically
  - More people => make playing field wider
  - More people => make playing field slightly higher
  - Fewer people => Mark (now superfluous) rows and columns as soon to be deleted, delete after a certain time passed
- If a disk you placed is part of a 4 disk long line you get points, and the associated columns are cleared

## Future Plans
- You get more points if you're on the losing fraction for balance purposes
- You can exchange your points for perks and skins
- To ensure fairness, some bots might be added to balance the teams

# Architecture

## Big Picture
![Architecture Overview](https://www.plantuml.com/plantuml/png/jPTFRvim5C3l_XHUrMlI0ELFfIegtKuxT5FNsxQ31oxnOcngJDgkwdUVyy2a9K6g5FCEMFRxy-Cz7WPVrPeetPGSVM8YuqrEyIMNXQpFSfcjgPfNHhVSK_wjfHXHhQNcR4nPoLeNYjOFVCIWtb2kwOnbVNnKLuffYa-fsCZdIicdP_pJM_XFGN3cHR_n2rgCYvya2qSoZeaJa6anII_-P1ZV8YAuJeE9EqROPnMvnzX4l7UPUKunbXBlP-SU4nxCyDmnve3LEO0SOw9ufC7TOBma0anug4AX6mp4YG439NAFiUraCMs91fKxTu81IZj2qBrj9d25iUFXP-vFW05vp_6EnwGN43wrc8Flq94OFqqj9assepS59jrAs67IAJ5z40ym_ZIOPD02IJG9vcA_aRsfCNO_60OE4gUODBAVbo7Q4GQU2NIR_RoOqRwObd8inkvYlcqy8x5TjvFZ6tgtYq6yBeN0LCkMy3XCQ3ZHOXSwR5E03umLpyKFLBfBA8A4ukv1xQhb0jFkQcQyC14JArrwWSj_wDIAOGv_CzrXBAXzqA4FWtiC14yN4mfwHKWppIc-ezbAI2wBP_njGzM6qKU4wZKJ5L6anAKKCbifMLAiLKFVQ79wJngkx-YJJZahH4c5nfcvK8LGHR8rAIW-kJnjZvRB4_o2C5OqqqGK73HlZd_BN-ABh2ecJp2fyRsI9ep8ZSD3GnimtXq8ZUsFYjByaHHIW3qi6-EU-bNIxL4Nb7L6E1F5jT6Pa70NW_je3x4cRESs-zSebgt0MyYSUXzmCJLXTU_XVy23Iv5BqRi4nkKLZJdToXK1MwYm8XpIWGhTizPsUqfVWad-nomBgStoDSlPDYAJWd2SoR9i1Cl8TexrIiccME7YzljdyBlnJ5lOskFPigihNCrqK4a4fS6NCwCPt1IKO5GJ7DIycmBTf85kF3nlvhk6QEU3Eu5LDHz6l3ANfkJ_0G00)

## Separation of Concerns
The service names are all a reference to the popular anime `JoJo's Bizarre Adventure`.

| Name       | Role            | Description                                                                   |
| ---------- | --------------- | ----------------------------------------------------------------------------- |
| Doppio     | Frontend        | Svelte-based web client                                                       |
| Speedwagon | Load balancer   | Envoy-based load balancing reverse proxy                                      |
| Joestar    | Scaling backend | Micronaut server for user authentication and caching                          |
| Rohan      | Central backend | Micronaut server for central storage and processing of the game and its users |

### Frontend
The frontend is written in [Svelte](https://svelte.dev/).
As it's main purpose is to display the current state of the board, we decided that frameworks such as Angular are overkill.

### Load balancing
We use [Envoy](https://www.envoyproxy.io/) as a reverse proxy to handle load balancing.
We had to use Envoy as it's the only reverse proxy which currently supports grpc-web.

### Backend
The backend is split into two parts:
* Scaling: Communicates directly with our Frontend, issues and validates JWT, caches game state and sends game updates to all clients
* Central: Manages the actual game state, synchronizes requests and handles game logic, stores persistent information, such as user credentials and scores, in a json document

Both backends use [Micronaut](https://micronaut.io/) with [Kotlin](https://kotlinlang.org/).

### Storage
We decided not to use a database but instead store the (very minimalistic) data in a JSON document.

## Communication

### Bi-directionality

Bidirectional communication is enabled through [gRPC](https://grpc.io/).
For example, this allows `Rohan` to send a game update event to all `Joestar` instances, which then forward them realtime to all `Doppio` clients.

### Authentication

We use a custom solution based on symmetric JWT for authentication, as we did not want to commit to the standard solution provided by the Micronaut framework (see [Symmetric JWT](#symmetric-jwt)).

### Control flow

We use streaming to propagate game state updates from Rohan to Joestar servers and from Joestar servers to Doppio clients.
All other communication is request-based.

### Asynchronicity

The streaming API offers hooks for various events.
The request-based API is asynchronous by nature as well, but allows for convenient programming styles that are similar to those common in synchronous contexts.

| Control Flow           | Kotlin                                                      | TypeScript                                |
| ---------------------- | ----------------------------------------------------------- | ----------------------------------------- |
| Streams                | `kotlinx.coroutines.flow.Flow` + callback methods           | `ClientReadableStream` + callback methods |
| 'Synchronous' Requests | `suspend fun` + `kotlinx.coroutines.BuildersKt.runBlocking` | `Promise` + `async` + `await`             |

### JWT

JWTs are issued and terminated by Joestar servers.

## Configuration

We use [Micronaut Application Configuration](https://docs.micronaut.io/latest/guide/index.html#config) to make some parts of the application configurable.
These configurations can be set through the `application.yml` file at compile time or through an environment variable at runtime.
The following configurations are probably the most interesting ones to configure, for a full list see the source code:

- Joestar
  - `JOESTAR_PORT`: The port at which Rohan is listening
  - `ROHAN_SERVER`: The hostname or ip of the Rohan server
  - `ROHAN_PORT`: The port at which Rohan is listening
  - `JWT_SECRET`: The base-64 encoded 512-bit private key used for signing the JWT
- Rohan
  - `ROHAN_PORT`: The port at which Rohan is listening

## Maintainability
TODO:
- Testing: 200+ unit tests
- More?

# Infrastructure

## Docker setup

In order to enable easy deployment every service is dockerized.
We do not use docker containers for developing, but you can easily build the containers locally to test their cross-container communication.
We also use docker to run gRPC code generation for `grpc-web` to ensure the code gets generated with the same compiler version on every device.

The docker images are generally optimized for file size and try to use the smallest available base image.

Our images also implement [Docker Healthchecks](https://docs.docker.com/engine/reference/builder/#healthcheck) which can be used to determine if a server is irrecoverably broken.

## Reverse Proxy
TODO:
- Responsibilities (overlap with [Separation of concerns](#separation-of-concerns)?)
- Load Balancing strategy
- More?

## CI/CD
We use [GitHub Actions](https://github.com/features/actions) to test and build ChaosConnect automatically.
Every push is tested and built and Pull Requests can only be merged if all tests succeed.
Creating a new [Release](https://github.com/PascalHonegger/ChaosConnect/releases) automatically builds and publishes all required [Packages](https://github.com/PascalHonegger?tab=packages&repo_name=ChaosConnect) such that users can run ChaosConnect with a specific version without needing to build the images themselves.

## Hosting

At the time of writing this, the latest stable version of ChaosConnect is deployed at [chaos.honegger.dev](https://chaos.honegger.dev/).
We decided to use the cloud provider [Linode](https://www.linode.com/) because they provide fair pricing and a good free initial credit.

Setting up hosting was very easy, you just copy the docker-compose.yml file, replace the placeholder jwt-secret with a generated one and you're almost ready to start.
If you don't already have a valid certificate around, you can easily generate one using [Certbot](https://certbot.eff.org/) using a command similar to the following and mounting them to their corresponding location within the Speedwagon container:

`sudo docker run -it --rm --name certbot -v "/etc/letsencrypt:/etc/letsencrypt" -v "/var/lib/letsencrypt:/var/lib/letsencrypt" -p 80:80 certbot/certbot certonly --standalone`

# Design Decisions

## gRPC
We wanted real time updates and gRPC provides a way to stream real time updates.
Implementing real time update from server-to-server and server-to-browser is no easy task and implementing a type safe variant through websockets would have taken a lot longer than just using a gRPC library.

## Micronaut

Micronaut offers a couple of benefits:

- It supports gRPC out-of-the-box
- It offers Configuration Management
- It provides fast startup and performance by using build-time dependency injection

## Kotlin
TODO: Rationale
- Conciseness
- `null` safety
- More?

## Svelte
TODO: Rationale
- Light-weight framework (suitable for small projects)
- More?

## Envoy
TODO: Rationale
- Compatibility (gRPC, JWT)?
- More?

## Symmetric JWT

We couldn't easily use the `micronaut-security` package because the feature is still WIP for gRPC (see [`micronaut-grpc` issue #164](https://github.com/micronaut-projects/micronaut-grpc/issues/164)).
The official token-based authentication works by using Google as a token provider, but we didn't want to have a vendor lock-in.

In the end, we decided to use simple symmetric tokens because of the project scope.

The client does use the metadata to send the token [which was recommended back in 2018 by the `grpc-web` team](https://github.com/grpc/grpc-web/issues/207#issuecomment-406134504).

## Notifications
TODO: Rationale
- Why real time updates with change events instead of whole state?
- More?

# Operation
The easiest way to get ChaosConnect running is using docker-compose. We do not provider support for running the software components otherwise.

## Prerequisites
The `docker-compose.yml` file contains a placeholder for the JWT signing key.
This base64-encoded 512 bit secret is crucial for verifying user authenticity and must be configured before starting.

Another crucial point are HTTPS certificates, which have to be mounted to `/etc/envoy/certs/cc.key` and `/etc/envoy/certs/cc.cert` within the container.
If you're running on localhost, see the development guide below on how to generate self-signed certificates.

## Starting

```sh
# Run services under https://localhost/ using images published to GitHub Packages
# Valid certificates are expected to be placed under ./certs/cc.key and ./certs/cc.cert
# Valid environment variables have to be configured 
docker-compose up --scale joestar=5 -d
```

# Development
In order to run components natively in development mode, the following commands are good to get started:

```sh
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

Or if you want to test the release configuration locally, you can build and run them locally:

```sh
# Generate self-signed certs, puts them in the ./proxy/certs directory
# Ensure the permissions are set such that the envoy docker user can read the certificate
docker-compose -f docker-compose.gen.yml up gen_self_signed_cert

# Run services hosted under http://localhost:5001, built locally, run 2 joestar instances
docker-compose -f docker-compose.dev.yml up --build --scale joestar=2
```
