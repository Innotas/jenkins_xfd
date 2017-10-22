# What Do All The Lights Mean
This system uses three colored lights to monitor two different builds, or two different groups of builds.  *Reading the
status is intuitive with a little experience*, but a little tedious to explain. 

## High Priority Builds
High priority builds can be configured however you like, but they are generally the production build jobs for a product.
A failure in one of these builds means the product could not be shipped as-is, which is a serious problem.

## Medium Priority Builds
Medium priority build jobs are those that require alerting, but may not be as serious as High priority build jobs.
Static analysis jobs might fall into this category, but again, it can be configured however you like.

## Light Meaning

In addition to high and medium priorities, the controller using flashing to indicate activity, such that individual 
lights can be interpreted as:

- Solid Red = a high priority build has failed, and no high priority build job is currently running.
- Flashing Red = a high priority build has failed, but a build job is currently running.
- Quickly Flashing Red = a high priority build has fail, but a running build job is almost finished.
- Solid Yellow = a medium priority build has failed, and no medium priority build job is currently running.
- Flashing Yellow = a medium priority build has failed, but a build job is currently running.
- Quickly Flashing Yellow = a medium priority build has fail, but a running build job is almost finished.
- Solid Green = a set of builds, high or medium priority or both, are passing, with no running jobs.
- Flashing Green = a build job that previously passed is currently running.  
- Quickly Flashing Green = a build job that previously passed is running and almost complete.

## Status Examples

| Stoplight                                                                                                                           | Meaning                                                                                                          |
|:-----------------------------------------------------------------------------------------------------------------------------------:| ---------------------------------------------------------------------------------------------------------------- |
| ![Solid Green](stoplight_diagram_solid_green.png?raw=true "Solid Green")                                                            | All is well. All builds succeeded.                                                                               |
| ![Flashing Green](stoplight_diagram_flashing_green.png?raw=true "Flashing Green")                                                   | All is well. One or more builds are running.                                                                     |
| ![Solid Red and Solid Green](stoplight_diagram_solid_red_solid_green.png?raw=true "Solid Red and Solid Green")                      | High-priority failed. Medium priority succeeded.                                                                 |
| ![Solid Red and Solid Yellow](stoplight_diagram_solid_red_solid_yellow.png?raw=true "Solid Red and Solid Yellow")                   | High-priority failed. Medium priority failed.                                                                    |
| ![Flashing Red and Flashing Yellow](stoplight_diagram_flashing_red_flashing_yellow.png?raw=true "Flashing Red and Flashing Yellow") | High-priority failed, but a new job is running. Medium priority failed, but a new job is running.                | 

