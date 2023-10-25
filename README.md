The system is designed to schedule and execute jobs of different types asynchronously

System has the following functionality:

* scheduleJob(Job job) - register a new job and remove finished tasks
* cancelJob(Job job) - cancel a job
* waitAndShutdown() - checks whether all jobs have completed and then stop the system
* getNumberOfRunningJobs() - return the number of simultaneously performed jobs

To run tests:

* mvn clean test 

