TODO
====

Implementation:

- [ ] Read inside AsyncTasks to check if they have posted tasks
- [ ] Clear propogated AsyncTasks at the end of a test case
- [ ] Handle not-overwritten onPostExecute of AsyncTasks
- [ ] Better arrange support libraries in ReflectionUtils
- [ ] Read delays/schedule from persistent storage
- [x] Modify the scheduler so that it checks threads' message queues


Experimental work:

- [ ] Define a coverage metric
- [ ] Apply the app to a number of applications and collect results
- [x] Reproduce the motivating example
- [x] Execute a single schedule of a few hand-picked apps, for fixed input sequence
- [x] Execute multiple schedules of those apps
- [x] Enumerate all schedules of those apps up to a delay bound
- [x] Record a sequence of user inputs and replay in all schedules
- [ ] Enumerate schedules of reasonable number (10 percent?) of examples
