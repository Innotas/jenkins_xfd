
- doc what the lights mean, either in README.md or in the StoplightController javadoc. 
- add configurability to the build environment, including user/host for deploying.
- add configurability to the runtime environment, possibly including a Main class that can be tested.
- switch from external process impl to the USB impl of the stoplight, or make the external process impl handle animation better, because when two lights are flashing the sequences obviously slow each other down.
- Add support for an onDeviceChange event fired from the stoplight, to detect a user change caused by pushing the device button.  Then detecting one button push can snooze any animations, pushing twice can turn the light off for 30 minutes, and pushing it a third time can restore things to normal.
- When the stoplight-stop.sh script runs, it errors when there is not service already running.
- When running './gradlew deploy', the build seems to succeed but then hang.
- JenkinsProjectSet and JenkinsProject could be smarter about retaining state, up to a point.  When we have a specific job that we know is running, we can hold on to its URL and poll only that until we learn that it's finished.  Then we have to throw it away and go back to the project to get status (because it might have a new job kicked off immediately). But this smartness would avoid some unnecessary network I/O when jobs are running.
- after certain repeated network errors (503) the controller seems to get hung.
