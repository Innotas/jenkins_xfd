# How to Configure Jenkins
No special plugins are required in the Jenkins installation. The controller uses the RESTful endpoints natively exposed
by Jenkins. The only configuration needed is to specify which build projects within Jenkins are _high_ priority and 
which are _medium_ priority.

## Designating High-Priority Build Projects
The Jenkins dashboard lists build _projects_ in *views*, with each _view_ represented as a tab on the dashboard. A view
is simply a subset of projects, where any project can belong to more than one view.

Our stoplight controller expects to find a view that contains the high-priority build projects. By default, it looks for
the `Priority-High` view. Any build project appearing in this view will be considered a high-priority project, and any 
failure among high-priority build projects will be indicated on the _red_ light.

## Designating Medium-Priority Build Projects
Similarly, medium priority build projects are designated by adding them to the medium priority Jenkins view, called
`Priority-Medium` by default. Any build project appearing in this view will be considered a medium-priority project, and 
any failure among medium-priority build projects will be indicated on the _yellow_ light.

## Optional Jenkins Plug-In
While no Jenkins plugins are required, the 
[Radiator View Plugin](https://wiki.jenkins.io/display/JENKINS/Radiator+View+Plugin) may be useful for displaying the
Jenkins 'view' (sublist of build projects) as a single status value, the way it will be indicated on the extreme 
feedback device.
