# cs446fall2016project
for cs446, UIUC, fall 2016
Max Bass, netid: mhbass2.

===================       Where to start:

This repo is for a personal research project associated with a class. The work was done for that class, and under 
Prof. Balajee Vanaman (CS dept. UIC)

The main file to look at first is the project report, that was written as a result of the work. Project_report.pdf. There is also a latex source file for this report, included.


=================        Brief Summary/Description

Project Goal: 
use Machine Learning for computer networking applications, by clustering rules so that they could be sorted through more
efficiently by a packet filter. Packet filtering rules ("policies") each have a priority, and an action (e.g. drop packets, or redirect).
Rules with the same action can be clustered together, according to their closeness in rule-space.

parameters:
This project mostly uses language, terms, and concepts from Machine Learning, although some networking terms and concepts are introduced as well.
ML terms: feature, feature space, k-means clustering, clustering, 
Networking terms: packet, ip address, port, ip-mask, ip-range, filtering rule

Project outcome:
Specifically, what I ended up actually implementing was a k-means clustering algorithm, which combined packet filtering rules. 
It ends up with impractically high error (total new rule area minus old rule area). Maybe in the future, more heuristics could be used 
to more effectively combine rules though,and thus achieve much higher accuracy. One requirement will probably be highly dense rules in
rule space. 

===================        Code Files
You will need a working weka library to run any of the java files.

my_cluster_filter.java contains the main clustering algorithm, and the main method, so that is the file you run. You can run it in eclipse, or if you compile it in the command line. It depends on the cluster.java file, as well as the weka library. 

Data Files:
The feature generator files will work on the edited synthetic filters, which is in the zipped up data folder. It works to generate the
arff files, which are also in the zipped up data folder. The arff files already exist, so you don't really need to run this one, unless
you want to play around with the features used, and possibly take, for instance, protocol (UDP vs TCP) into account as well. Protocol is
listed in the synthetic filters file, but wasn't used when generating the arff files. The "edited" is just referring to some minor
formatting changes from the original synthetic filter files (removing the "@" symbol, changing the spacing, etc.). 

