# SARS-CoV-2 Contact Tracing

This project demonstrates an open-source backend implementation of automated, scalable contact tracing to identify potentially infected individuals for testing and quarantine.
Assuming that Singapore will soon open-source their contact detection application, this project could serve as the backend for capturing the contact information and computing
likely infection paths for containment. This project contains a scalable database implementation for capturing and analyzing the contact data, a web application to access the data,
a simulator for generating people interaction data for testing purposes, and a notebook to analyze the resulting contact graph.

## Why do we need this?

**Tl;dr**: Automated, scalable contact tracing is needed if we want to effectively contain the spread of the pandemic without shutting down the economy and devasting people's lives.
However, collecting such intimate information is an invasion of privacy that needs to be restricted and made transparent as much as possible. That is the goal of this project.   

As the coronavirus pandemic is sweeping across the globe, most of the United States and many places all over the world are trying to significantly restrict people's movement 
through "shelter-in-place" or more severe orders. The goal is to "flatten the curve" which means to slow the spread of the disease in order to avoid overwhelming the health care
system which would lead to massive casualties from COVID19 as well as preventable deaths from other causes due to hospitals being unable to treat incoming patients.

To avoid this doomsday scenario of hundreds of thousands of people dying at home without proper care, we have to keep these control measures in place until either of the following happens:
1. The virus has infected a large enough percentage of population who have recovered that we effectively have herd immunity, which means there are enough people who are or have become immune
to the virus that it runs out of hosts to invade and dies out. 
2. We develop a vaccine against SARS-CoV-2 which works and doesn't have crazy side effects, i.e. it makes it through clinical trials successfully. Then we vaccinate a large enough part of the
population to get herd immunity.

Flattening the curve means that option 1 will take a long time because we are trying to limit the spread. How long exactly depends on how many people are already immune, what the hospitalization rate
is exactly and a number of other factors. But even optimistic models suggest that it will be at least a year. That's also how long it will take to get a vaccine in the best case.

So, realistically, we would have to keep up with the current control measures for at least a year to avoid overwhelming the health care system and the massive casualties that would result in.

However, restricting movement and closing many businesses for that reason already has a devastating impact on the economy after only a few weeks. If we keep this up for a whole year the economic
devastation is likely to be extremely severe and result in a great depression. This will have massive negative impact on people's livelihoods and health as people loose their jobs, their businesses,
and their health insurance. Not to mention the psychological and emotional toll we will incur by isolating people for such a prolonged period of time.

South Korea and Singapore have demonstrated that there is an alternative path out of this dilemma: Use severe control measures for a few months until we understand the spread of the infection well enough
to contain it via widespread testing, tracing, and isolation of affected individuals only. That would allow us to resume mostly normal economic operations and social functions with some caveats (e.g.
wearing face masks, avoiding unnecessary body contact, being more diligent about washing hands and cleaning surface areas, etc) and some limitations (e.g. very large gatherings of people like football games
would likely still be too risky).

However, while we are ramping up testing capacity to allow us to implement this model, our current approach to tracing infections to identify people who are at risk and need to be tested or isolated
does not scale to a population of hundreds of millions of people. Manually collecting such data and using people's memory to recall whom they have been in contact with is too laborious and error prone.

We need a technical solution to this problem. Most everybody nowadays carries a smartphone or similar electronic device on them at all times. We can use those devices to keep track of whom we have come
in contact with and automatically record this data. That would solve the "faulty memory" problem and make it simpler to identify who may have been exposed and needs to be tested. Singapore has announced
that they will open-source the application they wrote to do exactly this via bluetooth (i.e. detect which other devices are in close proximity via bluetooth signal).
In addition, we need a database to collect all this data and build a contact graph across the entire population so that we can easily determine who is at risk when somebody tests positive or shows symptoms.
This would enable authorities to implement effective containment measures that are good enough to allow us to relax the current control measures without causing massive harm.

The prospect of a central database that collects information on who was in contact with whom is scary because it invades our privacy. The goal of this project is to demonstrate that we can implement
effective contact tracing with a minimal amount of personal identifiable information and that we can do it at a scale that allows us to roll this out across the population. Developing this an an
open-source project allows us to come together and get this done as quickly as possible while providing the greatest degree of transparency.

## What exactly is this?

This project consists of a number of components:

- **Database schema and implementation**: A scalable database model to build a contact graph between devices and their owners in order to track who was in close proximity to whom at what point in time for
effective disease containment while storing the least amount of personal information.

- **Web application**: A web application that stores the contacts recorded and uploaded by devices that run the contact tracing application.

- **Simulator**: A simple contact simulator for generating artificial contact graphs to test the system with.

*Disclaimer:* This is a rough-cut early prototype which is **NOT** production ready (e.g. no security, no scale testing, etc). 
It is meant to serve as a proof of concept that can evolve into a working solution. The interfaces are speculative until Singapore open-sources the smart phone application that would be providing the data.
It needs a lot of work and refinement. My goal is for this to be a starting point.
If you can help make this a working system, please reach out to me or contribute. I am primarily a database expert and need help on the application side.  

## How do you try this out?

