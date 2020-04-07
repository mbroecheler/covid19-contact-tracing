# Database

We are using DataStax Enterprise as the data store for contact tracing because of it's scalable graph database features.
It's a distributed database that can scale to meet the need of nationwide contact tracing and is highly available to
provide resilience. Plus, it is free to use for development and for production use cases of contact tracing by authorities and officials.

Using a graph database provides a lot of features out of the box that make contact tracing easier to implement.
This directory contains the database schema as well as a notebook file to design contact tracing queries.

Those queries can then be easily implemented in the backend application.

Please refer to the README file in the root of the repository to learn how to setup the database and install Studio so you can access the notebook.