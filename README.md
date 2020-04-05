# SARS-CoV-2 Contact Tracing

This project demonstrates an open-source backend implementation of automated, scalable contact tracing to identify potentially infected individuals for testing and quarantine.
Assuming that Singapore will soon open-source their contact detection application, this project could serve as the backend for capturing the contact information and computing
likely infection paths for containment. This project contains a scalable database implementation for capturing and analyzing the contact data, a web application to access the data,
a simulator for generating people interaction data for testing purposes, and a notebook to analyze the resulting contact graph.



## Why do we need this?

Automated, scalable contact tracing is needed if we want to effectively contain the spread of the pandemic without shutting down the economy and devasting people's lives.
However, collecting such intimate information is an invasion of privacy that needs to be restricted and made transparent as much as possible. The goal of this project is to develop
a scalable, automated, effective, and transparent database and backend implementation of contact tracing as an open-source projects for authorities to quickly adapt and implement.

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

- **Database schema and implementation**: A scalable database model to build contact graph between devices and their owners in order to track who was in close proximity to whom at what point in time for
effective disease containment while storing the least amount of personal information.

- **Web application**: A web application that stores the contact records recorded and uploaded by devices that run the contact tracing application.

- **Simulator**: A simple contact simulator for generating artificial contact graphs to test the system with.

*Disclaimer:* This is a rough-cut early prototype which is **NOT** production ready (e.g. no security, no scale testing, etc). 
It is meant to serve as a proof of concept that can evolve into a working solution. The interfaces are speculative until Singapore open-sources the smart phone application that would be providing the data.
It needs a lot of work and refinement. My goal is for this to be a starting point.
If you can help make this a working system, please reach out to me or contribute. I am primarily a database expert and need help on the application side. 

Note, that this project does *NOT* contain the implementation of a smartphone application that would detect and record device encounters. We are currently assuming that such apps are being actively developed
and open-sourced soon. This project focuses on the backend implementation where the collected data is consolidated and operationalized. 

## How do you try this out?

