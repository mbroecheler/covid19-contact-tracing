# COVID19 Contact Tracing

This project demonstrates an open-source backend implementation of automated, scalable contact tracing to identify potentially infected individuals for testing and quarantine.
Assuming that Singapore will soon open-source their contact detection application, this project could serve as the backend for capturing the contact information and computing
likely infection paths for containment. This project contains a scalable database implementation for capturing and analyzing the contact data, a web application to access the data,
a simulator for generating people interaction data for testing purposes, and a notebook to analyze the resulting contact graph.

![Device Contact Graph](/docs/images/device_graph.png?raw=true)

## Why do we need this?

Automated, scalable contact tracing is needed if we want to effectively contain the spread of the pandemic without shutting down the economy and devasting people's lives.
However, collecting such intimate information is an invasion of privacy that needs to be restricted and made transparent as much as possible. The goal of this project is to develop
a scalable, automated, effective, and transparent database and backend implementation of contact tracing as an open-source project for authorities to quickly adapt and implement.

We believe that implementing this as an open-source project is important in order to achieve:

- **Effective Implementation:** Doing contact tracing effectively in an automated fashion at scale is non-trivial. Implementing this as an open-source project allows a community of
experts to come together and implement a working solution based on industry best practices and decades of experience.
- **Fast Implementation:** We need to get a working system up and running quickly. Our hope is that we can quickly mobilize developers from various areas of expertise to put a 
working backend implementation together and test this at scale.
- **Transparency:** With the entire codebase out in the open, we can provide the greatest degree of transparency into how this system works which will hopefully help to alleviate
reasonable concern regarding user privacy. Providing a high degree of transparency will be critical to achieve widespread adoption.

[Read more here](docs/why-contact-tracing.md) on why we need contact tracing in the first place.

## What exactly is this?

This project consists of a number of components:

- **Database schema and implementation**: A scalable database model to build a contact graph between devices and their owners in order to track who was in close proximity to whom at what point in time for
effective disease containment while storing the least amount of personal information. The key to this component is a scalable, highly flexible graph database backend that can handle the vast amounts of data
that need to be collected while providing the flexibility to efficiently implement the types of analyses that authorities need to effectively contain the virus. 
[Read more about this component.](database/)

- **Web application backend**: A web application that stores the contacts recorded and uploaded by devices that run the contact tracing application. This backend provides a RESTful API for mobile applications to upload
their contact records to as well as endpoints for authorities to quickly run possible spread analyses.
[Read more about this component.](backend/)

- **Simulator**: A simple contact simulator for generating artificial contact graphs to test the system with. [Read more about this component.](simulator/)

*Disclaimer:* This is a rough-cut early prototype which is **NOT** production ready (e.g. no security, no scale testing, etc). 
It is meant to serve as a proof of concept that can evolve into a working solution. The interfaces are speculative until Singapore open-sources the smart phone application that would be providing the data.
It needs a lot of work and refinement. My goal is for this to be a starting point.
If you can help make this a working system, please reach out to me or contribute. I am primarily a database expert and need help on the application side. 

Note, that this project does *NOT* contain the implementation of a smartphone application that would detect and record device encounters. We are currently assuming that such apps are being actively developed
and open-sourced soon. This project focuses on the backend implementation where the collected data is consolidated and operationalized. 

## How do you try this out?

Follow these steps to get this prototype up and running so you can see how it works and play around with it:

1. **Prerequisites:** Make sure your system has Java SDK 1.8 (build 150 or higher) and maven 3.4+ installed

2. **Clone this repository** to your local machine. 

