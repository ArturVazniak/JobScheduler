The system is designed to schedule and execute jobs of different types asynchronously

System has the following functionality:

* scheduleJob(Job job) - register a new job and remove finished tasks
* cancelJob(Job job) - cancel a job
* getNumberOfRunningJobs() - return the number of simultaneously performed jobs

To run tests:

* mvn clean test 

