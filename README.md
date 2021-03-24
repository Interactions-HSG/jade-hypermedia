# JADE-Hypermedia

[JADE](http://jade.tilab.com/) is a framework and platform for programming multi-agent systems. This
project implements a hypermedia interface for the JADE platform.

## JADE in a nutshell

A JADE application is composed of agents. Agents execute tasks and interact with one another through
messages. Agents live in containers, which can be distributed across the Internet. In every deployment
of the JADE platform, there is one *main container* that all other containers have to register with.
The main container can be replicated for fault tolerance.

![JADE Architecture Overview](hypermedia-weaver-agent/src/main/resources/jade-architecture.png)

Image source: [Tutorial 1: JADE Architecture Overview](https://jade.tilab.com/documentation/tutorials-guides/jade-administration-tutorial/architecture-overview/)

The main container includes two special agents: one *Agent Management System (AMS)*, which is the main
authority in the system, and one *Directory Facilitator (DF)*, which provides a yellow pages for services
provided by agents on the platform. The AMS and DF are defined by the [FIPA Agent Management
Specification](http://fipa.org/specs/fipa00023/SC00023K.html).

See [JADE Tutorials and Guides](https://jade.tilab.com/documentation/tutorials-guides/) for more
information on JADE.

## About this project

This project implements a *Hypermedia Weaver Agent (HWA)*, a special type of JADE agent that helps
construct a hypermedia interface for a distributed JADE platform. In a distributed deployment, there
must be one HWA per machine to manage the hypermedia interface for all containers deployed on that
machine.

## How to run the project

First build the project with:
```shell
gradle shadowJar
```

Then start a main container:
```shell
gradle runMain
```

Open [http://localhost:3000/](http://localhost:3000/) in your browser to navigate a Linked Data view
of the system. The above task will also launch the JADE RMA GUI.

To configure the HTTP endpoints used by the HWAs when exposing the hypermedia interface, see
`local-host` and `http-port` in `hypermedia-weaver-agent/main.properties`. The file contains a number
of other parameters.

To start regular containers on the same machine (run multiple times for multiple containers):
```shell
gradle runLocal
```

To start a peripheral container with an HWA that connects to a main container on a different host:
1. set the `host` and `port` parameters in `hypermedia-weaver-agent/peripheral.properties` to
   point to the host of the main container (the default port used by JADE is 1099)
2. set the `local-host` and `http-port` parameters in `hypermedia-weaver-agent/peripheral.properties`
3. Then run:
```shell
gradle runRemoteHWA
```

To start a remote JADE container without an HWA, set the `host` and `port` parameters (Setp 1. from
above) and then run:
```shell
gradle runRemote
```

You can run the above task multiple times to start multiple containers on the same machine.

## Developing agents with JADE

See [JADE Tutorials and Guides](https://jade.tilab.com/documentation/tutorials-guides/).