3. **Install DSE Database:** This implementation uses the DSE Graph database as the datastore. [Download DataStax Enterprise (DSE) 6.8](https://downloads.datastax.com/#enterprise)
 and follow the [installation instructions](https://docs.datastax.com/en/install/6.8/install/dseBasicInstall.html) 
 to install it on your machine but don't run it yet (i.e. only follow until step 4). 
Navigate to the directory where you unpacked DSE and then run the following command to start it in graph mode and make sure there are no error messages during startup:
```
bin/dse cassandra -g
```

4. **Run the backend:** Navigate to the directory where you cloned this repository and into the `backend` sub-directory. Run `mvn package` to build the backend and run it with
```
java -jar target/contacttracer-0.1-SNAPSHOT.jar server config.yml
```

5. **Test backend:** Open the following URL in your browser: `http://localhost:8081/`. This shows you the admin console of the backend application. Click on *Healthcheck* and verify that the database is healthy.
To run the actual tracing analysis, open the URL: `http://localhost:8080/tracer/f11177d2-ec63-3995-bb4a-c628e0d782df/infected`. You should get an empty array because the database is empty.

6. **Generate data:** To make things more interesting, let's load some data into the database. For that, we are first going to generate some contact data. Navigate to the `simulator` directory within the cloned
repository and run  `mvn package -DskipTests` which builds the simulator. To generate contact data for 100 individuals over a 28 day period into the `../data/` directory, run the following command:
```
java -jar target/covid19-simulator-0.1-SNAPSHOT-jar-with-dependencies.jar -f ../data/test_ -n 100 -l 672 -d 5 -p 0.02
```

7. **Load data:** Go to the `data` directory in the repository root which should contain 4 csv files from the simulation you just ran. To efficiently load those into the database, we are going to use a handy
tool called `dsbulk`. Download [Bulk Loader 1.5](https://downloads.datastax.com/#bulk-loader) and then following the 
[installation instructions](https://docs.datastax.com/en/dsbulk/doc/dsbulk/install/dsbulkInstall.html). Then you can import the data by simply running the script `./load.sh` that 
is already in the `data` directory. This will execute 4 loading operations to populate a sample contact tracing graph into the database.

8. **Try it out:** Refresh the URL `http://localhost:8080/tracer/f11177d2-ec63-3995-bb4a-c628e0d782df/infected` and you should get a list of individuals that could potentially have been infected by the
traced individual together with their first point of contact and the total exposure duration aggregated across all contacts.

9. **Play with the data:** If you want to play around with the data freeform and design your own queries, the `database` directory contains a notebook that you can use as a starting point.
To access the notebook, you need to install another useful tool called Studio. Download [Studio 6.8](https://downloads.datastax.com/#studio) and follow the installation instructions. Once the Studio server
is up and running, you can open the studio application in your browser and import the notebook that is stored in the `database` directory by dropping it onto the **+** icon on the home screen.
Once you open the notebook, feel free to play with the queries it contains and use those as the starting point to create your own queries.

This is a brief introduction to the components of this contact tracing prototype. To learn more, click on each of the components:

- **[Database](database/)**: Provides more details on the choice of database, the schema, and the notebook to explore the data easily.
- **[Backend](backend/)**: Provides more details on the dropwizard application for recording data and providing tracing analysis.
- **[Simulator](simulator/)**: Provides more details on how the simulator works.

Note, that DataStax Enterprise (DSE) is free for development use and DataStax is also making DSE freely available for any production use by 
officials and authorities for the purposes of COVID19 contact tracing as demonstrated in this project. As such, all the components and dependencies of this project
are free to use.

## How can you help?

This is just an initial prototype to demonstrate that we can implement a fully automated, scalable, and transparent backend and datastore for contact tracing. It needs more work to be ready for production.
This is where you come in:

- The backend needs to be more robust, secure, and safe. 
- We also need to adjust the interface of the backend to be compatible with the mobile contact recording application. This depends on which application we can use for that purpose 
(e.g. the one promised to be open-sourced by Singapore).
- We need an interface for authorities and officials to use the backend and work with users of this system to make sure it meets their needs.
- We need to make this system easy to deploy. Currently, the prototype is only optimized for local deployment.
- We need to test this system at scale.

Please reach out if you can help or open a pull request.